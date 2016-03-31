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
package org.eclipse.osee.ats.task;

import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERALIZED_EDIT;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.PRODUCE_ATTRIBUTE;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.SPECIALIZED_EDIT;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.editor.renderer.AbstractAtsRenderer;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.ui.skynet.MenuCmdDef;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;

/**
 * @author Jeff C. Phillips
 */
public class AtsTaskEditorRenderer extends AbstractAtsRenderer {

   private static final String Option_TASK_WORLD_EDITOR = "task.world.editor.option";

   @Override
   public String getName() {
      return "ATS Task Editor";
   }

   @Override
   public int getApplicabilityRating(PresentationType presentationType, IArtifact artifact, Object... objects) throws OseeCoreException {
      Artifact aArtifact = artifact.getFullArtifact();

      VariableMap options = new VariableMap();
      options.setValues(objects);

      if (aArtifact.isOfType(AtsArtifactTypes.Task) && !aArtifact.isHistorical() && !presentationType.matches(
         GENERALIZED_EDIT, PRODUCE_ATTRIBUTE)) {
         if (Option_TASK_WORLD_EDITOR.equals(options.getString(OPEN_OPTION))) {
            return SPECIALIZED_KEY_MATCH;
         } else {
            return PRESENTATION_SUBTYPE_MATCH;
         }
      }
      return NO_MATCH;
   }

   @Override
   public void addMenuCommandDefinitions(ArrayList<MenuCmdDef> commands, Artifact artifact) {
      commands.add(new MenuCmdDef(CommandGroup.EDIT, SPECIALIZED_EDIT, "ATS Task Editor", AtsImage.TASK, OPEN_OPTION,
         Option_TASK_WORLD_EDITOR));
   }

   @Override
   public AtsTaskEditorRenderer newInstance() {
      return new AtsTaskEditorRenderer();
   }

   @Override
   public void open(List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException {
      AtsUtil.openInAtsTaskEditor("Tasks", artifacts);
   }
}
