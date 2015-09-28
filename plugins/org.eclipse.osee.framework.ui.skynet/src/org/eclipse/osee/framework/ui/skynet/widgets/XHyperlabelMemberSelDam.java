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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;

/**
 * Select users and store as single userId attributes
 * 
 * @author Donald G. Dunne
 */
public class XHyperlabelMemberSelDam extends XHyperlabelMemberSelection implements IAttributeWidget {

   private Artifact artifact;
   private IAttributeType attributeType;

   public XHyperlabelMemberSelDam(String displayLabel) {
      super(displayLabel);
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public IAttributeType getAttributeType() {
      return attributeType;
   }

   @Override
   public void setAttributeType(Artifact artifact, IAttributeType attributeType) {
      this.artifact = artifact;
      this.attributeType = attributeType;

      super.setSelectedUsers(getStoredUsers());
   }

   public Set<User> getStoredUsers() {
      Set<User> users = new HashSet<>();
      try {
         for (String userId : artifact.getAttributesToStringList(attributeType)) {
            try {
               users.add(UserManager.getUserByUserId(userId));
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }

      return users;
   }

   @Override
   public void saveToArtifact() {
      try {
         Set<String> userIds = new HashSet<>();
         for (User user : getSelectedUsers()) {
            userIds.add(user.getUserId());
         }
         artifact.setAttributeValues(attributeType, userIds);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public Result isDirty() {
      if (isEditable()) {
         Set<User> selected = getSelectedUsers();
         Set<User> stored = getStoredUsers();
         if (!Collections.isEqual(selected, stored)) {
            return new Result(true, attributeType + " is dirty");
         }
      }
      return Result.FalseResult;
   }

   @Override
   public void revert() {
      super.setSelectedUsers(getStoredUsers());
   }
}
