/*********************************************************************
 * Copyright (c) 2012 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.access.provider.internal;

import org.eclipse.osee.framework.core.dsl.ui.integration.operations.AbstractOseeDslProvider;
import org.eclipse.osee.framework.core.enums.CoreArtifactTokens;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author John R. Misinco
 */
public class FrameworkDslProvider extends AbstractOseeDslProvider {

   public FrameworkDslProvider(String locationUri) {
      super(locationUri);
   }

   protected Artifact getStorageArtifact() {
      try {
         return ArtifactQuery.getArtifactFromToken(CoreArtifactTokens.FrameworkAccessModel);
      } catch (OseeCoreException ex) {
         return null;
      }
   }

   @Override
   protected String getModelFromStorage() {
      Artifact storageArtifact = getStorageArtifact();
      if (storageArtifact != null) {
         return storageArtifact.getSoleAttributeValue(CoreAttributeTypes.GeneralStringData);
      } else {
         return Strings.EMPTY_STRING;
      }
   }

   @Override
   protected void saveModelToStorage(String model) {
      Artifact artifact = getStorageArtifact();
      if (artifact != null) {
         artifact.setSoleAttributeFromString(CoreAttributeTypes.GeneralStringData, model);
         artifact.persist(getClass().getSimpleName());
      }
   }

}
