/*
 * Created on May 12, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.test.config;

import junit.framework.TestCase;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Donald G. Dunne
 */
public class AtsTeamDefintionToWorkflowTest extends TestCase {

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception {
      super.setUp();
   }

   public void testTeamDefintionToWorkflow() throws Exception {
      for (Artifact artifact : ArtifactQuery.getArtifactsFromType(TeamDefinitionArtifact.ARTIFACT_NAME,
            BranchPersistenceManager.getAtsBranch())) {
         TeamDefinitionArtifact teamDef = (TeamDefinitionArtifact) artifact;
         if (teamDef.getWorkFlowDefinition() == null) {
            System.err.println(teamDef + " has no Work Flow associated.");
         }
      }
   }
}
