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

import java.io.InputStream;
import org.eclipse.osee.framework.db.connection.exception.OseeArgumentException;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeWrappedException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.io.xml.XmlTextInputStream;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.word.WordAnnotationHandler;
import org.eclipse.osee.framework.skynet.core.word.WordUtil;

/**
 * @author Jeff C. Phillips
 */
public class WordAttribute extends StringAttribute {
   public static final String WORD_TEMPLATE_CONTENT = "Word Template Content";
   public static final String WHOLE_WORD_CONTENT = "Whole Word Content";
   public static final String OLE_DATA_NAME = "Word Ole Data";
   public static boolean noPopUps = false;
   public static String displayTrackedChangesErrorMessage = "";

   /**
    * wraps the value in a simple word paragraph
    * 
    * @param attributeType
    * @param value
    */
   public WordAttribute(AttributeType attributeType, Artifact artifact) {
      super(attributeType, artifact);
      displayTrackedChangesErrorMessage = "";
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.attribute.Attribute#setValue(java.lang.Object)
    */
   @Override
   public boolean subClassSetValue(String value) throws OseeCoreException {
      // Do not allow save on tracked changes
      if (WordAnnotationHandler.containsWordAnnotations(value)) {
         displayTrackedChangesErrorMessage = "Detected tracked changes on for this artifact.";
         throw new OseeArgumentException(displayTrackedChangesErrorMessage);
      } else {
         value = WordUtil.removeWordMarkupSmartTags(value);
         return super.subClassSetValue(value);
      }
   }

   public boolean containsWordAnnotations() throws OseeCoreException {
      String temp = getValue();
      return WordAnnotationHandler.containsWordAnnotations(temp);
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.attribute.Attribute#getDisplayableString()
    */
   @Override
   public String getDisplayableString() throws OseeCoreException {
      InputStream inputStream = null;
      try {
         inputStream = new XmlTextInputStream(getValue());
         return Lib.inputStreamToString(inputStream);
      } catch (Exception ex) {
         throw new OseeWrappedException(ex);
      } finally {
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (Exception ex) {
               // Do Nothing
            }
         }
      }
   }

   /**
    * Mainly used for testing purposes
    * 
    * @return the noPopUps
    */
   public static boolean isNoPopUps() {
      return noPopUps;
   }

   /**
    * Mainly used for testing purposes
    * 
    * @param noPopUps the noPopUps to set
    */
   public static void setNoPopUps(boolean noPopUps) {
      WordAttribute.noPopUps = noPopUps;
   }

   /**
    * Mainly used for testing purposes
    * 
    * @return the displayTrackedChangesErrorMessage
    */
   public static String getDisplayTrackedChangesErrorMessage() {
      return displayTrackedChangesErrorMessage;
   }

   /**
    * Mainly used for testing purposes
    * 
    * @param displayTrackedChangesErrorMessage the displayTrackedChangesErrorMessage to set
    */
   public static void setDisplayTrackedChangesErrorMessage(String displayTrackedChangesErrorMessage) {
      WordAttribute.displayTrackedChangesErrorMessage = displayTrackedChangesErrorMessage;
   }
}