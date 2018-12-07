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
package org.eclipse.osee.disposition.rest.internal;

import java.util.List;
import org.eclipse.osee.disposition.model.DispoConfig;
import org.eclipse.osee.disposition.model.DispoConfigData;
import org.eclipse.osee.disposition.model.MultiEnvTarget;
import org.eclipse.osee.disposition.model.ResolutionMethod;
import org.eclipse.osee.disposition.rest.util.DispoUtil;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.util.JsonUtil;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Angel Avila
 */
public class DispoConfigArtifact implements DispoConfig {

   private final ArtifactReadable artifact;

   public DispoConfigArtifact(ArtifactReadable artifact) {
      this.artifact = artifact;
   }

   @Override
   public List<ResolutionMethod> getValidResolutions() {
      List<String> attributes = artifact.getAttributeValues(CoreAttributeTypes.GeneralStringData);
      String resolutions = "";
      for (String attribute : attributes) {
         if (attribute.startsWith("{")) {
            return JsonUtil.readValue(attribute, DispoConfigData.class).getValidResolutions();
         } else if (attribute.startsWith("RESOLUTION_METHODS")) {
            resolutions = attribute.replaceFirst("RESOLUTION_METHODS=", "");
         }
      }
      return DispoUtil.jsonStringToList(resolutions, ResolutionMethod.class);
   }

   @Override
   public List<MultiEnvTarget> getMultiEnvTargets() {
      List<String> attributes = artifact.getAttributeValues(CoreAttributeTypes.GeneralStringData);
      String resolutionsJson = "";
      for (String attribute : attributes) {
         if (attribute.startsWith("{")) {
            return JsonUtil.readValue(attribute, DispoConfigData.class).getMultiEnvTargets();
         } else if (attribute.startsWith("RESOLUTION_METHODS")) {
            resolutionsJson = "[]";
         }
      }
      return DispoUtil.jsonStringToList(resolutionsJson, MultiEnvTarget.class);
   }
}
