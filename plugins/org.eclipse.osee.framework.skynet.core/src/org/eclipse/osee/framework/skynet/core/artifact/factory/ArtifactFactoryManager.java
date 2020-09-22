/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.skynet.core.artifact.factory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionDefinedObjects;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Ryan D. Brooks
 * @author Donald G. Dunne
 */
public final class ArtifactFactoryManager {
   private static final String ARTIFACT_FACTORY_EXTENSION = "ArtifactFactory";
   private static final String EXTENSION_ID = Activator.PLUGIN_ID + "." + ARTIFACT_FACTORY_EXTENSION;
   private static final String CLASSNAME_ATTRIBUTE = "classname";

   private static final ExtensionDefinedObjects<ArtifactFactory> extensionDefinedObjects =
      new ExtensionDefinedObjects<>(EXTENSION_ID, ARTIFACT_FACTORY_EXTENSION, CLASSNAME_ATTRIBUTE);

   private static final DefaultArtifactFactory defaultArtifactFactory = new DefaultArtifactFactory();
   private static Set<ArtifactTypeId> eternalArtifactTypes = null;

   public ArtifactFactory getFactory(ArtifactTypeToken artifactType) {
      Exception savedEx = null;
      ArtifactFactory responsibleFactory = null;
      for (ArtifactFactory factory : getFactories()) {
         try {
            if (factory.isResponsibleFor(artifactType)) {
               if (responsibleFactory == null) {
                  responsibleFactory = factory;
               } else {
                  OseeLog.logf(Activator.class, Level.SEVERE,
                     "Multiple ArtifactFactories [%s] [%s]responsible for same artifact type [%s].  Defaulting to DefaultArtifactFactory.",
                     responsibleFactory, factory, artifactType);
                  return getDefaultArtifactFactory();
               }
            }
         } catch (Exception ex) {
            // do not stop artifact loading for a single failed factory that may not be the needed one
            savedEx = ex;
         }
      }
      if (responsibleFactory != null) {
         return responsibleFactory;
      }
      if (savedEx != null) {
         // if we didn't find a isResponsible factory and had an exception, then log it
         OseeLog.log(getClass(), Level.SEVERE, savedEx);
      }
      return getDefaultArtifactFactory();
   }

   public static synchronized Collection<ArtifactTypeId> getEternalArtifactTypes() {
      if (eternalArtifactTypes == null) {
         eternalArtifactTypes = new HashSet<>();
         for (ArtifactFactory factory : getFactories()) {
            eternalArtifactTypes.addAll(factory.getEternalArtifactTypes());
         }
      }
      return eternalArtifactTypes;
   }

   private ArtifactFactory getDefaultArtifactFactory() {
      return defaultArtifactFactory;
   }

   private static synchronized List<ArtifactFactory> getFactories() {
      return extensionDefinedObjects.getObjects();
   }
}