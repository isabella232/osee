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
package org.eclipse.osee.ats.core.client.review;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class DecisionOption {
   private String name;
   private Collection<IAtsUser> assignees = new HashSet<IAtsUser>();
   private boolean followupRequired;

   @Override
   public int hashCode() {
      int result = 17;
      result = 31 * result + name.hashCode();

      return result;
   }

   public DecisionOption(String name, Collection<IAtsUser> assignees, boolean followup) {
      this.name = name;
      this.followupRequired = followup;
      if (assignees != null) {
         this.assignees = assignees;
      }
   }

   public DecisionOption(String name, IAtsUser assignee, boolean followup) {
      this.name = name;
      this.followupRequired = followup;
      if (assignee != null) {
         this.assignees.add(assignee);
      }
   }

   public DecisionOption(String name) {
      this(name, (IAtsUser) null, false);
   }

   public DecisionOption() {
      this("", (IAtsUser) null, false);
   }

   @Override
   public String toString() {
      return name;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof DecisionOption) {
         DecisionOption state = (DecisionOption) obj;
         if (!state.name.equals(name)) {
            return false;
         }
         return true;
      }
      return super.equals(obj);
   }

   public Collection<IAtsUser> getAssignees() {
      return assignees;
   }

   /**
    * Sets the assigness but DOES NOT write to SMA. This method should NOT be called outside the StateMachineArtifact.
    */
   public void setAssignees(Collection<IAtsUser> assignees) {
      this.assignees.clear();
      if (assignees != null) {
         this.assignees.addAll(assignees);
      }
   }

   /**
    * Sets the assignes but DOES NOT write to SMA. This method should NOT be called outside the StateMachineArtifact.
    */
   public void setAssignee(IAtsUser assignee) {
      this.assignees.clear();
      if (assignee != null) {
         this.assignees.add(assignee);
      }
   }

   public void addAssignee(IAtsUser assignee) {
      if (assignee != null) {
         this.assignees.add(assignee);
      }
   }

   /**
    * @return Returns the name.
    */
   public String getName() {
      return name;
   }

   /**
    * @param name The name to set.
    */
   public void setName(String name) {
      this.name = name;
   }

   public String toXml() throws OseeCoreException {
      StringBuffer sb = new StringBuffer(name);
      sb.append(";");
      for (IAtsUser u : assignees) {
         sb.append("<" + u.getUserId() + ">");
      }
      sb.append(";");
      sb.append(followupRequired);
      return sb.toString();
   }

   public Result setFromXml(String xml) {
      Matcher m = Pattern.compile("^(.*?);(.*?);(.*)$").matcher(xml);
      if (m.find()) {
         String toState = m.group(2).toLowerCase();
         name = m.group(1);
         if (name.equals("")) {
            return new Result("Invalid name");
         }
         if (toState.equals("followup")) {
            followupRequired = true;
         } else if (toState.equals("completed")) {
            followupRequired = false;
         } else {
            return new Result("Invalid followup string \"" + m.group(2) + "\"\nShould be followup or completed");
         }
         m = Pattern.compile("<(.*?)>").matcher(m.group(3));
         while (m.find()) {
            try {
               assignees.add(AtsClientService.get().getUserService().getUserById(m.group(1)));
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
         if (followupRequired && assignees.isEmpty()) {
            return new Result("If followup is specified, must set assignees.\nShould be: <userid><userid>");
         } else if (!followupRequired && assignees.size() > 0) {
            return new Result("If completed is specified, don't specify assigness.  Leave blank.\n");
         }
      } else {
         return new Result(
            "Can't unpack decision option data => " + xml + "\n\n" + "must be in format: \"Name;(followup|completed);<userid1><userid2>\"" + "where true if followup is required; false if not.  If followup required, assignees will be userid1, userid2.");
      }
      return Result.TrueResult;
   }

   /**
    * @return the followupRequired
    */
   public boolean isFollowupRequired() {
      return followupRequired;
   }

   /**
    * @param followupRequired the followupRequired to set
    */
   public void setFollowupRequired(boolean followupRequired) {
      this.followupRequired = followupRequired;
   }

}
