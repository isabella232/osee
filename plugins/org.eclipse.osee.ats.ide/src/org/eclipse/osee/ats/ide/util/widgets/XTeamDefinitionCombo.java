/*********************************************************************
 * Copyright (c) 2010 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.ats.ide.util.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.core.config.TeamDefinitionSorter;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Donald G. Dunne
 */
public class XTeamDefinitionCombo extends XComboViewer {
   public static final String WIDGET_ID = XTeamDefinitionCombo.class.getSimpleName();
   private Artifact selectedTeamDef = null;

   public XTeamDefinitionCombo() {
      super("Team Definition", SWT.READ_ONLY);
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      super.createControls(parent, horizontalSpan);

      Collection<IAtsTeamDefinition> teamDefs = null;
      try {
         teamDefs = AtsClientService.get().getTeamDefinitionService().getTeamDefinitions(Active.Active);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Error loading team definitions", ex);
      }

      if (teamDefs != null) {
         List<IAtsTeamDefinition> sortedTeamDefs = new ArrayList<>();
         sortedTeamDefs.addAll(teamDefs);
         Collections.sort(sortedTeamDefs, new TeamDefinitionSorter());
         getComboViewer().setInput(sortedTeamDefs);
         ArrayList<Object> defaultSelection = new ArrayList<>();
         defaultSelection.add("--select--");
         setSelected(defaultSelection);
         addXModifiedListener(new XModifiedListener() {

            @Override
            public void widgetModified(XWidget widget) {
               selectedTeamDef = AtsClientService.get().getQueryServiceClient().getArtifact(getSelected());
            }
         });
      }
   }

   public Artifact getSelectedTeamDef() {
      return selectedTeamDef;
   }

}
