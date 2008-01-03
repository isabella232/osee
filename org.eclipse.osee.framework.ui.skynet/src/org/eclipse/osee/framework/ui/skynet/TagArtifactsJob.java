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

package org.eclipse.osee.framework.ui.skynet;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.tagging.TagManager;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.menu.GlobalMenu;
import org.eclipse.osee.framework.ui.skynet.menu.GlobalMenuListener;
import org.eclipse.osee.framework.ui.skynet.menu.GlobalMenu.GlobalMenuItem;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;

/**
 * @author Donald G. Dunne
 */
public class TagArtifactsJob extends Job {

   private static final TagManager tagManager = TagManager.getInstance();
   private Collection<Artifact> artifacts;
   private final GlobalMenu globalMenu;

   public TagArtifactsJob(Collection<Artifact> artifacts, GlobalMenu globalMenu) {
      super("Tag Artifacts");
      this.artifacts = artifacts;
      this.globalMenu = globalMenu;
   }

   public TagArtifactsJob(Collection<Artifact> artifacts) {
      this(artifacts, null);
   }

   @Override
   protected IStatus run(IProgressMonitor monitor) {
      monitor.beginTask("Tag Artifacts", artifacts.size());
      if (globalMenu != null) {
         try {
            for (GlobalMenuListener listener : globalMenu.getGlobalMenuListeners()) {
               Result result = listener.actioning(GlobalMenuItem.TagArtifacts, artifacts);
               if (result.isFalse()) {
                  result.popup();
                  return new Status(Status.ERROR, SkynetActivator.PLUGIN_ID, Status.OK, result.getText(), null);
               }
            }
         } catch (Exception ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, false);
         }
      }
      for (Artifact artifact : artifacts) {
         monitor.subTask(artifact.getDescriptiveName());
         try {
            tagManager.autoTag(true, artifact);
         } catch (Exception ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, false);
         }
         monitor.worked(1);

         if (monitor.isCanceled()) {
            monitor.done();
            return Status.CANCEL_STATUS;
         }
      }
      if (globalMenu != null) {
         try {
            for (GlobalMenuListener listener : globalMenu.getGlobalMenuListeners()) {
               listener.actioned(GlobalMenuItem.TagArtifacts, artifacts);
            }
         } catch (Exception ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, false);
         }
      }

      monitor.done();

      return Status.OK_STATUS;
   }

}
