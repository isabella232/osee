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
package org.eclipse.osee.framework.ui.skynet.util.backup;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.io.Zip;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.results.XResultData;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultPage;
import org.eclipse.osee.framework.ui.skynet.util.email.EmailableJob;
import org.eclipse.osee.framework.ui.skynet.widgets.XDate;

/**
 * @author Donald G. Dunne
 */
public class BackupBranchesJob extends EmailableJob {

   public static String JOB_NAME = "Backup OSEE Database";
   XResultData rd;
   private final String path;
   private final Collection<Branch> branches;

   /**
    * @param name
    */
   public BackupBranchesJob(Collection<Branch> branches, String path, XResultData rd) {
      super(JOB_NAME);
      this.branches = branches;
      this.path = path;
      if (rd != null)
         this.rd = rd;
      else
         this.rd = new XResultData();
   }

   @Override
   protected IStatus run(IProgressMonitor monitor) {
      try {
         backup(branches);
         if (rd.toString().equals("")) rd.log("No Problems Found");
         rd.report(getName());
         XResultPage page = rd.getReport(getName());

         notifyOfCompletion(JOB_NAME, page.getManipulatedHtml());
         monitor.done();
         return Status.OK_STATUS;
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         return new Status(Status.ERROR, SkynetGuiPlugin.PLUGIN_ID, -1, "Failed", ex);
      }
   }

   public void backup(Collection<Branch> branches) {
      XResultData rd = new XResultData();
      try {
         rd = new XResultData();
         rd.log(getName());
         rd.log("Starting OSEE DB Backup - " + XDate.getDateNow());
         for (Branch branch : branches) {
            rd.log("Backing up \"" + branch.getShortName() + "\" - " + XDate.getDateNow());
            String backupName = Strings.truncate(branch.getName(), 25);
            backupName = backupName.replaceAll("\\W+", "_");
            File xmlFile =
                  new File(
                        path + "/OSEE_Branch_Backup__" + XDate.getDateNow("yyyy_MM_dd_HH_MM__") + backupName + ".xml");
            if (xmlFile != null) {
               throw new UnsupportedOperationException("Export from client is not supported");
               //               Job job = new ExportBranchJob(xmlFile, branch, false);
               //               job.setUser(true);
               //               job.setPriority(Job.LONG);
               //               job.schedule();
               //               try {
               //                  job.join();
               //               } catch (InterruptedException ex) {
               //                  OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
               //                  rd.logError(ex.getLocalizedMessage());
               //               }
            }
            rd.log("Zipping up \"" + branch.getShortName() + "\" - " + XDate.getDateNow());
            Zip.zip(new String[] {xmlFile.getAbsolutePath()}, xmlFile.getAbsolutePath().replaceFirst(".xml", ".zip"));
            // Delete backup file
            xmlFile.delete();
            rd.log("Finished with \"" + branch.getShortName() + "\" - " + XDate.getDateNow());
         }
         rd.log("Completed - " + XDate.getDateNow());
         rd.report(getName());

      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         rd.logError(ex.getLocalizedMessage());
      }

   }
}
