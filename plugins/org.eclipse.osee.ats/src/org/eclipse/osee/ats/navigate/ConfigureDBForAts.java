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
package org.eclipse.osee.ats.navigate;

import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.workflow.item.AtsWorkDefinitions;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinition.WriteType;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class ConfigureDBForAts extends XNavigateItemAction {

   private final String pluginId;

   public ConfigureDBForAts(XNavigateItem parent, String pluginId) {
      super(parent, "Admin - Configure DB For ATS");
      this.pluginId = pluginId;
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) {
      if (!MessageDialog.openConfirm(Displays.getActiveShell(), "Configure DB for ATS",
         "Configure DB for ATS " + pluginId)) {
         return;
      }
      if (!MessageDialog.openConfirm(Displays.getActiveShell(), "Configure DB for ATS",
         "This will break things really bad if ATS is alread configured for this item.  Are you sure?")) {
         return;
      }

      Jobs.startJob(new Report(getName()), true);
   }

   public class Report extends Job {

      public Report(String name) {
         super(name);
      }

      @Override
      protected IStatus run(IProgressMonitor monitor) {
         try {
            if (AtsUtil.dbInitWorkItemDefs()) {
               monitor.subTask("Loading Work Item Definitions for " + pluginId);
               AtsWorkDefinitions.importWorkItemDefinitionsIntoDb(WriteType.New, null,
                  AtsWorkDefinitions.getAtsWorkDefinitions());
               monitor.subTask("Loading Actionable Items and Teams for " + pluginId);
            }
         } catch (Exception ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
            return new Status(IStatus.ERROR, AtsPlugin.PLUGIN_ID, -1, ex.getMessage(), ex);
         }
         monitor.done();
         return Status.OK_STATUS;
      }
   }

}
