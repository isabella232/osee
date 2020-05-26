/*********************************************************************
 * Copyright (c) 2015 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.ats.api.task;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.jdk.core.result.XResultData;

/**
 * @author Donald G. Dunne
 */
public class NewTaskData {

   @JsonSerialize(using = ToStringSerializer.class)
   Long teamWfId;
   List<JaxAtsTask> newTasks = new ArrayList<>();
   String asUserId;
   String commitComment;
   XResultData results;
   Boolean fixTitles = false;

   public Long getTeamWfId() {
      return teamWfId;
   }

   public void setTeamWfId(Long teamWfId) {
      this.teamWfId = teamWfId;
   }

   public List<JaxAtsTask> getNewTasks() {
      return newTasks;
   }

   public void setNewTasks(List<JaxAtsTask> newTasks) {
      this.newTasks = newTasks;
   }

   public String getCommitComment() {
      return commitComment;
   }

   public void setCommitComment(String commitComment) {
      this.commitComment = commitComment;
   }

   public String getAsUserId() {
      return asUserId;
   }

   public void setAsUserId(String asUserId) {
      this.asUserId = asUserId;
   }

   @Override
   public String toString() {
      return "NewTaskData [teamId=" + teamWfId + ", tasks=" + newTasks + ", asUserId=" + asUserId + ", commitComment=" + commitComment + "]";
   }

   public boolean isEmpty() {
      return newTasks == null || newTasks.isEmpty();
   }

   public XResultData getResults() {
      return results;
   }

   public void setResults(XResultData results) {
      this.results = results;
   }

   public Boolean getFixTitles() {
      return fixTitles;
   }

   public Boolean isFixTitles() {
      return fixTitles;
   }

   public void setFixTitles(Boolean fixTitles) {
      this.fixTitles = fixTitles;
   }
}
