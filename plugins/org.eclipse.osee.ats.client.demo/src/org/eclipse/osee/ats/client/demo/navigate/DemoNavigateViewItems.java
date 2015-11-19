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
package org.eclipse.osee.ats.client.demo.navigate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.query.ReleasedOption;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.client.demo.DemoTeam;
import org.eclipse.osee.ats.client.demo.PopulateDemoActions;
import org.eclipse.osee.ats.client.demo.internal.Activator;
import org.eclipse.osee.ats.client.demo.internal.AtsClientService;
import org.eclipse.osee.ats.config.ValidateAtsConfiguration;
import org.eclipse.osee.ats.core.config.TeamDefinitions;
import org.eclipse.osee.ats.health.ValidateAtsDatabase;
import org.eclipse.osee.ats.navigate.IAtsNavigateItem;
import org.eclipse.osee.ats.navigate.SearchNavigateItem;
import org.eclipse.osee.ats.version.CreateNewVersionItem;
import org.eclipse.osee.ats.version.ReleaseVersionItem;
import org.eclipse.osee.ats.world.search.ArtifactTypeSearchItem;
import org.eclipse.osee.ats.world.search.ArtifactTypeWithInheritenceSearchItem;
import org.eclipse.osee.ats.world.search.NextVersionSearchItem;
import org.eclipse.osee.ats.world.search.OpenWorkflowsByTeamDefSearchItem;
import org.eclipse.osee.ats.world.search.VersionTargetedForTeamSearchItem;
import org.eclipse.osee.ats.world.search.WorldSearchItem.LoadView;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemFolder;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateUrlItem;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionUtility;

/**
 * Provides the ATS Navigator items for the sample XYZ company's teams
 *
 * @author Donald G. Dunne
 */
public class DemoNavigateViewItems implements IAtsNavigateItem {

   public DemoNavigateViewItems() {
      super();
   }

   private static IAtsTeamDefinition getTeamDef(DemoTeam team) throws OseeCoreException {
      IAtsTeamDefinition results = null;
      // Add check to keep exception from occurring for OSEE developers running against production
      if (!ClientSessionManager.isProductionDataStore()) {
         try {
            results = AtsClientService.get().getConfig().getSoleByUuid(team.getTeamDefToken().getUuid(),
               IAtsTeamDefinition.class);
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return results;
   }

   @Override
   public List<XNavigateItem> getNavigateItems(XNavigateItem parentItem) throws OseeCoreException {
      List<XNavigateItem> items = new ArrayList<>();

      if (DbConnectionUtility.areOSEEServicesAvailable().isFalse()) {
         return items;
      }

      // If Demo Teams not configured, ignore these navigate items
      try {
         if (getTeamDef(DemoTeam.Process_Team) == null) {
            return items;
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.INFO, "Demo Teams Not Cofigured", ex);
         return items;
      }
      XNavigateItem jhuItem = new XNavigateItemFolder(parentItem, "John Hopkins Univ (JHU)");
      new XNavigateUrlItem(jhuItem, "Open JHU Website - Externally", "http://www.jhu.edu/", true);
      new XNavigateUrlItem(jhuItem, "Open JHU Website - Internally", "http://www.jhu.edu/", false);

      items.add(jhuItem);

      for (DemoTeam team : DemoTeam.values()) {
         try {
            IAtsTeamDefinition teamDef = getTeamDef(team);
            XNavigateItem teamItems = new XNavigateItemFolder(jhuItem, "JHU " + team.name().replaceAll("_", " "));
            new SearchNavigateItem(teamItems,
               new OpenWorkflowsByTeamDefSearchItem("Show Open " + teamDef + " Workflows", Arrays.asList(teamDef)));
            // Handle all children teams
            for (IAtsTeamDefinition childTeamDef : TeamDefinitions.getChildren(teamDef, true)) {
               new SearchNavigateItem(teamItems, new OpenWorkflowsByTeamDefSearchItem(
                  "Show Open " + childTeamDef + " Workflows", Arrays.asList(childTeamDef)));
            }
            if (teamDef.isTeamUsesVersions()) {
               if (team.name().contains("SAW")) {
                  new XNavigateUrlItem(teamItems, "Open SAW Website", "http://www.cisst.org/cisst/saw/", false);
               } else if (team.name().contains("CIS")) {
                  new XNavigateUrlItem(teamItems, "Open CIS Website", "http://www.cisst.org/cisst/cis/", false);
               }

               new SearchNavigateItem(teamItems, new NextVersionSearchItem(teamDef, LoadView.WorldEditor));
               new SearchNavigateItem(teamItems,
                  new VersionTargetedForTeamSearchItem(teamDef, null, false, LoadView.WorldEditor));
               new SearchNavigateItem(teamItems, new OpenWorkflowsByTeamDefSearchItem("Show Un-Released Team Workflows",
                  Arrays.asList(teamDef), true, ReleasedOption.UnReleased));
               new ReleaseVersionItem(teamItems, teamDef);
               new CreateNewVersionItem(teamItems, teamDef);
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }

      XNavigateItem adminItems = new XNavigateItem(jhuItem, "JHU Admin", FrameworkImage.LASER);

      new SearchNavigateItem(adminItems, new ArtifactTypeSearchItem("Show all Actions", AtsArtifactTypes.Action));
      new SearchNavigateItem(adminItems,
         new ArtifactTypeSearchItem("Show all Decision Review", AtsArtifactTypes.DecisionReview));
      new SearchNavigateItem(adminItems,
         new ArtifactTypeSearchItem("Show all PeerToPeer Review", AtsArtifactTypes.PeerToPeerReview));
      new SearchNavigateItem(adminItems,
         new ArtifactTypeWithInheritenceSearchItem("Show all Team Workflows", AtsArtifactTypes.TeamWorkflow));
      new SearchNavigateItem(adminItems, new ArtifactTypeSearchItem("Show all Tasks", AtsArtifactTypes.Task));

      XNavigateItem healthItems = new XNavigateItem(adminItems, "Health", FrameworkImage.LASER);
      new ValidateAtsDatabase(healthItems);
      new ValidateAtsConfiguration(healthItems);
      new CreateGoalTestDemoArtifacts(healthItems);

      XNavigateItem demoItems = new XNavigateItem(adminItems, "Demo Data", PluginUiImage.ADMIN);
      new PopulateDemoActions(demoItems);

      return items;
   }
}
