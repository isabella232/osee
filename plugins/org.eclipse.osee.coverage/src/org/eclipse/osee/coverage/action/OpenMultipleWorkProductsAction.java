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
package org.eclipse.osee.coverage.action;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.coverage.editor.CoverageEditor;
import org.eclipse.osee.coverage.internal.ServiceProvider;
import org.eclipse.osee.coverage.model.IWorkProductTaskProvider;
import org.eclipse.osee.coverage.model.WorkProductAction;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.cm.IOseeCmService;
import org.eclipse.osee.framework.ui.skynet.cm.OseeCmEditor;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

/**
 * @author Donald G. Dunne
 */
public class OpenMultipleWorkProductsAction extends Action {

   private KeyedImage image = null;
   private final IWorkProductTaskProvider provider;
   private final CoverageEditor coverageEditor;

   public OpenMultipleWorkProductsAction(CoverageEditor coverageEditor, IWorkProductTaskProvider provider) {
      super("Open Work Products");
      this.coverageEditor = coverageEditor;
      this.provider = provider;
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      if (image == null) {
         IOseeCmService service = ServiceProvider.getOseeCmService();
         if (service != null) {
            image = service.getOpenImage(OseeCmEditor.CmMultiPcrEditor);
         }
      }
      if (image == null) {
         return ImageManager.getImageDescriptor(FrameworkImage.REPORT);
      }
      return ImageManager.getImageDescriptor(image);
   }

   @Override
   public void run() {
      if (provider.getWorkProductRelatedActions().isEmpty()) {
         AWorkbench.popup("No Work Products to open");
         return;
      }
      Job job = new Job(getText()) {

         @Override
         protected IStatus run(IProgressMonitor monitor) {
            IOseeCmService service = ServiceProvider.getOseeCmService();
            List<String> guids = new ArrayList<String>();
            for (WorkProductAction action : provider.getWorkProductRelatedActions()) {
               guids.add(action.getGuid());
            }
            service.openArtifactsByGuid(coverageEditor.getTitle() + " - Work Products", guids,
               OseeCmEditor.CmMultiPcrEditor);
            return Status.OK_STATUS;
         }
      };
      Jobs.startJob(job);
   }
}
