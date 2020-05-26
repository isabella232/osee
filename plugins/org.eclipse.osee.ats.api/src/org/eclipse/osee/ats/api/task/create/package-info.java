/*********************************************************************
 * Copyright (c) 2019 Boeing
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

/**
 * Provides the classes necessary to create tasks for different cases and design of task builder code.
 * <p>
 *
 * @link AtsTaskDefToken - Token used to specify what CreateTasksDefinition to use in generating tasks. This can be
 * specified in XCreateCodeTestTasksButton for use in WorkDef or through ATsTaskService to call programatically. <br/>
 * <br/>
 * <br/>
 * @link StaticTaskDefinition - Definition of static task to be created. This is only used by AtsTaskService.addTask and
 * not by tasks to be created by change report changes. TBD: May want to rename this to StaticCreateTaskDefinition?
 * <br/>
 * <br/>
 * @link CreateTasksDefinition - definition that determines what to do when tasks are to be generated. This is created
 * used the task builder in java code and provided to ATS framework through osgi as IAtsTaskSetDefinitionProvider.
 * See @link AtsTaskSetDefinitionProvider for examples. It also contains a list of StaticTaskDefinition for static tasks
 * to be defined to create regardless of the change report.<br/>
 * <br/>
 * <br/>
 * @link ChangeReportTaskData - Top level pojo that is sent into the ATS endpoint for task from change reports. As
 * creation operation moves through its processing, different values are added and set for future use.<br/>
 * It contains:<br/>
 * <ul>
 * <li>1..n of @link ChangeReportTaskTeamWfData - PoJo that represents that work to be done for one Team Workflow and
 * its generated tasks. There will be one of these for every sibling team workflow configured for tasks.<br/>
 * </li>
 * <li>1..n of @link ChangeReportTaskMatch - PoJo that represents 3 things. 1) ChangeReportArt it references 2) Task
 * Title 3) IAtsTask that exists or is null. One object will be created for each art/title pair. This allows for a
 * single change art to be tied to multiple tasks. eg: Different partitions need a task created for the same change
 * art.<br/>
 * </li>
 * </ul>
 */
package org.eclipse.osee.ats.api.task.create;
