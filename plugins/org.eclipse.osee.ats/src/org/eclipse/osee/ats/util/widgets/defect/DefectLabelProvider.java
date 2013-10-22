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
package org.eclipse.osee.ats.util.widgets.defect;

import java.util.logging.Level;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.Disposition;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.InjectionActivity;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.Severity;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class DefectLabelProvider extends XViewerLabelProvider {
   private final DefectXViewer xViewer;

   public DefectLabelProvider(DefectXViewer xViewer) {
      super(xViewer);
      this.xViewer = xViewer;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn dCol, int columnIndex) {
      ReviewDefectItem defectItem = (ReviewDefectItem) element;
      if (dCol.equals(DefectXViewerFactory.Severity_Col)) {
         return DefectSeverityToImage.getImage(defectItem.getSeverity());
      } else if (dCol.equals(DefectXViewerFactory.Injection_Activity_Col)) {
         return ImageManager.getImage(FrameworkImage.INFO_SM);
      } else if (dCol.equals(DefectXViewerFactory.Disposition_Col)) {
         return DefectDispositionToImage.getImage(defectItem.getDisposition());
      } else if (dCol.equals(DefectXViewerFactory.Closed_Col)) {
         return ImageManager.getImage(defectItem.isClosed() ? PluginUiImage.CHECKBOX_ENABLED : PluginUiImage.CHECKBOX_DISABLED);
      } else if (dCol.equals(DefectXViewerFactory.User_Col)) {
         try {
            return ArtifactImageManager.getImage(AtsClientService.get().getUserAdmin().getOseeUser(
               defectItem.getUser()));
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return null;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn aCol, int columnIndex) {
      ReviewDefectItem defectItem = (ReviewDefectItem) element;
      if (aCol.equals(DefectXViewerFactory.User_Col)) {
         String name;
         try {
            name = defectItem.getUser().getName();
         } catch (OseeCoreException ex) {
            name = String.format("Erroring getting user name: [%s]", ex.getLocalizedMessage());
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
         return name;
      } else if (aCol.equals(DefectXViewerFactory.Closed_Col)) {
         return String.valueOf(defectItem.isClosed());
      } else if (aCol.equals(DefectXViewerFactory.Created_Date_Col)) {
         return DateUtil.getMMDDYYHHMM(defectItem.getDate());
      } else if (aCol.equals(DefectXViewerFactory.Description_Col)) {
         return defectItem.getDescription();
      } else if (aCol.equals(DefectXViewerFactory.Resolution_Col)) {
         return defectItem.getResolution();
      } else if (aCol.equals(DefectXViewerFactory.Location_Col)) {
         return defectItem.getLocation();
      } else if (aCol.equals(DefectXViewerFactory.Severity_Col)) {
         return defectItem.getSeverity().equals(Severity.None) ? "" : defectItem.getSeverity().name();
      } else if (aCol.equals(DefectXViewerFactory.Disposition_Col)) {
         return defectItem.getDisposition().equals(Disposition.None) ? "" : defectItem.getDisposition().name();
      } else if (aCol.equals(DefectXViewerFactory.Injection_Activity_Col)) {
         return defectItem.getInjectionActivity() == InjectionActivity.None ? "" : defectItem.getInjectionActivity().name();
      }
      return "Unhandled Column";
   }

   @Override
   public void dispose() {
      // do nothing
   }

   @Override
   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   @Override
   public void addListener(ILabelProviderListener listener) {
      // do nothing
   }

   @Override
   public void removeListener(ILabelProviderListener listener) {
      // do nothing
   }

   public DefectXViewer getTreeViewer() {
      return xViewer;
   }
}
