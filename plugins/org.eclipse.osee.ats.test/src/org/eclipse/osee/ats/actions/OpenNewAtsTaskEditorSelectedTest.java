/*
 * Created on Oct 24, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.actions;

import java.util.Arrays;
import java.util.List;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.ats.actions.OpenNewAtsTaskEditorSelected.IOpenNewAtsTaskEditorSelectedHandler;
import org.eclipse.osee.ats.core.AtsTestUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public class OpenNewAtsTaskEditorSelectedTest extends AbstractAtsActionRunTest {

   @Override
   public OpenNewAtsTaskEditorSelected createAction() {
      return new OpenNewAtsTaskEditorSelected(new IOpenNewAtsTaskEditorSelectedHandler() {

         @Override
         public List<? extends Artifact> getSelectedArtifacts() throws OseeCoreException {
            return Arrays.asList(AtsTestUtil.getTeamWf());
         }

         @Override
         public CustomizeData getCustomizeDataCopy() {
            return null;
         }
      });
   }
}
