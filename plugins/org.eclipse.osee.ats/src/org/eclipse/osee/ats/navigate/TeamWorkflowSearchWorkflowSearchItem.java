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
package org.eclipse.osee.ats.navigate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.widgets.XHyperlabelTeamDefinitionSelection;
import org.eclipse.osee.ats.util.widgets.XStateSearchCombo;
import org.eclipse.osee.ats.world.WorldEditor;
import org.eclipse.osee.ats.world.WorldEditorParameterSearchItem;
import org.eclipse.osee.ats.world.search.TeamWorldSearchItem;
import org.eclipse.osee.ats.world.search.TeamWorldSearchItem.ReleasedOption;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.XCheckBox;
import org.eclipse.osee.framework.ui.skynet.widgets.XCombo;
import org.eclipse.osee.framework.ui.skynet.widgets.XMembersCombo;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IDynamicWidgetLayoutListener;
import org.eclipse.osee.framework.ui.skynet.widgets.util.SwtXWidgetRenderer;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetRendererItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Donald G. Dunne
 */
public class TeamWorkflowSearchWorkflowSearchItem extends WorldEditorParameterSearchItem {

   protected XHyperlabelTeamDefinitionSelection teamCombo = null;
   protected XCombo releasedCombo = null;
   protected XCombo versionCombo = null;
   protected XMembersCombo assigneeCombo;
   protected XCheckBox includeCompletedCheckbox;
   protected XCheckBox includeCancelledCheckbox;
   private XStateSearchCombo stateCombo = null;
   private TeamWorldSearchItem searchItem;

   public TeamWorkflowSearchWorkflowSearchItem(String name) {
      super(name, AtsImage.TEAM_WORKFLOW);
   }

   public TeamWorkflowSearchWorkflowSearchItem() {
      this("Team Workflow Search");
   }

   public TeamWorkflowSearchWorkflowSearchItem(TeamWorkflowSearchWorkflowSearchItem editTeamWorkflowSearchItem) {
      super(editTeamWorkflowSearchItem, AtsImage.TEAM_WORKFLOW);
   }

   @Override
   public TeamWorkflowSearchWorkflowSearchItem copy() {
      return new TeamWorkflowSearchWorkflowSearchItem(this);
   }

   @Override
   public TeamWorkflowSearchWorkflowSearchItem copyProvider() {
      return new TeamWorkflowSearchWorkflowSearchItem(this);
   }

   @Override
   public String getParameterXWidgetXml() throws OseeCoreException {
      return "<xWidgets>" +
      //
      "<XWidget displayName=\"Team Definitions(s)\" xwidgetType=\"XHyperlabelTeamDefinitionSelection\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget displayName=\"Version\" xwidgetType=\"XCombo()\" beginComposite=\"8\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget displayName=\"Released\" xwidgetType=\"XCombo(Both,Released,UnReleased)\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget displayName=\"Assignee\" xwidgetType=\"XMembersCombo\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget displayName=\"State\" xwidgetType=\"XStateSearchCombo\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget displayName=\"Include Completed\" beginComposite=\"6\" xwidgetType=\"XCheckBox\" defaultValue=\"false\" labelAfter=\"true\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget displayName=\"Include Cancelled\" xwidgetType=\"XCheckBox\" defaultValue=\"false\" labelAfter=\"true\" horizontalLabel=\"true\"/>" +
      //
      "</xWidgets>";
   }

   @Override
   public String getSelectedName(SearchType searchType) throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      Collection<IAtsTeamDefinition> teamDefs = getSelectedTeamDefinitions();
      if (!teamDefs.isEmpty()) {
         sb.append(" - Teams: ");
         sb.append(org.eclipse.osee.framework.jdk.core.util.Collections.toString(",", teamDefs));
      }
      if (getSelectedVersionArtifact() != null) {
         sb.append(" - Version: ");
         sb.append(getSelectedVersionArtifact());
      }
      ReleasedOption releaseOption = getSelectedReleased();
      if (releaseOption != null && releaseOption != ReleasedOption.Both) {
         sb.append(" - ReleasedOption: ");
         sb.append(releaseOption);
      }
      if (getSelectedUser() != null) {
         sb.append(" - Assignee: ");
         sb.append(getSelectedUser());
      }
      if (getSelectedState() != null) {
         sb.append(" - State: ");
         sb.append(getSelectedState());
      }
      if (isIncludeCompletedCheckbox() && isIncludeCancelledCheckbox()) {
         sb.append(" - Include Completed/Cancelled");
      }
      if (isIncludeCompletedCheckbox()) {
         sb.append(" - Include Completed");
      }
      if (isIncludeCancelledCheckbox()) {
         sb.append(" - Include Cancelled");
      }
      return Strings.truncate(getName() + sb.toString(), WorldEditor.TITLE_MAX_LENGTH, true);
   }

   @Override
   public void widgetCreated(XWidget widget, FormToolkit toolkit, Artifact art, SwtXWidgetRenderer dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) throws OseeCoreException {
      if (widget.getLabel().equals("Assignee")) {
         assigneeCombo = (XMembersCombo) widget;
      }
      if (widget.getLabel().equals("Include Completed")) {
         includeCompletedCheckbox = (XCheckBox) widget;
      }
      if (widget.getLabel().equals("Include Cancelled")) {
         includeCancelledCheckbox = (XCheckBox) widget;
      }
      if (widget.getLabel().equals("State")) {
         stateCombo = (XStateSearchCombo) widget;
         stateCombo.getComboViewer().getCombo().setVisibleItemCount(25);
         widget.setToolTip("Select State of Task");
      }
      if (widget.getLabel().equals("Version")) {
         versionCombo = (XCombo) widget;
         versionCombo.getComboBox().setVisibleItemCount(25);
         widget.setToolTip("Select Team to populate Version list");
      }
      if (widget.getLabel().equals("Released")) {
         releasedCombo = (XCombo) widget;
      }
      if (widget.getLabel().equals("Team Definitions(s)")) {
         teamCombo = (XHyperlabelTeamDefinitionSelection) widget;
         teamCombo.addXModifiedListener(new XModifiedListener() {
            @Override
            public void widgetModified(XWidget widget) {
               if (versionCombo != null) {
                  try {
                     Collection<IAtsTeamDefinition> teamDefArts = getSelectedTeamDefinitions();
                     if (teamDefArts.isEmpty()) {
                        versionCombo.setDataStrings(new String[] {});
                        return;
                     }
                     IAtsTeamDefinition teamDefHoldingVersions =
                        teamDefArts.iterator().next().getTeamDefinitionHoldingVersions();
                     if (teamDefHoldingVersions == null) {
                        versionCombo.setDataStrings(new String[] {});
                        return;
                     }
                     List<String> sorted = getSortedVersions(teamDefHoldingVersions);
                     if (sorted.isEmpty()) {
                        versionCombo.setDataStrings(new String[] {});
                        return;
                     }
                     versionCombo.setDataStrings(sorted.toArray(new String[sorted.size()]));
                  } catch (Exception ex) {
                     OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
                  }
               }
            }

            private List<String> getSortedVersions(IAtsTeamDefinition teamDefHoldingVersions) {
               List<IAtsVersion> versions = new ArrayList<>();
               versions.addAll(teamDefHoldingVersions.getVersions());
               Collections.sort(versions, new Comparator<IAtsVersion>() {

                  @Override
                  public int compare(IAtsVersion aObj1, IAtsVersion aObj2) {
                     if (!aObj1.isReleased() && aObj2.isReleased()) {
                        return -1;
                     } else if (aObj1.isReleased() && !aObj2.isReleased()) {
                        return 1;
                     }
                     return aObj1.getName().compareTo(aObj2.getName());
                  }
               });
               List<String> sorted = new ArrayList<>();
               for (IAtsVersion version : versions) {
                  String postfix = "";
                  if (version.isNextVersion()) {
                     postfix = " (Next)";
                  } else if (version.isReleased()) {
                     postfix = " (Released)";
                  }
                  sorted.add(version + postfix);
               }
               return sorted;
            }
         });
      }
   }

   public IAtsUser getSelectedUser() throws OseeCoreException {
      if (assigneeCombo == null || assigneeCombo.getUser() == null) {
         return null;
      }
      return AtsClientService.get().getUserService().getUserById(assigneeCombo.getUser().getUserId());
   }

   public void setSelectedUser(IAtsUser user) throws OseeCoreException {
      if (assigneeCombo != null) {
         assigneeCombo.set(
            (User) AtsClientService.get().getUserService().getUserById(user.getUserId()).getStoreObject());
      }
   }

   public String getSelectedState() {
      if (stateCombo == null) {
         return null;
      }
      return stateCombo.getSelectedState();
   }

   public boolean isIncludeCancelledCheckbox() {
      if (includeCancelledCheckbox == null) {
         return false;
      }
      return includeCancelledCheckbox.isSelected();
   }

   public void setIncludeCancelledCheckbox(boolean selected) {
      if (includeCancelledCheckbox != null) {
         includeCancelledCheckbox.set(selected);
      }
   }

   public boolean isIncludeCompletedCheckbox() {
      if (includeCompletedCheckbox == null) {
         return false;
      }
      return includeCompletedCheckbox.isSelected();
   }

   public void setIncludeCompletedCheckbox(boolean selected) {
      if (includeCompletedCheckbox != null) {
         includeCompletedCheckbox.set(selected);
      }
   }

   public IAtsVersion getSelectedVersionArtifact() throws OseeCoreException {
      if (versionCombo == null) {
         return null;
      }
      String versionStr = versionCombo.get();
      if (!Strings.isValid(versionStr)) {
         return null;
      }
      Collection<IAtsTeamDefinition> teamDefs = getSelectedTeamDefinitions();
      if (teamDefs.size() > 0) {
         IAtsTeamDefinition teamDefHoldingVersions = teamDefs.iterator().next().getTeamDefinitionHoldingVersions();
         if (teamDefHoldingVersions == null) {
            return null;
         }
         for (IAtsVersion versionArtifact : teamDefHoldingVersions.getVersions()) {
            if (versionArtifact.getName().equals(versionStr)) {
               return versionArtifact;
            }
         }
      }
      return null;
   }

   public void setVersion(String versionStr) {
      if (versionCombo != null && versionCombo.getInDataStrings() != null && versionCombo.getInDataStrings().length > 0) {
         versionCombo.set(versionStr);
      }
   }

   public Collection<IAtsTeamDefinition> getSelectedTeamDefinitions() {
      if (teamCombo == null) {
         return java.util.Collections.emptyList();
      }
      return teamCombo.getSelectedTeamDefintions();
   }

   public void setSelectedTeamDefinitions(Collection<IAtsTeamDefinition> selectedTeamDefs) {
      if (teamCombo != null) {
         teamCombo.setSelectedTeamDefs(selectedTeamDefs);
         teamCombo.notifyXModifiedListeners();
      }
   }

   public ReleasedOption getSelectedReleased() {
      if (releasedCombo == null || !Strings.isValid(releasedCombo.get())) {
         return ReleasedOption.Both;
      }
      return ReleasedOption.valueOf(releasedCombo.get());
   }

   public void setSelectedReleased(ReleasedOption option) {
      if (releasedCombo != null) {
         releasedCombo.set(option.toString());
      }
   }

   @Override
   public void createXWidgetLayoutData(XWidgetRendererItem layoutData, XWidget widget, FormToolkit toolkit, Artifact art, XModifiedListener modListener, boolean isEditable) {
      // do nothing
   }

   @Override
   public void widgetCreating(XWidget widget, FormToolkit toolkit, Artifact art, SwtXWidgetRenderer dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) {
      // do nothing
   }

   @Override
   public Result isParameterSelectionValid() throws OseeCoreException {
      try {
         boolean selected = false;
         Collection<IAtsTeamDefinition> teamDefs = getSelectedTeamDefinitions();
         if (teamDefs.size() > 0) {
            selected = true;
         }
         IAtsVersion verArt = getSelectedVersionArtifact();
         if (verArt != null) {
            selected = true;
         }
         IAtsUser user = getSelectedUser();
         if (user != null) {
            selected = true;
         }
         boolean includeCompleted = isIncludeCompletedCheckbox() || isIncludeCancelledCheckbox();
         if (!selected) {
            return new Result("You must select at least Team, Version or Assignee.");
         }
         if (user != null && includeCompleted) {
            return new Result("Assignee and Include Completed are not compatible selections.");
         }
         if (user != null && includeCompleted && verArt == null && teamDefs.isEmpty()) {
            return new Result("You must select at least Team or Version with Include Completed.");
         }
         return Result.TrueResult;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return new Result("Exception: " + ex.getLocalizedMessage());
      }
   }

   @Override
   public IDynamicWidgetLayoutListener getDynamicWidgetLayoutListener() {
      return null;
   }

   @Override
   public IAtsVersion getTargetedVersionArtifact() throws OseeCoreException {
      if (versionCombo == null) {
         return null;
      }
      return getSelectedVersionArtifact();
   }

   @Override
   public void setupSearch() {
      searchItem = new TeamWorldSearchItem("", getSelectedTeamDefinitions(), isIncludeCompletedCheckbox(),
         isIncludeCancelledCheckbox(), false, false, getSelectedVersionArtifact(), getSelectedUser(),
         getSelectedReleased(), getSelectedState());
   }

   @Override
   public Collection<Artifact> performSearch(SearchType searchType) {
      return searchItem.performSearchGetResults(false);
   }

}
