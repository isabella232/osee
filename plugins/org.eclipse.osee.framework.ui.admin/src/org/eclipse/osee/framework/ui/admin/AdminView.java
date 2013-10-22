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
package org.eclipse.osee.framework.ui.admin;

import java.util.ArrayList;
import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.BroadcastEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BroadcastEventType;
import org.eclipse.osee.framework.ui.admin.dbtabletab.DbItem;
import org.eclipse.osee.framework.ui.admin.dbtabletab.DbTableTab;
import org.eclipse.osee.framework.ui.admin.dbtabletab.SiteGssflRpcr;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericViewPart;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

/**
 * Allows administration of access for OSEE environment <li>Database tables <li>OSEE user permissions
 * 
 * @author Jeff C. Phillips
 */

public class AdminView extends GenericViewPart {
   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.admin.AdminView";
   private static Action saveAction;
   private TabFolder tabFolder;
   private final ArrayList<DbItem> dbItems;
   private final Cursor handCursor;
   private Composite parentComp;

   public AdminView() {
      dbItems = new ArrayList<DbItem>();
      dbItems.add(new SiteGssflRpcr());
      handCursor = new Cursor(null, SWT.CURSOR_HAND);
   }

   @Override
   public void dispose() {
      super.dispose();
      handCursor.dispose();
   }

   protected void createActions() throws OseeCoreException {

      saveAction = new Action("Save") {
         @Override
         public void run() {
            save();
         }
      };
      saveAction.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.SAVED));
      saveAction.setToolTipText("Save");

      Action refreshAction = new Action("Refresh") {

         @Override
         public void run() {
            try {
               DbTableTab.refresh();
            } catch (OseeCoreException ex) {
               OseeLog.log(AdminView.class, Level.SEVERE, ex);
            }
         }
      };
      refreshAction.setImageDescriptor(ImageManager.getImageDescriptor(PluginUiImage.REFRESH));
      refreshAction.setToolTipText("Refresh");

      Action broadcastMessage = new Action("Broadcast Message") {

         @Override
         public void run() {
            handleBroadcastMessage();
         }
      };

      broadcastMessage.setToolTipText("Broadcast Message");
      broadcastMessage.setEnabled(AccessControlManager.isOseeAdmin());

      Action pingAction = new Action("Ping OSEE Clients") {

         @Override
         public void run() {
            handlePing();
         }
      };

      pingAction.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.HELP));
      pingAction.setToolTipText("Ping OSEE Clients");
      pingAction.setEnabled(AccessControlManager.isOseeAdmin());

      IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
      toolbarManager.add(saveAction);
      toolbarManager.add(refreshAction);
      toolbarManager.add(broadcastMessage);
      if (AccessControlManager.isOseeAdmin()) {
         toolbarManager.add(pingAction);
      }
      setFocusWidget(parentComp);
   }

   public void handleBroadcastMessage() {
      EntryDialog ed =
         new EntryDialog(Displays.getActiveShell(), "Broadcast Message to OSEE Instantiations", null, "Enter Message",
            MessageDialog.QUESTION, new String[] {"OK", "Cancel"}, 0);
      if (ed.open() == 0) {
         String message = ed.getEntry();
         if (!message.equals("")) {
            if (MessageDialog.openConfirm(Displays.getActiveShell(), "Broadcast Message",
               "Broadcast message\n\n\"" + message + "\"\n\nAre you sure?")) {
               try {
                  OseeEventManager.kickBroadcastEvent(this, new BroadcastEvent(BroadcastEventType.Message, null,
                     message));
                  AWorkbench.popup("Success", "Message sent.");
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }
      }
   }

   public void handlePing() {
      if (MessageDialog.openConfirm(Displays.getActiveShell(), "Ping OSEE Instantiations?", "Ping OSEE Instantiations?")) {
         try {
            OseeEventManager.kickBroadcastEvent(this, new BroadcastEvent(BroadcastEventType.Ping, null,
               ClientSessionManager.getSession().toString()));
            AWorkbench.popup("Success", "Ping Sent");
         } catch (Exception ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
   }

   /*
    * @see IWorkbenchPart#createPartControl(Composite)
    */
   @Override
   public void createPartControl(Composite parent) {
      try {
         // IStatusLineManager slManager= getViewSite().getActionBars().getStatusLineManager();
         // slManager.setErrorMessage("error");
         parentComp = parent;

         GridData gridData = new GridData();
         gridData.verticalAlignment = GridData.FILL;
         gridData.horizontalAlignment = GridData.FILL;
         gridData.grabExcessVerticalSpace = true;
         gridData.grabExcessHorizontalSpace = true;

         GridLayout gridLayout = new GridLayout(1, false);
         gridData.heightHint = 1000;
         gridData.widthHint = 1000;
         parent.setLayout(gridLayout);

         tabFolder = new TabFolder(parent, SWT.BORDER);
         tabFolder.setLayoutData(gridData);

         // ModeChecker.check(parent);

         new OseeClientsTab(tabFolder);
         new ClientsStatsTab(tabFolder);
         new DbTableTab(tabFolder);

         parent.layout();

         createActions();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   /**
    * handles saving to the database for every tab item
    */
   public void save() {
      // database tab
      if (tabFolder.getSelectionIndex() == 2) {
         DbTableTab.dbTableViewer.save();
         setSaveNeeded(false);
      }
   }

   public static void setSaveNeeded(boolean needed) {

      if (needed) {
         saveAction.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.SAVE_NEEDED));
      } else {
         saveAction.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.SAVED));
      }
   }

}