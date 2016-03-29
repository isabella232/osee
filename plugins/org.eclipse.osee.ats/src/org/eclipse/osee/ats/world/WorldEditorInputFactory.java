/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.world;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osee.ats.search.AtsSearchWorkflowSearchItem;
import org.eclipse.osee.ats.world.search.AbstractWorkItemSearchItem;
import org.eclipse.osee.ats.world.search.AtsSearchGoalSearchItem;
import org.eclipse.osee.ats.world.search.AtsSearchReviewSearchItem;
import org.eclipse.osee.ats.world.search.AtsSearchTaskSearchItem;
import org.eclipse.osee.ats.world.search.AtsSearchTeamWorkflowSearchItem;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * The factory which is capable of recreating class file editor inputs stored in a memento.
 *
 * @author Donald G. Dunne
 */
public class WorldEditorInputFactory implements IElementFactory {

   public final static String ID = "org.eclipse.osee.ats.WorldEditorInputFactory"; //$NON-NLS-1$
   public final static String ART_UUIDS = "org.eclipse.osee.ats.WorldEditorInputFactory.artUuids"; //$NON-NLS-1$
   public final static String BRANCH_KEY = "org.eclipse.osee.ats.WorldEditorInputFactory.branchUuid"; //$NON-NLS-1$
   public final static String TITLE = "org.eclipse.osee.ats.WorldEditorInputFactory.title"; //$NON-NLS-1$
   public final static String ATS_SEARCH_UUID = "org.eclipse.osee.ats.WorldEditorInputFactory.atsSearchUuid"; //$NON-NLS-1$
   private static final String ATS_SEARCH_NAMESPACE = "org.eclipse.osee.ats.WorldEditorInputFactory.atsSearchNamespace"; //$NON-NLS-1$;

   public WorldEditorInputFactory() {
   }

   /*
    * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
    */
   @Override
   public IAdaptable createElement(IMemento memento) {
      long atsSearchUuid = 0L;
      long branchUuid = 0;
      String title = memento.getString(TITLE);
      if (Strings.isValid(memento.getString(BRANCH_KEY))) {
         branchUuid = Long.valueOf(memento.getString(BRANCH_KEY));
      }
      List<Integer> artUuids = new ArrayList<>();
      String artUuidsStr = memento.getString(ART_UUIDS);
      if (Strings.isValid(artUuidsStr)) {
         for (String artUuid : artUuidsStr.split(",")) {
            artUuids.add(Integer.valueOf(artUuid));
         }
      }
      String atsSearchUuidStr = memento.getString(ATS_SEARCH_UUID);
      if (Strings.isNumeric(atsSearchUuidStr)) {
         atsSearchUuid = Long.valueOf(atsSearchUuidStr);
      }
      try {
         if (atsSearchUuid > 0L) {
            String namespace = memento.getString(ATS_SEARCH_NAMESPACE);
            if (Strings.isValid(namespace)) {
               if (AtsSearchTeamWorkflowSearchItem.NAMESPACE.equals(namespace)) {
                  AbstractWorkItemSearchItem searchItem = new AtsSearchTeamWorkflowSearchItem();
                  searchItem.setRestoreUuid(atsSearchUuid);
                  return new WorldEditorInput(new WorldEditorParameterSearchItemProvider(searchItem, null));
               }
               if (AtsSearchTaskSearchItem.NAMESPACE.equals(namespace)) {
                  AbstractWorkItemSearchItem searchItem = new AtsSearchTaskSearchItem();
                  searchItem.setRestoreUuid(atsSearchUuid);
                  return new WorldEditorInput(new WorldEditorParameterSearchItemProvider(searchItem, null));
               }
               if (AtsSearchGoalSearchItem.NAMESPACE.equals(namespace)) {
                  AbstractWorkItemSearchItem searchItem = new AtsSearchGoalSearchItem();
                  searchItem.setRestoreUuid(atsSearchUuid);
                  return new WorldEditorInput(new WorldEditorParameterSearchItemProvider(searchItem, null));
               }
               if (AtsSearchReviewSearchItem.NAMESPACE.equals(namespace)) {
                  AtsSearchReviewSearchItem searchItem = new AtsSearchReviewSearchItem();
                  searchItem.setRestoreUuid(atsSearchUuid);
                  return new WorldEditorInput(new WorldEditorParameterSearchItemProvider(searchItem, null));
               }
               for (IAtsWorldEditorItem item : AtsWorldEditorItems.getItems()) {
                  if (item.isWorldEditorSearchProviderNamespaceMatch(namespace)) {
                     return item.getNewWorldEditorInputFromNamespace(namespace, atsSearchUuid);
                  }
               }
            }
            AtsSearchWorkflowSearchItem searchItem = new AtsSearchWorkflowSearchItem();
            searchItem.setRestoreUuid(atsSearchUuid);
            return new WorldEditorInput(new WorldEditorParameterSearchItemProvider(searchItem, null));
         }
      } catch (Exception ex) {
         // do nothing
      }
      return new WorldEditorInput(new WorldEditorReloadProvider(title, branchUuid, artUuids));
   }

   public static void saveState(IMemento memento, WorldEditorInput input) {
      String title = input.getName();
      String artUuids = Collections.toString(",", input.getGuids());
      long branchUuid = input.getBranchUuid();

      if (Strings.isValid(artUuids) && branchUuid > 0 && Strings.isValid(title)) {
         memento.putString(BRANCH_KEY, String.valueOf(branchUuid));
         memento.putString(ART_UUIDS, artUuids);
         memento.putString(TITLE, title);
      }
      if (input.getAtsSearchUuid() > 0L) {
         memento.putString(ATS_SEARCH_UUID, String.valueOf(input.getAtsSearchUuid()));
         memento.putString(ATS_SEARCH_NAMESPACE, String.valueOf(input.getAtsSearchNamespace()));
      }
   }

}
