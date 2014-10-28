/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.dialog;

import org.eclipse.osee.ats.util.widgets.dialog.ActionableItemListDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class ActionableItemListDialogTest {

   @Test
   public void testNewActionWizard() {
      ActionableItemListDialog dialog = new ActionableItemListDialog(Active.Active, "test");
      try {
         dialog.setBlockOnOpen(false);
         dialog.open();

         int count = dialog.getTreeViewer().getViewer().getTree().getItemCount();
         Assert.assertTrue(count >= 5);
      } finally {
         dialog.close();
      }
   }
}
