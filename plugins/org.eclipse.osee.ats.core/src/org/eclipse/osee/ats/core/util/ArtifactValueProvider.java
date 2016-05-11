/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.util.IValueProvider;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class ArtifactValueProvider implements IValueProvider {

   private final ArtifactId artifact;
   private final String attributeTypeName;
   private final IAtsServices services;

   public ArtifactValueProvider(ArtifactId artifact, IAtsWidgetDefinition widgetDef, IAtsServices services) {
      this.artifact = artifact;
      this.services = services;
      this.attributeTypeName = widgetDef.getAtrributeName();
   }

   public ArtifactValueProvider(ArtifactId artifact, IAttributeType attributeType, IAtsServices services) {
      this.artifact = artifact;
      this.services = services;
      this.attributeTypeName = attributeType.getName();
   }

   @Override
   public boolean isEmpty() throws OseeCoreException {
      IAttributeType attributeType = getAtributeType();
      if (attributeType != null) {
         return services.getAttributeResolver().getAttributeCount(artifact, attributeType) == 0;
      }
      return true;
   }

   @Override
   public Collection<String> getValues() throws OseeCoreException {
      IAttributeType attributeType = getAtributeType();
      if (attributeType != null) {
         return services.getAttributeResolver().getAttributesToStringList(artifact, attributeType);
      }
      return Collections.emptyList();
   }

   public IAttributeType getAtributeType() throws OseeCoreException {
      if (Strings.isValid(attributeTypeName)) {
         IAttributeType attrType = services.getStoreService().getAttributeType(attributeTypeName);
         return attrType;
      }
      return null;
   }

   @Override
   public String getName() {
      return artifact.getName();
   }

   @Override
   public Collection<Date> getDateValues() throws OseeCoreException {
      IAttributeType attributeType = getAtributeType();
      if (attributeType != null && services.getStoreService().isDateType(attributeType)) {
         List<Date> dates = new ArrayList<>();
         for (String attr : services.getAttributeResolver().getAttributesToStringList(artifact, attributeType)) {
            dates.add(new Date(Long.valueOf(attr)));
         }
         return dates;
      }
      return Collections.emptyList();

   }

   public ArtifactId getArtifact() {
      return artifact;
   }

   public Object getObject() {
      return artifact;
   }

}
