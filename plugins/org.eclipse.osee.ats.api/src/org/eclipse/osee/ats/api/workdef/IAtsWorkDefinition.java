/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.ats.api.workdef;

import java.util.List;
import org.eclipse.osee.ats.api.task.create.CreateTasksDefinition;
import org.eclipse.osee.ats.api.task.create.CreateTasksDefinitionBuilder;
import org.eclipse.osee.ats.api.workdef.model.HeaderDefinition;
import org.eclipse.osee.framework.jdk.core.type.NamedId;

/**
 * @author Donald G. Dunne
 */
public interface IAtsWorkDefinition extends NamedId {

   List<IAtsStateDefinition> getStates();

   IAtsStateDefinition getStateByName(String name);

   IAtsStateDefinition getStartState();

   boolean hasHeaderDefinitionItems();

   HeaderDefinition getHeaderDef();

   HeaderDefinition getDefaultHeaderDef();

   void setHeaderDefinition(HeaderDefinition headerDef);

   boolean isShowStateMetrics();

   void setShowStateMetrics(boolean showStateMetrics);

   default void addCreateTasksDefinition(CreateTasksDefinitionBuilder createTasksDefBldr) {
      getCreateTasksDefs().add(createTasksDefBldr.getCreateTasksDef());
   }

   List<CreateTasksDefinition> getCreateTasksDefs();

}