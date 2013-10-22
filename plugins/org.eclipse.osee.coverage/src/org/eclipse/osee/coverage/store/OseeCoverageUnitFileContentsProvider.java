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
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverageUnitFileContentsProvider;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class OseeCoverageUnitFileContentsProvider implements ICoverageUnitFileContentsProvider {

   private static OseeCoverageUnitFileContentsProvider instance;
   private final IOseeBranch branch;

   private OseeCoverageUnitFileContentsProvider(IOseeBranch branch) {
      this.branch = branch;
   }

   public static OseeCoverageUnitFileContentsProvider getInstance(IOseeBranch branch) {
      if (instance == null || !instance.getBranch().equals(branch)) {
         instance = new OseeCoverageUnitFileContentsProvider(branch);
      }
      return instance;
   }

   @Override
   public String getFileContents(CoverageUnit coverageUnit) {
      try {
         OseeCoverageUnitStore store = new OseeCoverageUnitStore(coverageUnit, branch);
         Artifact artifact = store.getArtifact(false);
         if (artifact != null) {
            return artifact.getSoleAttributeValue(CoverageAttributeTypes.FileContents, "");
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return "";
   }

   @Override
   public void setFileContents(CoverageUnit coverageUnit, String fileContents) {
      try {
         OseeCoverageUnitStore store = new OseeCoverageUnitStore(coverageUnit, branch);
         Artifact artifact = store.getArtifact(false);
         if (artifact != null) {
            artifact.setSoleAttributeValue(CoverageAttributeTypes.FileContents, fileContents);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public IOseeBranch getBranch() {
      return branch;
   }
}