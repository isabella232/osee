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
package org.eclipse.osee.ats.actions;

import java.util.Collection;
import org.eclipse.jface.action.IAction;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.osee.ats.core.client.actions.ISelectedAtsArtifacts;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.editor.SMAPromptChangeStatus;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class EditStatusAction extends AbstractAtsAction {

   private final ISelectedAtsArtifacts selectedAtsArtifacts;
   private final XViewer xViewer;
   private final IDirtiableEditor editor;

   public EditStatusAction(ISelectedAtsArtifacts selectedAtsArtifacts, IDirtiableEditor editor, XViewer xViewer) {
      super("Edit Status", IAction.AS_PUSH_BUTTON);
      this.selectedAtsArtifacts = selectedAtsArtifacts;
      this.editor = editor;
      this.xViewer = xViewer;
      setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.EDIT));
   }

   @Override
   public void runWithException() throws OseeCoreException {
      Collection<AbstractWorkflowArtifact> smaArts =
         Collections.castMatching(AbstractWorkflowArtifact.class, selectedAtsArtifacts.getSelectedSMAArtifacts());
      if (SMAPromptChangeStatus.promptChangeStatus(smaArts, true)) {
         if (xViewer != null) {
            xViewer.update(selectedAtsArtifacts.getSelectedSMAArtifacts().toArray(), null);
         }
         if (editor != null) {
            editor.onDirtied();
         }
      }

   }

}
