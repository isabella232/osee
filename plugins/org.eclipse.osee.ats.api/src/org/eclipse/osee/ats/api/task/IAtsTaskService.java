/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.task;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface IAtsTaskService {

   Collection<IAtsTask> createTasks(IAtsTeamWorkflow teamWf, List<String> titles, List<IAtsUser> assignees, Date createdDate, IAtsUser createdBy, String relatedToState, String taskWorkDef, Map<String, List<String>> attributes, String commitComment);

   Collection<IAtsTask> createTasks(NewTaskData newTaskData);

   Collection<IAtsTask> createTasks(IAtsTeamWorkflow teamWf, List<String> titles, List<IAtsUser> assignees, Date createdDate, IAtsUser createdBy, String relatedToState, String taskWorkDef, Map<String, List<String>> attributes, IAtsChangeSet changes);

   NewTaskData getNewTaskData(IAtsTeamWorkflow teamWf, List<String> titles, List<IAtsUser> assignees, Date createdDate, IAtsUser createdBy, String relatedToState, String taskWorkDef, Map<String, List<String>> attributes, String commitComment);

   Collection<IAtsTask> createTasks(NewTaskData newTaskData, IAtsChangeSet changes);

   Collection<IAtsTask> createTasks(NewTaskDatas newTaskDatas);

   NewTaskData getNewTaskData(IAtsTeamWorkflow teamWf, List<String> titles, List<IAtsUser> assignees, Date createdDate, IAtsUser createdBy, String relatedToState, String taskWorkDef, Map<String, List<String>> attributes);

   Collection<IAtsTask> getTasks(IAtsTeamWorkflow teamWf, IStateToken relatedToState) throws OseeCoreException;

   Collection<IAtsTask> getTaskArtifacts(IAtsWorkItem workItem) throws OseeCoreException;

   Collection<IAtsTask> getTasks(IAtsTeamWorkflow teamWf) throws OseeCoreException;

   Collection<? extends IAtsTask> getTasks(IAtsWorkItem workItem, IStateToken state);

}
