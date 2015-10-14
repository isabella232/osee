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
package org.eclipse.osee.ats.task;

import java.util.Collection;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;

/**
 * @author Donald G. Dunne
 */
public class TaskEditorParameterSearchItemProvider extends TaskEditorProvider {

   private final TaskEditorParameterSearchItem taskParameterSearchItem;

   public TaskEditorParameterSearchItemProvider(TaskEditorParameterSearchItem taskParameterSearchItem, CustomizeData customizeData, TableLoadOption... tableLoadOptions) {
      super(customizeData, tableLoadOptions);
      this.taskParameterSearchItem = taskParameterSearchItem;
   }

   @Override
   public String getTaskEditorLabel(SearchType searchType) throws OseeCoreException {
      return Strings.truncate(taskParameterSearchItem.getTaskEditorLabel(searchType), TaskEditor.TITLE_MAX_LENGTH,
         true);
   }

   @Override
   public Collection<? extends Artifact> getTaskEditorTaskArtifacts() throws OseeCoreException {
      return taskParameterSearchItem.getTaskEditorTaskArtifacts();
   }

   public boolean isFirstTime() {
      return taskParameterSearchItem.isFirstTime();
   }

   @Override
   public String getName() throws OseeCoreException {
      return taskParameterSearchItem.getName();
   }

   public TaskEditorParameterSearchItem getWorldSearchItem() {
      return taskParameterSearchItem;
   }

   @Override
   public ITaskEditorProvider copyProvider() {
      return new TaskEditorParameterSearchItemProvider((TaskEditorParameterSearchItem) taskParameterSearchItem.copy(),
         customizeData, tableLoadOptions);
   }

}
