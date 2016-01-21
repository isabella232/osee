/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column;

import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.swt.SWT;

/**
 * @author Morgan E. Cook
 */
public class RelatedArtifactChangedColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static RelatedArtifactChangedColumn instance = new RelatedArtifactChangedColumn();

   public static RelatedArtifactChangedColumn getInstance() {
      return instance;
   }

   private RelatedArtifactChangedColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".RelatedArtifactChangedColumn", "Related Artifact Changed", 75,
         SWT.LEFT, true, SortDataType.String, false,
         "Committed - baseline/committed branch \nUnmodified - Related artifact has not changed " + "\n<date> - Related artifact has been modified after task at the specified date \nEmpty - There is no related artifact");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public RelatedArtifactChangedColumn copy() {
      RelatedArtifactChangedColumn newXCol = new RelatedArtifactChangedColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof Artifact) {
            Artifact refArt =
               ((Artifact) element).getSoleAttributeValue(AtsAttributeTypes.TaskToChangedArtifactReference, null);

            if (refArt != null) {
               BranchId refBranch = refArt.getBranch();
               if (refArt.isDeleted()) {
                  return "Deleted";
               } else if (BranchManager.getState(refBranch).isCommitted() || BranchManager.getType(
                  refBranch).isBaselineBranch()) {
                  return "Commited";
               } else if (refArt.getLastModified().after(((Artifact) element).getLastModified())) {
                  return refArt.getLastModified().toString();
               } else {
                  return "Unmodified";
               }
            }
         }
      } catch (OseeCoreException ex) {
         return LogUtil.getCellExceptionString(ex);
      }
      return "";
   }
}