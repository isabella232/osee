/*********************************************************************
 * Copyright (c) 2016 Boeing
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

package org.eclipse.osee.ats.ide.ev;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.ide.AtsImage;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.artifact.massEditor.MassArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class OpenWorkPackageByIdAction extends Action {

   public OpenWorkPackageByIdAction() {
      this("Open Work Package by ID(s)");
   }

   public OpenWorkPackageByIdAction(String name) {
      super(name);
      setToolTipText(getText());
   }

   @Override
   public void run() {
      EntryDialog dialog = new EntryDialog(getText(), "Enter Work Package id, activity id or financial id");
      if (dialog.open() == Window.OK) {
         final List<String> ids = new LinkedList<>();
         for (String str : dialog.getEntry().split(",")) {
            str = str.replaceAll("^\\s+", "");
            str = str.replaceAll("\\s+$", "");
            if (Strings.isNumeric(str)) {
               ids.add(str);
            }
            Job searchWps = new Job(getText()) {

               @Override
               protected IStatus run(IProgressMonitor monitor) {
                  List<Artifact> results = new LinkedList<>();
                  if (!ids.isEmpty()) {
                     results.addAll(ArtifactQuery.getArtifactListFromAttributeValues(AtsAttributeTypes.ActivityId, ids,
                        AtsApiService.get().getAtsBranch(), 5));
                     results.addAll(ArtifactQuery.getArtifactListFromAttributeValues(AtsAttributeTypes.CognosUniqueId,
                        ids, AtsApiService.get().getAtsBranch(), 5));
                  }
                  if (results.isEmpty()) {
                     AWorkbench.popup("No Work Packages found with id(s): " + dialog.getEntry());
                  } else {
                     MassArtifactEditor.editArtifacts(getName(), results);
                  }
                  return Status.OK_STATUS;
               }
            };
            Jobs.startJob(searchWps, true);
         }
      }
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(AtsImage.WORK_PACKAGE);
   }

}
