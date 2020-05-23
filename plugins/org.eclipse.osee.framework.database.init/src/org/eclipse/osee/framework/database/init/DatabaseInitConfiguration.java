/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.framework.database.init;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Roberto E. Escobar
 */
public class DatabaseInitConfiguration implements IDatabaseInitConfiguration {
   private final List<String> dbInitTasks;
   private final List<String> oseeTypeIds;

   public DatabaseInitConfiguration() {
      this.dbInitTasks = new ArrayList<>();
      this.oseeTypeIds = new ArrayList<>();
   }

   public void addTask(String taskId) {
      dbInitTasks.add(taskId);
   }

   public void addOseeType(String oseeTypesExtensionIds) {
      oseeTypeIds.add(oseeTypesExtensionIds);
   }

   public void addOseeType(DefaultOseeTypeDefinitions typeDef) {
      addDefaultType(oseeTypeIds, typeDef);
   }

   @Override
   public List<String> getTaskExtensionIds() {
      List<String> initTasks = new ArrayList<>();
      addDefaultTask(initTasks, DefaultDbInitTasks.BOOTSTRAP_TASK);
      initTasks.addAll(dbInitTasks);
      addDefaultTask(initTasks, DefaultDbInitTasks.DB_USER_CLEANUP);
      addDefaultTask(initTasks, DefaultDbInitTasks.BRANCH_DATA_IMPORT);
      return initTasks;
   }

   @Override
   public List<String> getOseeTypeExtensionIds() {
      Set<String> oseeTypes = new LinkedHashSet<>();
      oseeTypes.addAll(oseeTypeIds);
      return new ArrayList<>(oseeTypes);
   }

   private void addDefaultTask(Collection<String> initTasks, DefaultDbInitTasks task) {
      initTasks.add(task.getExtensionId());
   }

   private void addDefaultType(Collection<String> initTasks, DefaultOseeTypeDefinitions type) {
      initTasks.add(type.getExtensionId());
   }
}