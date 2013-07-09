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
package org.eclipse.osee.orcs.core.internal.attribute;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.Identity;
import org.eclipse.osee.framework.core.data.Named;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.core.ds.ResourceNameResolver;
import org.eclipse.osee.orcs.data.AttributeTypes;

/**
 * @author Roberto E. Escobar
 */
public class AttributeResourceNameResolver implements ResourceNameResolver {
   private final static int MAX_NAME_SIZE = 60;

   private final Attribute<?> attribute;
   private final AttributeTypes attributeTypeCache;

   public AttributeResourceNameResolver(AttributeTypes attributeTypeCache, Attribute<?> attribute) {
      this.attributeTypeCache = attributeTypeCache;
      this.attribute = attribute;
   }

   @Override
   public String getStorageName() throws OseeCoreException {
      Identity<String> identity = attribute.getContainer();
      String guid = identity.getGuid();
      Conditions.checkExpressionFailOnTrue(!GUID.isValid(guid), "Artifact has an invalid guid [%s]", guid);
      return guid;
   }

   @Override
   public String getInternalFileName() throws OseeCoreException {
      Named identity = attribute.getContainer();

      StringBuilder builder = new StringBuilder();
      try {
         String name = identity.getName();
         if (name.length() > MAX_NAME_SIZE) {
            name = name.substring(0, MAX_NAME_SIZE);
         }
         builder.append(URLEncoder.encode(name, "UTF-8"));
         builder.append(".");
      } catch (UnsupportedEncodingException ex) {
         // Do Nothing - this is not important
      }

      builder.append(getStorageName());

      String fileTypeExtension = getExtension(attribute);
      if (Strings.isValid(fileTypeExtension)) {
         builder.append(".");
         builder.append(fileTypeExtension);
      }
      return builder.toString();
   }

   private String getExtension(Attribute<?> attribute) throws OseeCoreException {
      IAttributeType attributeType = attribute.getAttributeType();
      String fileTypeExtension = null;
      if (attribute.isOfType(CoreAttributeTypes.NativeContent)) {
         fileTypeExtension = (String) attribute.getValue();
      }
      if (!Strings.isValid(fileTypeExtension)) {
         fileTypeExtension = attributeTypeCache.getFileTypeExtension(attributeType);
      }
      return fileTypeExtension;
   }
}
