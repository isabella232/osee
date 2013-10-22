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
package org.eclipse.osee.coverage.editor;

import static org.eclipse.osee.coverage.store.CoverageArtifactTypes.CoverageFolder;
import static org.eclipse.osee.coverage.store.CoverageArtifactTypes.CoveragePackage;
import static org.eclipse.osee.coverage.store.CoverageArtifactTypes.CoverageUnit;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERALIZED_EDIT;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.PRODUCE_ATTRIBUTE;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.coverage.store.CoverageArtifactTypes;
import org.eclipse.osee.coverage.util.CoverageImage;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.ui.skynet.render.DefaultArtifactRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Ryan D. Brooks
 */
public class CoverageRenderer extends DefaultArtifactRenderer {
   private static final String COMMAND_ID = "org.eclipse.osee.coverage.editor.command";

   @Override
   public List<String> getCommandIds(CommandGroup commandGroup) {
      ArrayList<String> commandIds = new ArrayList<String>(1);

      if (commandGroup.isEdit()) {
         commandIds.add(COMMAND_ID);
      }

      return commandIds;
   }

   @Override
   public ImageDescriptor getCommandImageDescriptor(Command command, Artifact artifact) {
      return ImageManager.getImageDescriptor(CoverageImage.COVERAGE_PACKAGE);
   }

   @Override
   public String getName() {
      return "Coverage Editor";
   }

   public CoverageRenderer() {
      super();
   }

   private void recurseAndOpenCoveragePackage(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(CoverageArtifactTypes.CoveragePackage)) {
         CoverageEditor.open(new CoverageEditorInput(artifact.getName(), artifact, null, false));
      } else {
         if (artifact.getParent() != null) {
            recurseAndOpenCoveragePackage(artifact.getParent());
         }
      }
   }

   @Override
   public CoverageRenderer newInstance() {
      return new CoverageRenderer();
   }

   @Override
   public int getApplicabilityRating(PresentationType presentationType, IArtifact artifact) throws OseeCoreException {
      Artifact aArtifact = artifact.getFullArtifact();
      if (!presentationType.matches(GENERALIZED_EDIT, PRODUCE_ATTRIBUTE) && !aArtifact.isHistorical()) {
         if (aArtifact.isOfType(CoveragePackage, CoverageFolder, CoverageUnit)) {
            return PRESENTATION_SUBTYPE_MATCH;
         }
      }

      return NO_MATCH;
   }

   @Override
   public int minimumRanking() throws OseeCoreException {
      if (AccessControlManager.isOseeAdmin()) {
         return NO_MATCH;
      } else {
         return PRESENTATION_TYPE;
      }
   }

   @Override
   public void open(List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException {
      for (Artifact artifact : artifacts) {
         recurseAndOpenCoveragePackage(artifact);
      }
   }
}