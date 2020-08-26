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

package org.eclipse.osee.ats.ide.column;

import java.util.Collection;
import java.util.Map;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.ide.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.ide.world.WorldXViewerFactory;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;

/**
 * @author Donald G. Dunne
 */
public class ParentIdColumn extends XViewerAtsColumn implements IAtsXViewerPreComputedColumn {

   public static ParentIdColumn instance = new ParentIdColumn();

   public static ParentIdColumn getInstance() {
      return instance;
   }

   private ParentIdColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".parentid", "Parent Id", 75, XViewerAlign.Left, false,
         SortDataType.String, false, "ID of Parent Action or Team Workflow");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ParentIdColumn copy() {
      ParentIdColumn newXCol = new ParentIdColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   public String getText(Object element) {
      try {
         if (element instanceof AbstractWorkflowArtifact && ((AbstractWorkflowArtifact) element).getParentAWA() != null) {
            return AtsApiService.get().getWorkItemService().getCombinedPcrId(
               (IAtsWorkItem) ((AbstractWorkflowArtifact) element).getParentAWA());
         }
      } catch (OseeCoreException ex) {
         return LogUtil.getCellExceptionString(ex);
      }
      return "";
   }

   @Override
   public void populateCachedValues(Collection<?> objects, Map<Long, String> preComputedValueMap) {
      for (Object obj : objects) {
         preComputedValueMap.put(getKey(obj), getText(obj));
      }
   }

}
