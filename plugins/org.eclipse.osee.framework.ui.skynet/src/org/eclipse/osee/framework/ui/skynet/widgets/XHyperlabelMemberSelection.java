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
package org.eclipse.osee.framework.ui.skynet.widgets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.UserCheckTreeDialog;

/**
 * @author Donald G. Dunne
 */
public class XHyperlabelMemberSelection extends XHyperlinkLabelCmdValueSelection {

   Set<User> selectedUsers = new HashSet<>();
   private final Collection<User> users;
   private final Collection<User> teamMembers = new HashSet<>();

   public XHyperlabelMemberSelection(String label) {
      this(label, UserManager.getUsers());
   }

   public XHyperlabelMemberSelection(String label, Collection<User> users) {
      super(label, false, 80);
      this.users = users;
   }

   /**
    * If set, team members will be shown prior to rest of un-checked users
    */
   public void setTeamMembers(Collection<? extends User> teamMembers) {
      this.teamMembers.addAll(teamMembers);
   }

   public Set<User> getSelectedUsers() {
      return selectedUsers;
   }

   @Override
   public String getCurrentValue() {
      return Artifacts.toString("; ", selectedUsers);
   }

   public void setSelectedUsers(Set<User> selectedUsers) {
      this.selectedUsers = selectedUsers;
      refresh();
   }

   @Override
   public Object getData() {
      return getSelectedUsers();
   }

   @Override
   public boolean handleSelection() {
      try {
         UserCheckTreeDialog uld =
            new UserCheckTreeDialog("Select Users", "Select to assign.\nDeSelect to un-assign.", users);
         uld.setInitialSelections(selectedUsers);
         uld.setTeamMembers(teamMembers);
         if (uld.open() != 0) {
            return false;
         }
         selectedUsers.clear();
         for (User art : uld.getUsersSelected()) {
            selectedUsers.add(art);
         }
         return true;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return false;
   }

   @Override
   public boolean isEmpty() {
      return selectedUsers.isEmpty();
   }

}
