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

import org.eclipse.osee.ats.ide.util.Import.ImportTasksFromSimpleList;
import org.eclipse.osee.ats.ide.workflow.teamwf.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.blam.BlamEditor;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class ImportTasksViaSimpleList extends AbstractAtsAction {

   private final TeamWorkFlowArtifact taskableArt;
   private final ImportListener listener;

   public ImportTasksViaSimpleList(TeamWorkFlowArtifact taskableArt, ImportListener listener) {
      super();
      this.taskableArt = taskableArt;
      this.listener = listener;
      setText("Import Tasks via simple list");
      setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.IMPORT));
   }

   @Override
   public void runWithException() {
      ImportTasksFromSimpleList blamOperation = new ImportTasksFromSimpleList();
      blamOperation.setTaskableStateMachineArtifact(taskableArt);
      BlamEditor.edit(blamOperation);
      if (listener != null) {
         listener.importCompleted(new XResultData());
      }
   }
}
