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

package org.eclipse.osee.ats.actions.wizard;

import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.ats.AtsOpenOption;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.team.ChangeType;
import org.eclipse.osee.ats.api.workflow.INewActionListener;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.util.AtsChangeSet;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class NewActionJob extends Job {
   private String title;
   private final String desc;
   private final ChangeType changeType;
   private final String priority;
   private final Date needByDate;
   private final boolean validationRequired;
   private Artifact actionArt;
   private final Set<IAtsActionableItem> actionableItems;
   private final NewActionWizard wizard;
   private final INewActionListener newActionListener;
   private boolean openOnComplete = true;

   public NewActionJob(String title, String desc, ChangeType changeType, String priority, Date needByDate, boolean validationRequired, Set<IAtsActionableItem> actionableItems, NewActionWizard wizard, INewActionListener newActionListener) {
      super("Creating New Action");
      this.title = title;
      this.desc = desc;
      this.changeType = changeType;
      this.priority = priority;
      this.needByDate = needByDate;
      this.validationRequired = validationRequired;
      this.actionableItems = actionableItems;
      this.wizard = wizard;
      this.newActionListener = newActionListener;
   }

   @Override
   public IStatus run(final IProgressMonitor monitor) {
      try {
         if (actionableItems.isEmpty()) {
            throw new OseeArgumentException("Actionable Items can not be empty for New Action");
         }
         if ("tt".equals(title)) {
            title += " " + AtsUtil.getAtsDeveloperIncrementingNum();
         }
         AtsChangeSet changes = new AtsChangeSet("Create New Action");
         actionArt = ActionManager.createAction(monitor, title, desc, changeType, priority, validationRequired,
            needByDate, actionableItems, new Date(), AtsClientService.get().getUserService().getCurrentUser(),
            newActionListener, changes);

         if (wizard != null) {
            wizard.notifyAtsWizardItemExtensions(actionArt, changes);
         }

         if (monitor != null) {
            monitor.subTask("Persisting");
         }
         changes.execute();

         if (openOnComplete) {
            // Because this is a job, it will automatically kill any popups that are created during.
            // Thus, if multiple teams were selected to create, don't popup on openAction or dialog
            // will exception out when it is killed at the end of this job.
            AtsUtil.openATSAction(actionArt, AtsOpenOption.OpenAll);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, ex.getMessage(), ex);
      } finally {
         if (monitor != null) {
            monitor.done();
         }
      }
      return Status.OK_STATUS;
   }

   public Artifact getActionArt() {
      return actionArt;
   }

   public boolean isOpenOnComplete() {
      return openOnComplete;
   }

   public void setOpenOnComplete(boolean openOnComplete) {
      this.openOnComplete = openOnComplete;
   }

}
