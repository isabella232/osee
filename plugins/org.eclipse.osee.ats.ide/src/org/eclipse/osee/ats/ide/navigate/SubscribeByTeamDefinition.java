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

package org.eclipse.osee.ats.ide.navigate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.ats.ide.util.widgets.dialog.TeamDefinitionCheckTreeDialog;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;

/**
 * @author Donald G. Dunne
 */
public class SubscribeByTeamDefinition extends XNavigateItemAction {

   public SubscribeByTeamDefinition(XNavigateItem parent) {
      super(parent, "Subscribe by Team Definition", FrameworkImage.EMAIL);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) {
      final TeamDefinitionCheckTreeDialog diag = new TeamDefinitionCheckTreeDialog(getName(),
         "Select Team Definition\n\nEmail will be sent for every Action created against these Teams.", Active.Active,
         false);
      try {
         List<IAtsTeamDefinition> objs = new ArrayList<>();
         for (ArtifactToken art : AtsClientService.get().getRelationResolver().getRelated(
            (IAtsObject) AtsClientService.get().getUserService().getCurrentUser(),
            AtsRelationTypes.SubscribedUser_Artifact)) {
            if (art.isOfType(AtsArtifactTypes.TeamDefinition)) {
               objs.add(AtsClientService.get().getTeamDefinitionService().getTeamDefinitionById(art));
            }
         }
         diag.setInitialTeamDefs(objs);
         if (diag.open() != 0) {
            return;
         }
         Collection<IAtsTeamDefinition> selected = diag.getChecked();
         Collection<Artifact> arts = AtsClientService.get().getConfigArtifacts(selected);

         User user = UserManager.getUserByArtId(AtsClientService.get().getUserService().getCurrentUser());
         SubscribeUtility.setSubcriptionsAndPersist(user, AtsRelationTypes.SubscribedUser_Artifact, arts,
            AtsArtifactTypes.TeamDefinition, getClass().getSimpleName());
         AWorkbench.popup(getName(), "Subscriptions updated.");
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }
}
