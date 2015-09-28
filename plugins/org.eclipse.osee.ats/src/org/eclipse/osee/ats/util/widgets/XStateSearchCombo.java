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
package org.eclipse.osee.ats.util.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.workdef.AtsWorkDefinitionSheetProviders;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Provides combo box to choose from all valid states in ATS configured items. Used for generic searching windows where
 * set states are not known.
 * 
 * @author Donald G. Dunne
 */
public class XStateSearchCombo extends XComboViewer {
   protected static List<String> validStates = new ArrayList<>();
   public static final String WIDGET_ID = XStateSearchCombo.class.getSimpleName();
   private String selectedState = null;

   public XStateSearchCombo() {
      super("State", SWT.NONE);
      ensurePopulated();
   }

   protected synchronized void ensurePopulated() {
      if (validStates.isEmpty()) {
         validStates.add("--select--");
         validStates.addAll(AtsWorkDefinitionSheetProviders.getAllValidStateNames());
         Collections.sort(validStates);
      }
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      super.createControls(parent, horizontalSpan);

      getComboViewer().setInput(validStates);
      ArrayList<Object> defaultSelection = new ArrayList<>();
      defaultSelection.add("--select--");
      setSelected(defaultSelection);
      addXModifiedListener(new XModifiedListener() {

         @Override
         public void widgetModified(XWidget widget) {
            String selected = (String) getSelected();
            if (selected.equals("--select--")) {
               selectedState = null;
            } else {
               selectedState = (String) getSelected();
            }
         }
      });
   }

   public String getSelectedState() {
      return selectedState;
   }

   @Override
   public void setSelected(List<Object> selected) {
      super.setSelected(selected);
      if (!selected.isEmpty() && !selected.iterator().next().equals("--select--")) {
         selectedState = (String) selected.iterator().next();
      }
   }

}
