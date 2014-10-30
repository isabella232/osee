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
package org.eclipse.osee.coverage.util.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageOption;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactNameSorter;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.FilteredCheckboxTreeDialog;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class CoverageMethodListDialog extends FilteredCheckboxTreeDialog {

   public CoverageMethodListDialog(Collection<CoverageOption> values) {
      this(values, new ArrayList<CoverageOption>());
   }

   public CoverageMethodListDialog(Collection<CoverageOption> values, Collection<CoverageOption> selected) {
      super("Select Coverage Method(s)", "Select Coverage Method(s)", new ArrayTreeContentProvider(), labelProvider,
         new ArtifactNameSorter());
      try {
         setInput(values.toArray(new CoverageOption[values.size()]));
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      setInitialSelections(selected);
   }

   public Set<CoverageOption> getSelected() {
      Set<CoverageOption> selected = new HashSet<CoverageOption>();
      for (Object obj : getResult()) {
         selected.add((CoverageOption) obj);
      }
      return selected;
   }

   static ILabelProvider labelProvider = new ILabelProvider() {

      @Override
      public Image getImage(Object element) {
         return null;
      }

      @Override
      public String getText(Object element) {
         if (element instanceof CoverageOption) {
            return ((CoverageOption) element).getNameDesc();
         }
         return "Unknown";
      }

      @Override
      public void addListener(ILabelProviderListener listener) {
         // do nothing
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
      public void removeListener(ILabelProviderListener listener) {
         // do nothing
      }

   };

}
