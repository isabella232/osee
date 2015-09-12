/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.internal.writer;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;
import org.eclipse.osee.orcs.writer.model.reader.OwArtifact;
import org.eclipse.osee.orcs.writer.model.reader.OwArtifactToken;
import org.eclipse.osee.orcs.writer.model.reader.OwArtifactType;
import org.eclipse.osee.orcs.writer.model.reader.OwAttribute;
import org.eclipse.osee.orcs.writer.model.reader.OwAttributeType;
import org.eclipse.osee.orcs.writer.model.reader.OwCollector;
import org.eclipse.osee.orcs.writer.model.reader.OwRelation;
import org.eclipse.osee.orcs.writer.model.reader.OwRelationType;

/**
 * @author Donald G. Dunne
 */
public class OrcsCollectorWriter {

   private final OwCollector collector;
   private final OrcsApi orcsApi;
   private Map<Long, ArtifactId> uuidToArtifact;
   private TransactionBuilder transaction;
   private IOseeBranch branch;
   private ArtifactReadable user;
   private final XResultData results;

   public OrcsCollectorWriter(OrcsApi orcsApi, OwCollector collector, XResultData results) {
      this.orcsApi = orcsApi;
      this.collector = collector;
      this.results = results;
      uuidToArtifact = new HashMap<>();
   }

   public XResultData run() {
      XResultData results = new XResultData(false);
      processCreate(results);
      processUpdate(results);
      processDelete(results);

      getTransaction().commit();
      return results;
   }

   private void processDelete(XResultData results) {
      for (OwArtifactToken owArtifact : collector.getDelete()) {
         ArtifactReadable artifact = orcsApi.getQueryFactory().fromBranch(getBranch()).andUuid(
            owArtifact.getUuid()).getResults().getAtMostOneOrNull();
         if (artifact == null) {
            results.logWarningWithFormat("Delete Artifact Token %s does not exist in database.  Skipping", owArtifact);
         } else {
            getTransaction().deleteArtifact(artifact);
            results.logWithFormat("Deleted artifact %s", owArtifact);
         }
      }
   }

   private void processUpdate(XResultData results) {
      for (OwArtifact owArtifact : collector.getUpdate()) {
         ArtifactReadable artifact = orcsApi.getQueryFactory().fromBranch(getBranch()).andUuid(
            owArtifact.getUuid()).getResults().getAtMostOneOrNull();

         if (artifact == null) {
            throw new OseeArgumentException("Artifact not found for OwArtifact %s", owArtifact);
         }

         if (!owArtifact.getName().equals(artifact.getName())) {
            getTransaction().setName(artifact, owArtifact.getName());
            logChange(artifact, CoreAttributeTypes.Name, artifact.getName(), owArtifact.getName());
         }

         for (OwAttribute owAttribute : owArtifact.getAttributes()) {
            IAttributeType attrType = getAttributeType(owAttribute.getType());

            if (artifact.getAttributeCount(attrType) <= 1 && owAttribute.getValues().size() <= 1) {
               String currValue = artifact.getSoleAttributeAsString(attrType, null);

               String newValue = null;
               if (owAttribute.getValues().size() == 1) {
                  Object object = owAttribute.getValues().iterator().next();
                  if (object != null) {
                     newValue = owAttribute.getValues().iterator().next().toString();
                  }
               }

               // handle delete attribute case first
               if (Strings.isValid(currValue) && newValue == null) {
                  logChange(artifact, attrType, currValue, newValue);
                  getTransaction().deleteAttributes(artifact, attrType);
               } else if (orcsApi.getOrcsTypes().getAttributeTypes().isBooleanType(attrType)) {
                  Boolean currVal = getBoolean(currValue);
                  Boolean newVal = getBoolean(newValue);
                  if (currVal == null || !currVal.equals(newVal)) {
                     logChange(artifact, attrType, currValue, newValue);
                     getTransaction().setSoleAttributeValue(artifact, attrType, newVal);
                  }
               } else if (orcsApi.getOrcsTypes().getAttributeTypes().isFloatingType(attrType)) {
                  try {
                     Double currVal = getDouble(currValue);
                     Double newVal = getDouble(newValue);
                     if (currVal == null || !currVal.equals(newVal)) {
                        logChange(artifact, attrType, currValue, newValue);
                        getTransaction().setSoleAttributeValue(artifact, attrType, newVal);
                     }
                  } catch (Exception ex) {
                     throw new OseeArgumentException("Exception processing Double for OwAttribute %s Exception %s",
                        owAttribute, ex);
                  }
               } else if (orcsApi.getOrcsTypes().getAttributeTypes().isIntegerType(attrType)) {
                  try {
                     Integer currVal = getInteger(currValue);
                     Integer newVal = getInteger(newValue);
                     if (currVal == null || !currVal.equals(newVal)) {
                        logChange(artifact, attrType, currValue, newValue);
                        getTransaction().setSoleAttributeValue(artifact, attrType, newVal);
                     }
                  } catch (Exception ex) {
                     throw new OseeArgumentException("Exception processing Integer for OwAttribute %s Exception %s",
                        owAttribute, ex);
                  }
               } else if (orcsApi.getOrcsTypes().getAttributeTypes().isDateType(attrType)) {
                  try {
                     Date currVal = artifact.getSoleAttributeValue(attrType, null);
                     Date newVal = getDate(newValue);
                     if (currVal == null || currVal.compareTo(newVal) != 0) {
                        logChange(artifact, attrType, DateUtil.getMMDDYYHHMM(currVal), DateUtil.getMMDDYYHHMM(newVal));
                        TransactionBuilder tx = getTransaction();
                        tx.setSoleAttributeValue(artifact, attrType, newVal);
                     }
                  } catch (Exception ex) {
                     throw new OseeArgumentException("Exception processing Integer for OwAttribute %s Exception %s",
                        owAttribute, ex);
                  }
               } else if ((currValue == null && newValue != null) || (currValue != null && !currValue.equals(
                  newValue))) {
                  logChange(artifact, attrType, currValue, newValue);
                  getTransaction().setSoleAttributeValue(artifact, attrType, newValue);
               }
            }

         }
      }
   }

   private void logChange(ArtifactReadable artifact, IAttributeType attrType, String currValue, String newValue) {
      results.log(String.format("Attr Values not same; Current [%s], New [%s] for attr type [%s] and artifact %s",
         currValue, newValue, attrType, artifact.toStringWithId()));
   }

   private Integer getInteger(String value) {
      Integer result = null;
      if (Strings.isValid(value)) {
         result = Integer.valueOf(value);
      }
      return result;
   }

   private Double getDouble(String value) {
      Double result = null;
      if (Strings.isValid(value)) {
         result = Double.valueOf(value);
      }
      return result;
   }

   private Boolean getBoolean(String value) {
      if (Strings.isValid(value)) {
         if (value.toLowerCase().equals("true")) {
            return true;
         } else if (value.toLowerCase().equals("false")) {
            return false;
         } else if (value.equals("1")) {
            return true;
         } else if (value.equals("0")) {
            return false;
         }
      }
      return null;
   }

   private IAttributeType getAttributeType(OwAttributeType attributeType) {
      if (attributeType.getUuid() <= 0L) {
         for (IAttributeType type : orcsApi.getOrcsTypes().getAttributeTypes().getAll()) {
            if (type.getName().equals(attributeType.getName())) {
               return type;
            }
         }
         throw new OseeArgumentException("Invalid attribute type name [%s]", attributeType);
      }
      return orcsApi.getOrcsTypes().getAttributeTypes().getByUuid(attributeType.getUuid());
   }

   private void processCreate(XResultData results) {
      for (OwArtifact owArtifact : collector.getCreate()) {
         OwArtifactType owArtType = owArtifact.getType();
         IArtifactType artType = orcsApi.getOrcsTypes().getArtifactTypes().getByUuid(owArtType.getUuid());

         long artifactUuid = owArtifact.getUuid();
         if (artifactUuid > 0L) {
            if (uuidToArtifact == null) {
               uuidToArtifact = new HashMap<>();
            }
         } else {
            artifactUuid = Lib.generateArtifactIdAsInt();
         }
         String name = owArtifact.getName();
         ArtifactId artifact = getTransaction().createArtifact(artType, name, GUID.create(), artifactUuid);

         uuidToArtifact.put(artifactUuid, artifact);

         createAttributes(owArtifact, artifact, results);

         createRelations(owArtifact, artifact, results);
      }
   }

   private void createRelations(OwArtifact owArtifact, ArtifactId artifact, XResultData results) {
      for (OwRelation relation : owArtifact.getRelations()) {
         OwRelationType owRelType = relation.getType();
         IRelationType relType = orcsApi.getOrcsTypes().getRelationTypes().getByUuid(owRelType.getUuid());

         OwArtifactToken artToken = relation.getArtToken();
         long branchUuid = collector.getBranch().getUuid();
         ArtifactReadable otherArtifact = null;

         if (uuidToArtifact.containsKey(artToken.getUuid())) {
            otherArtifact = (ArtifactReadable) uuidToArtifact.get(artToken.getUuid());
         } else {
            otherArtifact = orcsApi.getQueryFactory().fromBranch(branchUuid).andUuid(
               artToken.getUuid()).getResults().getExactlyOne();
            uuidToArtifact.put(artToken.getUuid(), otherArtifact);
         }
         if (relation.getType().isSideA()) {
            getTransaction().relate(otherArtifact, relType, artifact);
         } else {
            getTransaction().relate(artifact, relType, otherArtifact);
         }
      }
   }

   private void createAttributes(OwArtifact owArtifact, ArtifactId artifact, XResultData results) {
      for (OwAttribute owAttribute : owArtifact.getAttributes()) {
         if (!CoreAttributeTypes.Name.getGuid().equals(owAttribute.getType().getUuid())) {
            OwAttributeType owAttrType = owAttribute.getType();
            IAttributeType attrType = getAttributeType(owAttrType);

            List<Object> values = owAttribute.getValues();
            for (Object value : values) {
               if (orcsApi.getOrcsTypes().getAttributeTypes().isFloatingType(attrType)) {
                  getTransaction().setSoleAttributeValue(artifact, attrType, Double.valueOf((String) value));
               } else if (orcsApi.getOrcsTypes().getAttributeTypes().isIntegerType(attrType)) {
                  getTransaction().setSoleAttributeValue(artifact, attrType, Integer.valueOf((String) value));
               } else if (orcsApi.getOrcsTypes().getAttributeTypes().isBooleanType(attrType)) {
                  Boolean set = getBoolean((String) value);
                  if (set != null) {
                     getTransaction().setSoleAttributeValue(artifact, attrType, set);
                  }
               } else if (orcsApi.getOrcsTypes().getAttributeTypes().isDateType(attrType)) {
                  Date date = getDate(value);
                  if (date != null) {
                     getTransaction().setSoleAttributeValue(artifact, attrType, date);
                  } else {
                     throw new OseeArgumentException("Unexpected date format [%s]", value);
                  }
               } else if (orcsApi.getOrcsTypes().getAttributeTypes().getMaxOccurrences(attrType) == 1) {
                  getTransaction().setSoleAttributeValue(artifact, attrType, value);
               } else {
                  getTransaction().createAttribute(artifact, attrType, value);
               }
            }
         }
      }
   }

   private Date getDate(Object value) {
      Date date = null;
      boolean resolved = false;
      if (Strings.isNumeric((String) value)) {
         date = new Date(Long.valueOf((String) value));
      } else {
         try {
            date = DateUtil.getDate(DateUtil.MMDDYY, (String) value);
            resolved = true;
         } catch (Exception ex) {
            // do nothing
         }
         if (date == null) {
            try {
               date = DateUtil.getDate("MM/dd/yy", (String) value);
               resolved = true;
            } catch (Exception ex) {
               // do nothing
            }
         }
         if (date == null) {
            try {
               date = DateUtil.getDate(DateUtil.MMDDYYHHMM, (String) value);
               resolved = true;
            } catch (Exception ex) {
               // do nothing
            }
         }
         if (date == null) {
            try {
               Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime((String) value);
               date = calendar.getTime();
               resolved = true;
            } catch (Exception ex) {
               // do nothing
            }
         }
      }
      if (Strings.isValid((String) value) && !resolved) {
         throw new OseeArgumentException("Date format [%s] not supported.", value);
      }
      return date;
   }

   private IOseeBranch getBranch() {
      if (branch == null) {
         branch = orcsApi.getQueryFactory().branchQuery().andUuids(
            collector.getBranch().getUuid()).getResults().getAtMostOneOrNull();
      }
      return branch;
   }

   public TransactionBuilder getTransaction() throws OseeCoreException {
      if (transaction == null) {
         transaction =
            orcsApi.getTransactionFactory().createTransaction(getBranch(), getUser(), collector.getPersistComment());
      }
      return transaction;
   }

   private ArtifactReadable getUser() {
      if (user == null) {
         user = orcsApi.getQueryFactory().fromBranch(CoreBranches.COMMON).and(CoreAttributeTypes.UserId,
            collector.getAsUserId()).getResults().getExactlyOne();
      }
      return user;
   }

}
