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

package org.eclipse.osee.ats.ide.actions;

import java.util.Collection;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.ide.operation.CreateActionFromTaskBlam;
import org.eclipse.osee.ats.ide.workflow.task.TaskArtifact;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.blam.BlamEditor;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class CreateActionFromTaskAction extends Action {

   private final Collection<TaskArtifact> tasks;

   public CreateActionFromTaskAction(Collection<TaskArtifact> tasks) {
      super("Create Action from Task");
      this.tasks = tasks;
   }

   @Override
   public void run() {
      CreateActionFromTaskBlam blamOperation = new CreateActionFromTaskBlam();
      blamOperation.setDefaultTeamWorkflows(tasks);
      BlamEditor.edit(blamOperation);
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.DUPLICATE);
   }

}
