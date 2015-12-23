/*******************************************************************************
 * Copyright (c) 2010 Boeing.
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
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectManager;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class ReviewNumMinorDefectsColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static ReviewNumMinorDefectsColumn instance = new ReviewNumMinorDefectsColumn();

   public static ReviewNumMinorDefectsColumn getInstance() {
      return instance;
   }

   private ReviewNumMinorDefectsColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".reviewMinorDefects", "Review Minor Defects", 40, SWT.CENTER, false,
         SortDataType.Integer, false, "Number of Minor Defects found in Review");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ReviewNumMinorDefectsColumn copy() {
      ReviewNumMinorDefectsColumn newXCol = new ReviewNumMinorDefectsColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof PeerToPeerReviewArtifact) {
            return String.valueOf(new ReviewDefectManager((PeerToPeerReviewArtifact) element).getNumMinor());
         }
      } catch (OseeCoreException ex) {
         LogUtil.getCellExceptionString(ex);
      }
      return "";
   }
}
