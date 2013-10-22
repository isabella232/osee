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
package org.eclipse.osee.ats.notify;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.core.client.notify.IAtsNotification;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.OseeNotificationEvent;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.notify.OseeNotificationManager;
import org.eclipse.osee.framework.ui.skynet.notify.OseeNotifyUsersJob;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;

/**
 * @author Donald G. Dunne
 */
public class AtsNotificationNavigateItem extends XNavigateItemAction {

   public AtsNotificationNavigateItem(XNavigateItem parent) {
      this(parent, false);
   }

   public AtsNotificationNavigateItem(XNavigateItem parent, boolean sync) {
      super(parent, (sync ? "Sync - " : "") + "Process ATS Notifications", PluginUiImage.ADMIN);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws Exception {

      AtsNotificationCheckTreeDialog diag = new AtsNotificationCheckTreeDialog();
      if (diag.open() == 0) {
         if (diag.getSelectedAtsNotifications().isEmpty()) {
            AWorkbench.popup("Error", "No Notifications Selected");
            return;
         }
         Operations.executeAsJob(new NotificationJob(diag.isSendNotifications(), diag.getSelectedAtsNotifications()),
            true);
      }
   }

   private static class NotificationJob extends AbstractOperation {

      private final boolean sendNotifications;
      private final Collection<IAtsNotification> notifications;

      public NotificationJob(boolean sendNotifications, Collection<IAtsNotification> notifications) {
         super("Processing ATS Notifications", Activator.PLUGIN_ID);
         this.sendNotifications = sendNotifications;
         this.notifications = notifications;
      }

      @Override
      protected void doWork(IProgressMonitor monitor) throws Exception {
         try {
            XResultData rd = new XResultData();
            if (sendNotifications) {
               rd.addRaw(AHTML.bold("Notifications were sent"));
            } else {
               rd.addRaw("Report Only - Notifications were NOT sent");
            }
            rd.addRaw(AHTML.beginMultiColumnTable(100, 1));
            rd.addRaw(AHTML.addHeaderRowMultiColumnTable(new String[] {"Reason", "Description", "Id", "User(s)", "URL"}));
            int numEvents = 0;
            for (IAtsNotification notify : notifications) {
               for (OseeNotificationEvent event : notify.getNotificationEvents(monitor)) {
                  numEvents++;
                  rd.addRaw(AHTML.addRowMultiColumnTable(event.getType(), event.getDescription(),
                     XResultDataUI.getHyperlink(event.getId(), event.getId(), AtsUtil.getAtsBranch().getId()),
                     AtsObjects.toString("; ", event.getUsers()), OseeNotifyUsersJob.getHyperlink(event)));
                  if (sendNotifications) {
                     OseeNotificationManager.getInstance().addNotificationEvent(event);
                  }
               }
            }
            rd.addRaw(AHTML.endMultiColumnTable());
            XResultDataUI.report(rd, getName() + " - (" + numEvents + " Events)");
            if (sendNotifications) {
               OseeNotificationManager.getInstance().sendNotifications();
               AWorkbench.popup("Complete", numEvents + " Notifications Sent");
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
   };
}
