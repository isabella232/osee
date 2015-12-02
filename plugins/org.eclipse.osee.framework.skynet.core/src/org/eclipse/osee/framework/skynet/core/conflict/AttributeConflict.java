/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/

package org.eclipse.osee.framework.skynet.core.conflict;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Level;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.ConflictStatus;
import org.eclipse.osee.framework.core.enums.ConflictType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.AttributeDoesNotExist;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.io.Streams;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.attribute.EnumeratedAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.StringAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.WordAttribute;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Jeff C. Phillips
 * @author Theron Virgin
 */
public class AttributeConflict extends Conflict {
   public static final String EMPTY_XML = "<w:p><w:r><w:t></w:t></w:r></w:p>";
   public final static String NO_VALUE = "";
   public final static String STREAM_DATA = "Stream data";
   public final static String RESOLVE_MERGE_MARKUP =
      "Can not mark as resolved an attribute that has merge markup.  Finish merging the document to be able to resolve the conflict.";
   public final static String DIFF_MERGE_MARKUP =
      "Can not run a diff against an attribute that has merge markup.  Finish merging the document to be able to resolve the conflict.";
   private final int attrId;
   private final long attrTypeId;
   private Object sourceObject;
   private Object destObject;
   private Attribute<?> attribute = null;
   private Attribute<?> sourceAttribute = null;
   private Attribute<?> destAttribute = null;
   private AttributeType attributeType;
   private boolean mergeEqualsSource;
   private boolean mergeEqualsDest;
   private boolean sourceEqualsDest;
   private static final boolean DEBUG =
      "TRUE".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.osee.framework.ui.skynet/debug/Merge"));

   public AttributeConflict(int sourceGamma, int destGamma, int artId, TransactionRecord toTransactionId, TransactionRecord commitTransaction, String sourceValue, int attrId, long attrTypeId, BranchId mergeBranch, IOseeBranch sourceBranch, IOseeBranch destBranch) throws OseeCoreException {
      super(sourceGamma, destGamma, artId, toTransactionId, commitTransaction, mergeBranch, sourceBranch, destBranch);
      this.attrId = attrId;
      this.attrTypeId = attrTypeId;
      this.status = ConflictStatus.EDITED;
      computeEqualsValues();
   }

   public Attribute<?> getAttribute() throws OseeCoreException {
      if (attribute != null) {
         return attribute;
      }
      Collection<Attribute<Object>> localAttributes = getArtifact().getAttributes(getAttributeType());
      for (Attribute<Object> localAttribute : localAttributes) {
         if (localAttribute.getId() == attrId) {
            attribute = localAttribute;
         }
      }
      return attribute;
   }

   public Attribute<?> getSourceAttribute(boolean allowDeleted) throws OseeCoreException {
      if (sourceAttribute != null) {
         return sourceAttribute;
      }
      Collection<Attribute<?>> localAttributes;
      if (allowDeleted) {
         localAttributes = getSourceArtifact().getAttributes(true);
      } else {
         localAttributes = getSourceArtifact().getAttributes();
      }
      for (Attribute<?> localAttribute : localAttributes) {
         if (localAttribute.getId() == attrId) {
            sourceAttribute = localAttribute;
         }
      }
      if (sourceAttribute == null) {
         if (sourceBranch != null) {
            throw new AttributeDoesNotExist("Attribute %d could not be found on artifact %d on branch %s", attrId,
               getArtId(), sourceBranch.getUuid());
         } else {
            throw new AttributeDoesNotExist("Attribute %d could not be found on artifact %d", attrId, getArtId());
         }
      }
      return sourceAttribute;
   }

   public Attribute<?> getDestAttribute() throws OseeCoreException {
      if (destAttribute != null) {
         return destAttribute;
      }
      Collection<Attribute<Object>> localAttributes = getDestArtifact().getAttributes(getAttributeType());
      for (Attribute<Object> localAttribute : localAttributes) {
         if (localAttribute.getId() == attrId) {
            destAttribute = localAttribute;
         }
      }
      if (destAttribute == null) {
         throw new AttributeDoesNotExist("Attribute %d could not be found on artifact %d on branch %s", attrId,
            getArtId(), destBranch.getUuid());
      }
      return destAttribute;
   }

   private Attribute<?> getAttribute(Artifact artifact) throws OseeCoreException {
      Attribute<?> attribute = null;
      Collection<Attribute<Object>> localAttributes = artifact.getAttributes(getAttributeType());
      for (Attribute<Object> localAttribute : localAttributes) {
         if (localAttribute.getId() == attrId) {
            attribute = localAttribute;
         }
      }
      if (attribute == null) {
         throw new AttributeDoesNotExist("Attribute %d could not be found on artifact %d on branch %s", attrId,
            artifact.getArtId(), artifact.getBranch().getGuid());
      }
      return attribute;
   }

   /**
    * @return the attributeType
    */
   public AttributeType getAttributeType() throws OseeCoreException {
      if (attributeType == null) {
         attributeType = AttributeTypeManager.getTypeByGuid(attrTypeId);
      }
      return attributeType;
   }

   public Object getSourceObject() throws OseeCoreException {
      if (sourceObject != null) {
         return sourceObject;
      }
      try {
         sourceObject = getSourceAttribute(false).getValue();
      } catch (AttributeDoesNotExist ex) {
         sourceObject = "DELETED";
      }
      return sourceObject;
   }

   public Object getDestObject() throws OseeCoreException {
      if (destObject != null) {
         return destObject;
      }
      try {
         destObject = getDestAttribute().getValue();
      } catch (AttributeDoesNotExist ex) {
         destObject = "DELETED";
      }
      return destObject;
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Object getAdapter(Class adapter) {
      if (adapter == null) {
         throw new IllegalArgumentException("adapter can not be null");
      }

      if (adapter.isInstance(this)) {
         return this;
      }

      try {
         Attribute attribute = null;
         try {
            attribute = getSourceAttribute(true);
         } catch (AttributeDoesNotExist ex) {
            // do nothing
         }
         if (adapter.isInstance(attribute)) {
            return attribute;
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }

      return null;
   }

   public int getAttrId() {
      return attrId;
   }

   public long getTypeId() {
      return attrTypeId;
   }

   @Override
   public String getDestDisplayData() throws OseeCoreException {
      String displayValue =
         isWordAttribute() ? STREAM_DATA : getDestObject() == null ? "Null Value" : getDestObject().toString();
      try {
         getDestAttribute();
      } catch (AttributeDoesNotExist ex) {
         displayValue = "DELETED";
      }
      return displayValue;
   }

   @Override
   public String getSourceDisplayData() throws OseeCoreException {
      String displayValue =
         isWordAttribute() ? STREAM_DATA : getSourceObject() == null ? "Null Value" : getSourceObject().toString();
      try {
         getSourceAttribute(false);
      } catch (AttributeDoesNotExist ex) {
         displayValue = "DELETED";
      }
      return displayValue;
   }

   @Override
   public boolean mergeEqualsSource() {
      return mergeEqualsSource;
   }

   @Override
   public boolean mergeEqualsDestination() {
      return mergeEqualsDest;
   }

   @Override
   public boolean sourceEqualsDestination() {
      return sourceEqualsDest;
   }

   public Object getMergeObject() throws OseeCoreException {
      return getAttribute() != null ? getAttribute().getValue() : null;
   }

   public TreeSet<String> getEnumerationAttributeValues() throws OseeCoreException {
      return new TreeSet<String>(AttributeTypeManager.getEnumerationValues(getAttributeType()));
   }

   public boolean setStringAttributeValue(String value) throws OseeCoreException {
      if (!getStatus().isOverwriteAllowed()) {
         if (DEBUG) {
            System.out.println(
               String.format("AttributeConflict: Failed setting the Merge Value for attr_id %d", getAttrId()));
         }
         return false;
      }
      if (DEBUG) {
         System.out.println(String.format("AttributeConflict: Set the Merge Value for attr_id %d", getAttrId()));
      }
      getArtifact().setSoleAttributeFromString(getAttributeType(), value);
      getArtifact().persist(getClass().getSimpleName());
      markStatusToReflectEdit();
      return true;
   }

   public boolean setAttributeValue(Object value) throws OseeCoreException {
      if (!getStatus().isOverwriteAllowed()) {
         if (DEBUG) {
            System.out.println(
               String.format("AttributeConflict: Failed setting the Merge Value for attr_id %d", getAttrId()));
         }
         return false;
      }
      if (DEBUG) {
         System.out.println(String.format("AttributeConflict: Set the Merge Value for attr_id %d", getAttrId()));
      }
      getArtifact().setSoleAttributeValue(getAttributeType(), value);
      getArtifact().persist(getClass().getSimpleName());
      markStatusToReflectEdit();
      return true;
   }

   @Override
   public boolean setToSource() throws OseeCoreException {
      if (!getStatus().isOverwriteAllowed() || getSourceObject() == null) {
         if (DEBUG) {
            System.out.println(String.format(
               "AttributeConflict: Failed setting the Merge Value to the Source Value for attr_id %d", getAttrId()));
         }
         return false;
      }
      if (DEBUG) {
         System.out.println(
            String.format("AttributeConflict: Set the Merge Value to the Source Value for attr_id %d", getAttrId()));
      }
      getArtifact().setSoleAttributeValue(getAttributeType(), getSourceObject());
      getArtifact().persist(getClass().getSimpleName());
      markStatusToReflectEdit();
      return true;
   }

   @Override
   public boolean setToDest() throws OseeCoreException {
      if (!getStatus().isOverwriteAllowed() || getDestObject() == null) {
         if (DEBUG) {
            System.out.println(String.format(
               "AttributeConflict: Failed setting the Merge Value to the Dest Value for attr_id %d", getAttrId()));
         }
         return false;
      }
      if (DEBUG) {
         System.out.println(
            String.format("AttributeConflict: Set the Merge Value to the Dest Value for attr_id %d", getAttrId()));
      }
      getArtifact().setSoleAttributeValue(getAttributeType(), getDestObject());
      getArtifact().persist(getClass().getSimpleName());
      markStatusToReflectEdit();
      return true;
   }

   @Override
   public boolean clearValue() throws OseeCoreException {
      if (!getStatus().isOverwriteAllowed()) {
         if (DEBUG) {
            System.out.println(
               String.format("AttributeConflict: Failed to clear the Merge Value for attr_id %d", getAttrId()));
         }
         return false;
      }
      if (DEBUG) {
         System.out.println(String.format("AttributeConflict: Cleared the Merge Value for attr_id %d", getAttrId()));
      }
      setStatus(ConflictStatus.UNTOUCHED);
      if (isWordAttribute()) {
         getAttribute().resetToDefaultValue();
         getArtifact().persist(getClass().getSimpleName());
      } else {
         getArtifact().setSoleAttributeFromString(getAttributeType(), NO_VALUE);
         getArtifact().persist(getClass().getSimpleName());
      }
      computeEqualsValues();
      return true;
   }

   @Override
   public void computeEqualsValues() throws OseeCoreException {
      mergeEqualsSource = compareObjects(getMergeObject(), getSourceObject());
      mergeEqualsDest = compareObjects(getMergeObject(), getDestObject());
      sourceEqualsDest = compareObjects(getSourceObject(), getDestObject());
   }

   private boolean compareObjects(Object obj1, Object obj2) throws OseeCoreException {
      if (obj1 == null || obj2 == null) {
         return false;
      }
      if (obj1 instanceof InputStream && obj2 instanceof InputStream) {
         InputStream inputStream1 = (InputStream) obj1;
         InputStream inputStream2 = (InputStream) obj2;

         boolean equals = Arrays.equals(Streams.getByteArray(inputStream1), Streams.getByteArray(inputStream2));

         try {
            inputStream1.reset();
            inputStream2.reset();
         } catch (IOException ex) {
            OseeCoreException.wrapAndThrow(ex);
         }

         return equals;
      }
      return obj1.equals(obj2);
   }

   public void markStatusToReflectEdit() throws OseeCoreException {
      computeEqualsValues();
      if (status.equals(ConflictStatus.UNTOUCHED) || status.equals(
         ConflictStatus.OUT_OF_DATE_RESOLVED) || status.equals(ConflictStatus.OUT_OF_DATE) || status.equals(
            ConflictStatus.PREVIOUS_MERGE_APPLIED_CAUTION) || status.equals(
               ConflictStatus.PREVIOUS_MERGE_APPLIED_SUCCESS)) {
         setStatus(ConflictStatus.EDITED);
      }
   }

   @Override
   public ConflictStatus computeStatus() throws OseeCoreException {
      ConflictStatus passedStatus = ConflictStatus.UNTOUCHED;
      try {
         getSourceAttribute(false);
      } catch (AttributeDoesNotExist ex) {
         passedStatus = ConflictStatus.INFORMATIONAL;
      }
      try {
         getDestAttribute();
      } catch (AttributeDoesNotExist ex) {
         passedStatus = ConflictStatus.INFORMATIONAL;

      }
      return super.computeStatus(attrId, passedStatus);
   }

   @Override
   public int getObjectId() {
      return attrId;
   }

   @Override
   public String getMergeDisplayData() throws OseeCoreException {
      ConflictStatus status = getStatus();
      if (status.isUntouched() && !(sourceEqualsDestination() && mergeEqualsSource()) || status.isInformational()) {
         return NO_VALUE;
      }
      if (!isWordAttribute()) {
         return getMergeObject() == null ? null : getMergeObject().toString();
      }
      return AttributeConflict.STREAM_DATA;
   }

   @Override
   public String getChangeItem() throws OseeCoreException {
      return getAttributeType().getName();
   }

   @Override
   public ConflictType getConflictType() {
      return ConflictType.ATTRIBUTE;
   }

   @Override
   public int getMergeGammaId() throws OseeCoreException {
      return getAttribute().getGammaId();
   }

   public boolean isWordAttribute() {
      boolean toReturn = false;
      try {
         toReturn = getAttributeType().equals(CoreAttributeTypes.WordTemplateContent);
      } catch (OseeCoreException ex) {
         OseeLog.log(getClass(), OseeLevel.SEVERE_POPUP, ex);
      }
      return toReturn;
   }

   @Override
   public void setStatus(ConflictStatus status) throws OseeCoreException {
      if (status.equals(
         ConflictStatus.RESOLVED) && isWordAttribute() && ((WordAttribute) getAttribute()).containsWordAnnotations()) {
         throw new OseeStateException(RESOLVE_MERGE_MARKUP);
      }
      super.setStatus(status);
   }

   public boolean wordMarkupPresent() throws OseeCoreException {
      if (isWordAttribute() && ((WordAttribute) getAttribute()).containsWordAnnotations()) {
         return true;
      }
      return false;
   }

   @Override
   public boolean applyPreviousMerge(long mergeBranchId, long destBranchId) throws OseeCoreException {
      if (DEBUG) {
         System.out.println("Apply the merge using the merge branch value " + mergeBranchId);
      }
      if (!getStatus().isResolved()) {
         Artifact mergeArtifact;
         Artifact destArtifact;
         try {
            mergeArtifact =
               ArtifactQuery.getArtifactFromId(getArtifact().getArtId(), TokenFactory.createBranch(mergeBranchId));
            destArtifact =
               ArtifactQuery.getArtifactFromId(getArtifact().getArtId(), TokenFactory.createBranch(destBranchId));
         } catch (ArtifactDoesNotExist ex) {
            return false;
         }
         setAttributeValue(getAttribute(mergeArtifact).getValue());
         computeEqualsValues();
         if (getDestAttribute().getValue().equals(getAttribute(
            destArtifact).getValue()) || getDestAttribute().getGammaId() == getAttribute(destArtifact).getGammaId()) {
            setStatus(ConflictStatus.PREVIOUS_MERGE_APPLIED_SUCCESS);
         } else {
            setStatus(ConflictStatus.PREVIOUS_MERGE_APPLIED_CAUTION);
         }
         return true;
      }
      return false;
   }

   public boolean isSimpleStringAttribute() {
      boolean returnValue = true;
      returnValue &= !isWordAttribute();
      returnValue &= !(attribute instanceof EnumeratedAttribute);
      returnValue &= attribute instanceof StringAttribute;
      return returnValue;
   }

   public boolean involvesNativeContent() throws OseeCoreException {
      return getArtifact().isAttributeTypeValid(CoreAttributeTypes.NativeContent);
   }
}
