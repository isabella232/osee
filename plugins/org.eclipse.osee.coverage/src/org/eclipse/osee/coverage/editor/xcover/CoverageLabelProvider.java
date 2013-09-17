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
package org.eclipse.osee.coverage.editor.xcover;

import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.internal.ServiceProvider;
import org.eclipse.osee.coverage.merge.MergeItem;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.model.MessageCoverageItem;
import org.eclipse.osee.coverage.store.OseeCoverageUnitStore;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.skynet.core.utility.UsersByIds;
import org.eclipse.osee.framework.ui.skynet.FrameworkArtifactImageProvider;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.cm.IOseeCmService.ImageType;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;

public class CoverageLabelProvider extends XViewerLabelProvider {

   private final CoverageXViewer xViewer;

   public CoverageLabelProvider(CoverageXViewer xViewer) {
      super(xViewer);
      this.xViewer = xViewer;
   }

   public static Image getCoverageItemUserImage(ICoverage coverageItem) {
      try {
         if (coverageItem.isAssignable() && Strings.isValid(coverageItem.getAssignees())) {
            return FrameworkArtifactImageProvider.getUserImage(UsersByIds.getUsers(coverageItem.getAssignees()));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) {
      if (element instanceof MessageCoverageItem && xCol.equals(CoverageXViewerFactory.Name)) {
         return ImageManager.getImage(FrameworkImage.X_RED);
      }
      if (element instanceof MessageCoverageItem) {
         return null;
      }
      ICoverage coverageItem = (ICoverage) element;
      if (xCol.equals(CoverageXViewerFactory.Assignees_Col)) {
         return getCoverageItemUserImage(coverageItem);
      }
      if (xCol.equals(CoverageXViewerFactory.Name)) {
         return ImageManager.getImage(coverageItem.getOseeImage());
      }
      if (xCol.equals(CoverageXViewerFactory.Work_Product_Task)) {
         if (Strings.isValid(coverageItem.getWorkProductTaskStr())) {
            return ImageManager.getImage(ServiceProvider.getOseeCmService().getImage(ImageType.Task));
         }
      }
      return null;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      ICoverage coverage = (ICoverage) element;
      if (xCol.equals(CoverageXViewerFactory.Name)) {
         return coverage.getName();
      }
      if (element instanceof MessageCoverageItem) {
         return "";
      }
      if (xCol.equals(CoverageXViewerFactory.Assignees_Col)) {
         if (element instanceof CoverageUnit) {
            return Artifacts.toString("; ", OseeCoverageUnitStore.getAssignees((CoverageUnit) coverage));
         }
         return "";
      }
      if (xCol.equals(CoverageXViewerFactory.Notes_Col)) {
         return coverage.getNotes();
      }
      if (xCol.equals(CoverageXViewerFactory.Coverage_Percent)) {
         return coverage.getCoveragePercentStr();
      }
      if (xCol.equals(CoverageXViewerFactory.Location)) {
         return coverage.getLocation();
      }
      if (xCol.equals(CoverageXViewerFactory.Full_Path)) {
         return CoverageUtil.getFullPath(coverage);
      }
      if (xCol.equals(CoverageXViewerFactory.Namespace)) {
         return coverage.getNamespace();
      }
      if (xCol.equals(CoverageXViewerFactory.Work_Product_Task)) {
         return coverage.getWorkProductTaskStr();
      }
      if (xCol.equals(CoverageXViewerFactory.Guid)) {
         return coverage.getGuid();
      }
      if (xCol.equals(CoverageXViewerFactory.Unit)) {
         String unit = "";
         if (element instanceof CoverageUnit) {
            unit = coverage.getName();
         } else {
            unit = coverage.getParent().getName();
         }

         return unit;
      }
      if (xCol.equals(CoverageXViewerFactory.Lines_Covered)) {
         if (element instanceof CoverageUnit) {
            return String.valueOf(((CoverageUnit) coverage).getCoverageItemsCoveredCount(true));
         }
      }
      if (xCol.equals(CoverageXViewerFactory.Total_Lines)) {
         if (element instanceof CoverageUnit) {
            return String.valueOf(((CoverageUnit) coverage).getCoverageItems(true).size());
         }
      }

      if (coverage instanceof CoverageItem || (coverage instanceof MergeItem && ((MergeItem) coverage).getImportItem() instanceof CoverageItem)) {
         CoverageItem coverageItem = null;
         if (coverage instanceof CoverageItem) {
            coverageItem = (CoverageItem) coverage;
         } else {
            coverageItem = (CoverageItem) ((MergeItem) coverage).getImportItem();
         }
         if (xCol.equals(CoverageXViewerFactory.Coverage_Rationale)) {
            return coverageItem.getRationale();
         }
         if (xCol.equals(CoverageXViewerFactory.Method_Number)) {
            return coverageItem.getParent().getOrderNumber();
         }
         if (xCol.equals(CoverageXViewerFactory.Execution_Number)) {
            return coverageItem.getOrderNumber();
         }
         if (xCol.equals(CoverageXViewerFactory.Line_Number)) {
            return coverageItem.getLineNumber();
         }
         if (xCol.equals(CoverageXViewerFactory.Coverage_Method)) {
            return coverageItem.getCoverageMethod().getName();
         }
         if (xCol.equals(CoverageXViewerFactory.Parent_Coverage_Unit)) {
            return coverageItem.getCoverageUnit().getName();
         }
         if (xCol.equals(CoverageXViewerFactory.Coverage_Test_Units)) {
            Collection<String> testUnits = coverageItem.getTestUnits();
            if (testUnits == null) {
               return "";
            }
            return Collections.toString(", ", testUnits);
         }
         return "";
      }
      if (coverage instanceof CoverageUnit || (coverage instanceof MergeItem && ((MergeItem) coverage).getImportItem() instanceof CoverageUnit)) {
         CoverageUnit coverageUnit = null;
         if (coverage instanceof CoverageUnit) {
            coverageUnit = (CoverageUnit) coverage;
         } else if (coverage instanceof MergeItem) {
            coverageUnit = (CoverageUnit) ((MergeItem) coverage).getImportItem();
         }
         if (xCol.equals(CoverageXViewerFactory.Parent_Coverage_Unit)) {
            return coverageUnit.getParentCoverageUnit() == null ? "" : coverageUnit.getParentCoverageUnit().getName();
         }
         if (xCol.equals(CoverageXViewerFactory.Method_Number)) {
            return coverageUnit.getOrderNumber();
         }

      }
      if (coverage instanceof CoverageItem) {
         CoverageItem coverageItem = (CoverageItem) coverage;
         if (xCol.equals(CoverageXViewerFactory.Parent_Coverage_Unit)) {
            return coverageItem.getParent() == null ? "" : coverageItem.getParent().getName();
         }
         if (xCol.equals(CoverageXViewerFactory.Method_Number)) {
            return coverageItem.getParent() == null ? "" : coverageItem.getParent().getOrderNumber();
         }
         if (xCol.equals(CoverageXViewerFactory.Execution_Number)) {
            return coverageItem.getOrderNumber();
         }
         if (xCol.equals(CoverageXViewerFactory.Coverage_Method)) {
            return coverageItem.getCoverageMethod().getName();
         }
      }
      if (coverage instanceof MergeItem && (((MergeItem) coverage).getImportItem() instanceof CoverageItem || ((MergeItem) coverage).getPackageItem() instanceof CoverageItem)) {
         MergeItem mergeItem = (MergeItem) coverage;
         ICoverage importItem = mergeItem.getImportItem();
         ICoverage packageItem = mergeItem.getPackageItem();
         if (xCol.equals(CoverageXViewerFactory.Parent_Coverage_Unit)) {
            if (importItem != null && importItem.getParent() != null) {
               return importItem.getParent().getName();
            } else if (packageItem != null && packageItem.getParent() != null) {
               return packageItem.getParent().getName();
            }
            return "";
         }
         if (xCol.equals(CoverageXViewerFactory.Method_Number)) {
            if (importItem != null && importItem.getParent() != null) {
               return importItem.getParent().getOrderNumber();
            } else if (packageItem != null && packageItem.getParent() != null) {
               return packageItem.getParent().getOrderNumber();
            }
            return "";
         }
         if (xCol.equals(CoverageXViewerFactory.Execution_Number)) {
            if (importItem != null) {
               return importItem.getOrderNumber();
            } else if (packageItem != null) {
               return packageItem.getOrderNumber();
            }
            return "";
         }
         if (xCol.equals(CoverageXViewerFactory.Coverage_Method)) {
            if (importItem != null) {
               return ((CoverageItem) importItem).getCoverageMethod().getName();
            } else if (packageItem != null) {
               return ((CoverageItem) packageItem).getCoverageMethod().getName();
            }
            return "";
         }
      }
      return "";

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

   public CoverageXViewer getTreeViewer() {
      return xViewer;
   }

   @Override
   public int getColumnGradient(Object element, XViewerColumn xCol, int columnIndex) throws Exception {
      if (element == null) {
         return 0;
      }
      if (element instanceof MessageCoverageItem) {
         return 0;
      }
      ICoverage coverageItem = (ICoverage) element;
      if (xCol.equals(CoverageXViewerFactory.Coverage_Percent)) {
         return coverageItem.getCoveragePercent().intValue();
      }
      return 0;
   }
}
