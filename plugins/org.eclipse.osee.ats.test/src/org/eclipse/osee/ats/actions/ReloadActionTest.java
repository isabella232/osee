/*
 * Created on Oct 24, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.actions;

import org.eclipse.osee.ats.core.AtsTestUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public class ReloadActionTest extends AbstractAtsActionRunTest {

   @Override
   public ReloadAction createAction() throws OseeCoreException {
      return new ReloadAction(AtsTestUtil.getTeamWf());
   }

}
