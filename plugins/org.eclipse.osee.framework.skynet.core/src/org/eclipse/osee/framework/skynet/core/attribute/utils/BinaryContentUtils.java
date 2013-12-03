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
package org.eclipse.osee.framework.skynet.core.attribute.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;

/**
 * @author Roberto E. Escobar
 */
public class BinaryContentUtils {

   private final static int MAX_NAME_SIZE = 60;

   public static String getContentType(String extension) {
      String contentType = null;
      if (Strings.isValid(extension)) {
         contentType = URLConnection.guessContentTypeFromName("dummy." + extension);
      } else {
         contentType = "application/*";
      }
      return contentType;
   }

   public static String generateFileName(Attribute<?> attribute) throws OseeCoreException {
      StringBuilder builder = new StringBuilder();
      try {
         String name = attribute.getArtifact().getName();
         if (name.length() > MAX_NAME_SIZE) {
            name = name.substring(0, MAX_NAME_SIZE);
         }
         builder.append(URLEncoder.encode(name, "UTF-8"));
         builder.append(".");
      } catch (UnsupportedEncodingException ex) {
         // Do Nothing - this is not important
      }

      builder.append(getStorageName(attribute));

      String fileTypeExtension = getExtension(attribute);
      if (Strings.isValid(fileTypeExtension)) {
         builder.append(".");
         builder.append(fileTypeExtension);
      }
      return builder.toString();
   }

   private static String getExtension(Attribute<?> attribute) throws OseeCoreException {
      String fileTypeExtension = attribute.getAttributeType().getFileTypeExtension();
      if (attribute.isOfType(CoreAttributeTypes.NativeContent)) {
         fileTypeExtension = attribute.getArtifact().getSoleAttributeValue(CoreAttributeTypes.Extension, "");
      }
      return fileTypeExtension;
   }

   public static String getStorageName(Attribute<?> attribute) throws OseeCoreException {
      String guid = attribute.getArtifact().getGuid();
      Conditions.checkExpressionFailOnTrue(!GUID.isValid(guid), "Artifact has an invalid guid [%s]", guid);
      return guid;
   }
}