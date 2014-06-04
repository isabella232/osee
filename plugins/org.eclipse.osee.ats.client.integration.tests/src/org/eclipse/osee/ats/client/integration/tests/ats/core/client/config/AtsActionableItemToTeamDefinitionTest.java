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
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.config;

import static org.junit.Assert.assertFalse;
import java.util.Arrays;
import java.util.Collection;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.core.client.config.AtsBulkLoad;
import org.eclipse.osee.ats.core.config.TeamDefinitions;

/**
 * @author Donald G. Dunne
 */
public class AtsActionableItemToTeamDefinitionTest {

   @org.junit.Test
   public void testAtsActionableItemToTeamDefinition() throws Exception {
      boolean error = false;
      StringBuffer sb = new StringBuffer("Actionable Actionable Items with no Team Def associated:\n");
      AtsBulkLoad.reloadConfig(true);
      for (IAtsActionableItem aia : AtsClientService.get().getConfig().get(IAtsActionableItem.class)) {
         if (aia.isActionable()) {
            Collection<IAtsTeamDefinition> impactedTeamDefs = TeamDefinitions.getImpactedTeamDefs(Arrays.asList(aia));
            if (impactedTeamDefs.isEmpty()) {
               System.out.println(" ");
               sb.append("[" + aia + "]");
               AtsClientService.get().getConfig().get(IAtsTeamDefinition.class);
               error = true;
            }
         }
      }
      assertFalse(sb.toString(), error);
   }
}
