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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.BroadcastEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BroadcastEventType;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.FontManager;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class OseeClientsTab {

   private final User whoAmI;
   private final List<User> users;
   private CheckboxTreeViewer peopleCheckboxTreeViewer;
   private Composite mainComposite;
   private Text text;

   public OseeClientsTab(TabFolder tabFolder) throws OseeCoreException {
      super();
      this.users = UserManager.getUsersSortedByName();
      this.whoAmI = UserManager.getUser();
      users.remove(whoAmI);
      this.mainComposite = null;
      createControl(tabFolder);
      mainComposite.setEnabled(isUserAllowedToOperate());
   }

   private void createControl(TabFolder tabFolder) throws OseeCoreException {
      mainComposite = new Composite(tabFolder, SWT.NONE);
      mainComposite.setLayout(new GridLayout());
      mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      TabItem tab = new TabItem(tabFolder, SWT.NONE);
      tab.setControl(mainComposite);
      tab.setText("OSEE Clients");

      Group group = new Group(mainComposite, SWT.NONE);
      group.setLayout(new GridLayout());
      group.setText("Issue Shutdown Request");
      group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      if (isUserAllowedToOperate()) {
         SashForm sashForm = new SashForm(group, SWT.NONE);
         sashForm.setLayout(new GridLayout());
         sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

         createUserSelectionArea(sashForm);
         createMessageArea(sashForm);
      } else {
         createDefaultWarning(group);
      }
   }

   private void createMessageArea(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(new GridLayout());
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      Group group = new Group(composite, SWT.NONE);
      group.setLayout(new GridLayout());
      group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      group.setText("Enter Reason for Shutdown -- THIS WILL BE DISPLAYED TO THE SELECTED USERS --");

      text = new Text(group, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
      text.setEditable(true);
      GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
      gd.heightHint = 100;
      text.setLayoutData(gd);

      Button button = new Button(composite, SWT.NONE);
      button.setText("Send Shutdown Request");
      button.setToolTipText("By pressing the send button, a shutdown message will be sent to\n" + "all the selected OSEE clients causing their workbench to close.\n" + "NOTE: Users will be prompted to save their work.");
      button.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            processShutdownRequest(text.getText(), getSelectedUsers());
         }
      });

      Composite blankComposite = new Composite(composite, SWT.NONE);
      blankComposite.setLayout(new GridLayout());
      blankComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
   }

   public static Control createDefaultWarning(Composite parent) {
      Composite composite = new Composite(parent, SWT.BORDER);
      composite.setLayout(new GridLayout(2, false));
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      composite.setBackground(Displays.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

      Label image = new Label(composite, SWT.NONE);
      image.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, true));
      image.setImage(ImageManager.getImage(FrameworkImage.LOCKED_KEY));
      image.setBackground(Displays.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

      Label label = new Label(composite, SWT.NONE);
      label.setFont(FontManager.getFont("Courier New", 10, SWT.BOLD));
      label.setForeground(Displays.getSystemColor(SWT.COLOR_DARK_RED));
      label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true));
      label.setText("Access Denied.\nContact your administrator.");
      label.setBackground(Displays.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

      return composite;
   }

   private void processShutdownRequest(String reason, Collection<User> selectedUsers) {
      if (Strings.isValid(reason) && !selectedUsers.isEmpty()) {
         boolean result =
            MessageDialog.openConfirm(mainComposite.getShell(), "Disconnect OSEE Clients",
               "Are you sure you want to shutdown the selected OSEE clients?");
         if (false != result) {
            try {
               OseeEventManager.kickBroadcastEvent(this, new BroadcastEvent(BroadcastEventType.Force_Shutdown,
                  selectedUsers, reason));
               AWorkbench.popup("Success", "Shutdown request sent.");
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      } else {
         StringBuilder error = new StringBuilder();
         error.append("Unable to process request.\n");
         if (reason == null || reason.length() <= 0) {
            error.append("  A reason must be entered before a client can be requested to shutdown.\n");
         }
         if (selectedUsers.isEmpty()) {
            error.append("  At least 1 user must be selected.\n");
         }
         MessageDialog.openError(mainComposite.getShell(), "Disconnect OSEE Clients", error.toString());
      }
   }

   private boolean isUserAllowedToOperate() throws OseeCoreException {
      return AccessControlManager.isOseeAdmin();
   }

   private Collection<User> getSelectedUsers() {
      Set<User> toReturn = new HashSet<User>();
      try {
         Object[] checked = peopleCheckboxTreeViewer.getCheckedElements();
         for (Object object : checked) {
            if (false != peopleCheckboxTreeViewer.getChecked(object)) {
               toReturn.add((User) object);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return toReturn;
   }

   private void createUserSelectionArea(Composite parent) {
      Group group = new Group(parent, SWT.NONE);
      group.setLayout(new GridLayout());
      group.setText("Select Users to Shutdown");

      peopleCheckboxTreeViewer =
         new CheckboxTreeViewer(group, SWT.BORDER | SWT.MULTI | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL);
      peopleCheckboxTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      peopleCheckboxTreeViewer.setContentProvider(new TreeContentProvider());
      peopleCheckboxTreeViewer.setLabelProvider(new PersonLabelProvider());
      peopleCheckboxTreeViewer.setInput(users);
      Composite composite = new Composite(group, SWT.NONE);
      composite.setLayout(new GridLayout(2, true));

      Button selectAll = new Button(composite, SWT.PUSH);
      selectAll.setText("Select All");
      selectAll.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            selectAll(true);
         }
      });

      Button deselectAll = new Button(composite, SWT.PUSH);
      deselectAll.setText("Deselect All");
      deselectAll.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            selectAll(false);
         }
      });
   }

   private void selectAll(boolean isSelectAll) {
      Object[] objects = peopleCheckboxTreeViewer.getExpandedElements();
      for (Object object : objects) {
         peopleCheckboxTreeViewer.setSubtreeChecked(object, isSelectAll);
      }
   }

   private static class TreeContentProvider implements ITreeContentProvider {

      @Override
      public void dispose() {
         // Nothing to dispose
      }

      @Override
      public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
         // Nothing to change
      }

      @Override
      public Object[] getChildren(Object parentElement) {
         return null;
      }

      @Override
      public Object getParent(Object element) {
         return null;
      }

      @Override
      public boolean hasChildren(Object element) {
         return false;
      }

      @SuppressWarnings("rawtypes")
      @Override
      public Object[] getElements(Object inputElement) {
         return ((ArrayList) inputElement).toArray();
      }
   }

   private static class PersonLabelProvider extends LabelProvider {

      @Override
      public Image getImage(Object arg0) {
         return null;
      }

      @Override
      public String getText(Object arg0) {
         return ((User) arg0).getName();
      }
   }

}
