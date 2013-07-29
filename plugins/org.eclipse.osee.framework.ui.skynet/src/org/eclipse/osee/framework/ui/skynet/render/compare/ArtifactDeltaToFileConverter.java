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
package org.eclipse.osee.framework.ui.skynet.render.compare;

import static org.eclipse.osee.framework.ui.skynet.render.ITemplateRenderer.TEMPLATE_OPTION;
import java.util.Collection;
import org.eclipse.core.resources.IFile;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.plugin.core.util.AIFile;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.change.ArtifactDelta;
import org.eclipse.osee.framework.ui.skynet.render.FileSystemRenderer;
import org.eclipse.osee.framework.ui.skynet.render.IRenderer;
import org.eclipse.osee.framework.ui.skynet.render.ITemplateRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;

public class ArtifactDeltaToFileConverter {
   private final FileSystemRenderer renderer;

   public ArtifactDeltaToFileConverter(FileSystemRenderer renderer) {
      this.renderer = renderer;
   }

   public FileSystemRenderer getRenderer() {
      return renderer;
   }

   public Pair<IFile, IFile> convertToFile(PresentationType presentationType, ArtifactDelta artifactDelta) throws OseeCoreException {
      Artifact baseArtifact = artifactDelta.getStartArtifact();
      Artifact newerArtifact = artifactDelta.getEndArtifact();
      if (newerArtifact.getModType().isDeleted()) {
         newerArtifact = null;
      }
      IOseeBranch branch = artifactDelta.getBranch();

      IFile baseFile = renderer.renderToFile(baseArtifact, branch, presentationType);
      IFile newerFile = renderer.renderToFile(newerArtifact, branch, presentationType);
      return new Pair<IFile, IFile>(baseFile, newerFile);
   }

   public void convertToFileForMerge(final Collection<IFile> outputFiles, Artifact baseVersion, Artifact newerVersion) throws OseeCoreException {
      ArtifactDelta artifactDelta = new ArtifactDelta(baseVersion, newerVersion);

      CompareDataCollector colletor = new CompareDataCollector() {
         @Override
         public void onCompare(CompareData data) {
            outputFiles.add(AIFile.constructIFile(data.getOutputPath()));
         }
      };
      // Set ADD MERGE TAG as an option so resulting document will indicate a merge section
      RendererManager.diff(colletor, artifactDelta, "", IRenderer.NO_DISPLAY, true, TEMPLATE_OPTION,
         ITemplateRenderer.DIFF_NO_ATTRIBUTES_VALUE, ITemplateRenderer.ADD_MERGE_TAG, true);
   }
}