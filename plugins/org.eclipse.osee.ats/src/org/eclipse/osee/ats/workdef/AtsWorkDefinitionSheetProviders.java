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
package org.eclipse.osee.ats.workdef;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.core.workdef.WorkDefinitionSheet;
import org.eclipse.osee.ats.dsl.atsDsl.AtsDsl;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.workdef.config.ImportAIsAndTeamDefinitionsToDb;
import org.eclipse.osee.ats.workdef.provider.AtsWorkDefinitionImporter;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.PluginUtil;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.osgi.framework.Bundle;

/**
 * @author Donald G. Dunne
 */
public final class AtsWorkDefinitionSheetProviders {

   private static Set<IAtsWorkDefinitionSheetProvider> teamWorkflowExtensionItems;
   public static String WORK_DEF_TEAM_DEFAULT = "WorkDef_Team_Default";

   private AtsWorkDefinitionSheetProviders() {
      // Utility Class
   }

   public static void initializeDatabase(XResultData resultData) throws OseeCoreException {
      IAtsChangeSet changes = AtsClientService.get().createChangeSet("Import ATS Work Definitions, Teams and AIs");
      Artifact folder =
         OseeSystemArtifacts.getOrCreateArtifact(AtsArtifactToken.WorkDefinitionsFolder, AtsClientService.get().getAtsBranch());
      if (folder.isDirty()) {
         changes.add(folder);
      }
      List<WorkDefinitionSheet> sheets = getWorkDefinitionSheets();
      Set<String> stateNames = new HashSet<>();
      importWorkDefinitionSheets(resultData, changes, folder, sheets, stateNames);
      importTeamsAndAis(resultData, changes, folder, sheets);
      changes.execute();
   }

   public static void importWorkDefinitionSheets(XResultData resultData, IAtsChangeSet changes, Artifact folder, Collection<WorkDefinitionSheet> sheets, Set<String> stateNames) throws OseeCoreException {
      for (WorkDefinitionSheet sheet : sheets) {
         OseeLog.logf(Activator.class, Level.INFO, "Importing ATS Work Definitions [%s]", sheet.getName());
         Artifact artifact =
            AtsWorkDefinitionImporter.get().importWorkDefinitionSheetToDb(sheet, resultData, stateNames, changes);
         if (artifact != null) {
            folder.addChild(artifact);
            changes.add(artifact);
         }
      }
   }

   public static void importTeamsAndAis(XResultData resultData, IAtsChangeSet changes, Artifact folder, Collection<WorkDefinitionSheet> sheets) throws OseeCoreException {
      for (WorkDefinitionSheet sheet : sheets) {
         OseeLog.logf(Activator.class, Level.INFO, "Importing ATS Teams and AIs [%s]", sheet.getName());
         importAIsAndTeamsToDb(sheet, changes);
      }
   }

   public static void importAIsAndTeamsToDatabase() throws OseeCoreException {

      IAtsChangeSet changes = AtsClientService.get().createChangeSet("Import ATS AIs and Team Definitions");
      for (WorkDefinitionSheet sheet : getWorkDefinitionSheets()) {
         OseeLog.logf(Activator.class, Level.INFO, "Importing ATS AIs and Teams sheet [%s]", sheet.getName());
         importAIsAndTeamsToDb(sheet, changes);
      }
      changes.execute();
   }

   public static void importAIsAndTeamsToDb(WorkDefinitionSheet sheet, IAtsChangeSet changes) throws OseeCoreException {
      String modelName = sheet.getFile().getName();
      AtsDsl atsDsl = AtsDslUtil.getFromSheet(modelName, sheet);
      ImportAIsAndTeamDefinitionsToDb importer = new ImportAIsAndTeamDefinitionsToDb(modelName, atsDsl, changes);
      importer.execute();
   }

   public static List<WorkDefinitionSheet> getWorkDefinitionSheets() {
      List<WorkDefinitionSheet> sheets = new ArrayList<>();
      sheets.add(new WorkDefinitionSheet(WORK_DEF_TEAM_DEFAULT,
         getSupportFile(Activator.PLUGIN_ID, "support/WorkDef_Team_Default.ats")));
      sheets.add(new WorkDefinitionSheet("WorkDef_Task_Default",
         getSupportFile(Activator.PLUGIN_ID, "support/WorkDef_Task_Default.ats")));
      sheets.add(new WorkDefinitionSheet("WorkDef_Review_Decision",
         getSupportFile(Activator.PLUGIN_ID, "support/WorkDef_Review_Decision.ats")));
      sheets.add(new WorkDefinitionSheet("WorkDef_Review_PeerToPeer",
         getSupportFile(Activator.PLUGIN_ID, "support/WorkDef_Review_PeerToPeer.ats")));
      sheets.add(new WorkDefinitionSheet("WorkDef_Team_Simple",
         getSupportFile(Activator.PLUGIN_ID, "support/WorkDef_Team_Simple.ats")));
      sheets.add(
         new WorkDefinitionSheet("WorkDef_Goal", getSupportFile(Activator.PLUGIN_ID, "support/WorkDef_Goal.ats")));
      sheets.add(
         new WorkDefinitionSheet("WorkDef_Sprint", getSupportFile(Activator.PLUGIN_ID, "support/WorkDef_Sprint.ats")));
      for (IAtsWorkDefinitionSheetProvider provider : getProviders()) {
         sheets.addAll(provider.getWorkDefinitionSheets());
      }
      return sheets;
   }

   public static File getSupportFile(String pluginId, String filename) {
      try {
         PluginUtil util = new PluginUtil(pluginId);
         return util.getPluginFile(filename);
      } catch (IOException ex) {
         OseeLog.logf(Activator.class, Level.SEVERE, ex, "Unable to access work definition sheet [%s]", filename);
      }
      return null;
   }

   /*
    * due to lazy initialization, this function is non-reentrant therefore, the synchronized keyword is necessary
    */
   private synchronized static Set<IAtsWorkDefinitionSheetProvider> getProviders() {
      if (teamWorkflowExtensionItems != null) {
         return teamWorkflowExtensionItems;
      }
      teamWorkflowExtensionItems = new HashSet<>();

      IExtensionPoint point =
         Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.osee.ats.AtsWorkDefinitionSheetProvider");
      if (point == null) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP,
            "Can't access AtsWorkDefinitionSheetProvider extension point");
         return teamWorkflowExtensionItems;
      }
      IExtension[] extensions = point.getExtensions();
      for (IExtension extension : extensions) {
         IConfigurationElement[] elements = extension.getConfigurationElements();
         String classname = null;
         String bundleName = null;
         for (IConfigurationElement el : elements) {
            if (el.getName().equals("AtsWorkDefinitionSheetProvider")) {
               classname = el.getAttribute("classname");
               bundleName = el.getContributor().getName();
               if (classname != null && bundleName != null) {
                  Bundle bundle = Platform.getBundle(bundleName);
                  try {
                     Class<?> taskClass = bundle.loadClass(classname);
                     Object obj = taskClass.newInstance();
                     teamWorkflowExtensionItems.add((IAtsWorkDefinitionSheetProvider) obj);
                  } catch (Exception ex) {
                     OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP,
                        "Error loading AtsWorkDefinitionSheetProvider extension", ex);
                  }
               }
            }
         }
      }
      return teamWorkflowExtensionItems;
   }

}
