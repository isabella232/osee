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
package org.eclipse.osee.framework.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * @author Donald G. Dunne
 */
public class OSEEFilteredTree extends FilteredTree {

   public OSEEFilteredTree(Composite parent) {
      this(parent, SWT.BORDER | SWT.MULTI, new ToStringContainsPatternFilter());
   }

   public OSEEFilteredTree(Composite parent, int treeStyle) {
      super(parent, treeStyle, new ToStringContainsPatternFilter(), false);
      setInitialText("");
   }

   public OSEEFilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
      super(parent, treeStyle, filter, false);
      setInitialText("");
   }

   @Override
   protected Composite createFilterControls(Composite parent) {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(ALayout.getZeroMarginLayout(3, false));
      comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      new Label(comp, SWT.NONE).setText("Filter: ");
      super.createFilterControls(comp);

      return comp;
   }

   @Override
   protected void createFilterText(Composite parent) {
      super.createFilterText(parent);
      filterText.addKeyListener(new KeyListener() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.ARROW_DOWN) {
               treeViewer.getTree().setFocus();
            } else {
               filterText.setFocus();
            }
         }

         @Override
         public void keyReleased(KeyEvent e) {
            if (e.keyCode == SWT.ARROW_DOWN) {
               treeViewer.getTree().setFocus();
            } else {
               filterText.setFocus();
            }
         }
      });
   }

}
