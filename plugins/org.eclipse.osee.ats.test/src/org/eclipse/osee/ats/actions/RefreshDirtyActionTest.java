/*
 * Created on Oct 24, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.actions;

import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;

public class RefreshDirtyActionTest extends AbstractAtsActionRunTest {

   @Override
   public RefreshDirtyAction createAction() {
      return new RefreshDirtyAction(new IDirtiableEditor() {

         @Override
         public void onDirtied() {
            System.out.println(" ");
         }
      });
   }

}
