/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.attribute;

import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.util.HttpProcessor;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.utils.AttributeURL;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.internal.OseeSql;
import org.eclipse.osee.framework.skynet.core.transaction.BaseTransactionData;

/**
 * @author Jeff C. Phillips
 * @author Roberto E. Escobar
 */
public class AttributeTransactionData extends BaseTransactionData {
   private static final String INSERT_ATTRIBUTE =
      "INSERT INTO osee_attribute (art_id, attr_id, attr_type_id, value, gamma_id, uri) VALUES (?, ?, ?, ?, ?, ?)";

   private final Attribute<?> attribute;
   private final DAOToSQL daoToSql;

   public AttributeTransactionData(Attribute<?> attribute) {
      super(attribute.getId(), attribute.getModificationType());
      this.attribute = attribute;
      this.daoToSql = new DAOToSQL();
   }

   public Attribute<?> getAttribute() {
      return attribute;
   }

   @Override
   public OseeSql getSelectTxNotCurrentSql() {
      return OseeSql.TX_GET_PREVIOUS_TX_NOT_CURRENT_ATTRIBUTES;
   }

   @Override
   protected void addInsertToBatch(InsertDataCollector collector) throws OseeCoreException {
      super.addInsertToBatch(collector);
      if (!attribute.isUseBackingData()) {
         attribute.getAttributeDataProvider().persist(getGammaId());
         daoToSql.setData(attribute.getAttributeDataProvider().getData());
         internalAddInsertToBatch(collector, 3, INSERT_ATTRIBUTE, attribute.getArtifact().getArtId(), getItemId(),
            attribute.getAttributeType().getId(), daoToSql.getValue(), getGammaId(), daoToSql.getUri());
      }
   }

   @Override
   protected void internalUpdate(TransactionRecord transactionId) throws OseeCoreException {
      attribute.internalSetGammaId(getGammaId());
      attribute.getArtifact().setTransactionId(transactionId.getId());
   }

   @Override
   protected void internalClearDirtyState() {
      attribute.setNotDirty();
   }

   @Override
   protected void internalOnRollBack() throws OseeCoreException {
      if (!attribute.isUseBackingData() && Strings.isValid(daoToSql.getUri())) {
         try {
            HttpProcessor.delete(AttributeURL.getDeleteURL(daoToSql.getUri()));
         } catch (Exception ex) {
            OseeExceptions.wrapAndThrow(ex);
         }
      }
   }

   @Override
   protected int createGammaId() throws OseeCoreException {
      return attribute.isUseBackingData() ? attribute.getGammaId() : getNextGammaIdFromSequence();
   }

   private static final class DAOToSQL {
      private String uri;
      private String value;

      public DAOToSQL(Object... data) {
         if (data != null) {
            setData(data);
         } else {
            uri = null;
            value = null;
         }
      }

      public void setData(Object... data) {
         this.uri = getItemAt(1, data);
         this.value = getItemAt(0, data);
      }

      private String getItemAt(int index, Object... data) {
         String toReturn = null;
         if (data != null && data.length > index) {
            Object obj = data[index];
            if (obj != null) {
               toReturn = obj.toString();
            }
         }
         return toReturn;
      }

      public String getUri() {
         return uri != null ? uri : "";
      }

      public String getValue() {
         return value != null ? value : "";
      }
   }

   @Override
   protected void internalAddToEvents(ArtifactEvent artifactEvent) {
      return;
   }

}