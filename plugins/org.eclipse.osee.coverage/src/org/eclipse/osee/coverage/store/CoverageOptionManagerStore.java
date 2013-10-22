/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.coverage.store;

import java.util.logging.Level;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageOptionManager;
import org.eclipse.osee.coverage.model.CoverageOptionManagerDefault;
import org.eclipse.osee.coverage.model.CoveragePreferences;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.MultipleAttributesExist;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class CoverageOptionManagerStore {

   private final OseeCoveragePackageStore oseeCoveragePackageStore;
   public static enum StoreLocation {
      Local,
      Global,
      None
   };

   public CoverageOptionManagerStore(OseeCoveragePackageStore oseeCoveragePackageStore) {
      this.oseeCoveragePackageStore = oseeCoveragePackageStore;
   }

   public String getCoverageOptions() throws MultipleAttributesExist, OseeCoreException {
      String coverageOptions =
         oseeCoveragePackageStore.getArtifact(true).getSoleAttributeValueAsString(CoverageAttributeTypes.Options, null);
      if (!Strings.isValid(coverageOptions)) {
         coverageOptions =
            new CoveragePreferences(oseeCoveragePackageStore.getArtifact(true).getBranch()).getCoverageOptions();
      }
      return coverageOptions;
   }

   private String getLocalCoverageOptions() throws OseeCoreException {
      return oseeCoveragePackageStore.getArtifact(true).getSoleAttributeValueAsString(CoverageAttributeTypes.Options,
         null);
   }

   private String getGlobalCoverageOptions() throws OseeCoreException {
      return new CoveragePreferences(oseeCoveragePackageStore.getArtifact(true).getBranch()).getCoverageOptions();
   }

   public Result isSaveable() throws OseeCoreException {
      StoreLocation location = getStoreLocation();
      if (location == StoreLocation.None) {
         return Result.TrueResult;
      } else if (location == StoreLocation.Local) {
         if (!AccessControlManager.hasPermission(oseeCoveragePackageStore.getArtifact(true), PermissionEnum.WRITE)) {
            return new Result(String.format("You do not have permissions to change coverage options on [%s]",
               oseeCoveragePackageStore.getArtifact(true)));
         }
         return Result.TrueResult;
      } else if (location == StoreLocation.Global) {
         CoveragePreferences prefs = new CoveragePreferences(oseeCoveragePackageStore.getArtifact(true).getBranch());
         if (prefs.isSaveable().isFalse()) {
            return new Result(String.format("You do not have permissions to change coverage options on [%s]",
               oseeCoveragePackageStore.getArtifact(true)));
         }
         return Result.TrueResult;
      } else {
         OseeLog.log(Activator.class, Level.SEVERE, "Unexpected StoreLocation " + location);
         return new Result("Unexpected StoreLocation [" + location + "] write permission denied.");
      }

   }

   public StoreLocation getStoreLocation() throws OseeCoreException {
      if (Strings.isValid(getLocalCoverageOptions())) {
         return StoreLocation.Local;
      }
      if (Strings.isValid(getGlobalCoverageOptions())) {
         return StoreLocation.Global;
      }
      return StoreLocation.None;
   }

   public CoverageOptionManager getCoverageOptionManager() throws OseeCoreException {
      String coverageOptions = getCoverageOptions();
      if (!Strings.isValid(coverageOptions)) {
         coverageOptions = CoverageOptionManagerDefault.instance().toXml();
      }
      return new CoverageOptionManager(coverageOptions);
   }

   public void store(CoverageOptionManager coverageOptionManager, StoreLocation storeLocation) throws OseeCoreException {
      if (storeLocation == StoreLocation.None) {
         throw new OseeArgumentException("No Valid Store Location selected for Coverage Options");
      }
      store(coverageOptionManager.toXml(), storeLocation);
   }

   public void store(String coverageOptions, StoreLocation storeLocation) throws OseeCoreException {
      if (storeLocation == StoreLocation.Local) {
         oseeCoveragePackageStore.getArtifact(true).setSoleAttributeFromString(CoverageAttributeTypes.Options,
            coverageOptions);
         oseeCoveragePackageStore.getArtifact(true).persist(getClass().getSimpleName());
      } else if (storeLocation == StoreLocation.Global) {
         new CoveragePreferences(oseeCoveragePackageStore.getArtifact(true).getBranch()).setCoverageOptions(coverageOptions);
      } else {
         throw new OseeArgumentException("No Valid Store Location selected for Coverage Options");
      }

   }
}
