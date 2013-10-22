/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.coverage.store;

import org.eclipse.osee.coverage.util.CoverageImage;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageProvider;

/**
 * @author Ryan D. Brooks
 */
public class CoverageArtifactImageProvider extends ArtifactImageProvider {

   @Override
   public void init() throws OseeCoreException {
      ArtifactImageManager.registerBaseImage(CoverageArtifactTypes.CoveragePackage, CoverageImage.COVERAGE_PACKAGE,
         this);
      ArtifactImageManager.registerBaseImage(CoverageArtifactTypes.CoverageUnit, CoverageImage.COVERAGE, this);
      ArtifactImageManager.registerBaseImage(CoverageArtifactTypes.CoverageFolder, PluginUiImage.FOLDER, this);
   }

}