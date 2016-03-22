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
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.api.review.Role;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.client.review.role.UserRoleManager;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;

/**
 * @author Donald G. Dunne
 */
public class ReviewReviewerColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static ReviewReviewerColumn instance = new ReviewReviewerColumn();

   public static ReviewReviewerColumn getInstance() {
      return instance;
   }

   private ReviewReviewerColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".reviewReviewer", "Review Reviewer", 100, XViewerAlign.Left, false,
         SortDataType.String, false, "Review Reviewers(s)");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ReviewReviewerColumn copy() {
      ReviewReviewerColumn newXCol = new ReviewReviewerColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof PeerToPeerReviewArtifact) {
            UserRoleManager roleMgr = new UserRoleManager((PeerToPeerReviewArtifact) element);
            return AtsObjects.toString("; ", roleMgr.getRoleUsers(Role.Reviewer));
         }
      } catch (OseeCoreException ex) {
         LogUtil.getCellExceptionString(ex);
      }
      return "";
   }
}
