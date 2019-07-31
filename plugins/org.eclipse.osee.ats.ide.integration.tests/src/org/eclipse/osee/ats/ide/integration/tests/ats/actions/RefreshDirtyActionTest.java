/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.ide.integration.tests.ats.actions;

import org.eclipse.osee.ats.ide.actions.RefreshDirtyAction;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;

/**
 * @author Donald G. Dunne
 */
public class RefreshDirtyActionTest extends AbstractAtsActionRunTest {

   @Override
   public RefreshDirtyAction createAction() {
      return new RefreshDirtyAction(new IDirtiableEditor() {

         @Override
         public void onDirtied() {
            // do nothing
         }
      });
   }

}
