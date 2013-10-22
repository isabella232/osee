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
package org.eclipse.osee.ats.util.xviewer.column;

import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.util.XViewerException;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.client.review.AbstractReviewArtifact;
import org.eclipse.osee.ats.core.client.review.role.UserRole;
import org.eclipse.osee.ats.core.client.review.role.UserRoleManager;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class XViewerReviewRoleColumn extends XViewerValueColumn {

   private final IAtsUser user;

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn need to extend this constructor to copy extra stored fields
    */
   @Override
   public XViewerReviewRoleColumn copy() {
      return new XViewerReviewRoleColumn(getUser(), getId(), getName(), getWidth(), getAlign(), isShow(),
         getSortDataType(), isMultiColumnEditable(), getDescription());
   }

   public XViewerReviewRoleColumn(IAtsUser user) {
      super("ats.column.role", "Role", 75, SWT.LEFT, true, SortDataType.String, false, null);
      this.user = user;
   }

   public XViewerReviewRoleColumn(IAtsUser user, String id, String name, int width, int align, boolean show, SortDataType sortDataType, boolean multiColumnEditable, String description) {
      super(id, name, width, align, show, sortDataType, multiColumnEditable, description);
      this.user = user;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) throws XViewerException {
      try {
         if (element instanceof AbstractReviewArtifact) {
            return getRolesStr((AbstractReviewArtifact) element, user);
         }
         return "";
      } catch (OseeCoreException ex) {
         throw new XViewerException(ex);
      }
   }

   private static String getRolesStr(AbstractReviewArtifact reviewArt, IAtsUser user) throws OseeCoreException {
      StringBuilder builder = new StringBuilder();
      for (UserRole role : UserRoleManager.getUserRoles(reviewArt)) {
         if (role.getUser().equals(user)) {
            builder.append(role.getRole().name());
            builder.append(", ");
         }
      }

      return builder.toString().replaceFirst(", $", "");
   }

   public IAtsUser getUser() {
      return user;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((user == null) ? 0 : user.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (!super.equals(obj)) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      XViewerReviewRoleColumn other = (XViewerReviewRoleColumn) obj;
      if (user == null) {
         if (other.user != null) {
            return false;
         }
      } else if (!user.equals(other.user)) {
         return false;
      }
      return true;
   }

}
