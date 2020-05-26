/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.ats.ide.config.program;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.workflow.teamwf.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.logging.OseeLog;
import org.osgi.framework.Bundle;

/**
 * @author Donald G. Dunne
 */
public abstract class ProgramManagers {

   public static IAtsProgramManager getAtsProgramManager(TeamWorkFlowArtifact teamArt) {
      for (IAtsProgramManager program : getAtsProgramManagers()) {
         if (program.isApplicable(teamArt)) {
            return program;
         }
      }
      return null;
   }

   @SuppressWarnings("rawtypes")
   public static Set<IAtsProgramManager> getAtsProgramManagers() {
      Set<IAtsProgramManager> programItems = new HashSet<>();
      IExtensionPoint point =
         Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID + ".AtsProgramManager");
      if (point == null) {
         OseeLog.log(Activator.class, Level.SEVERE, "Can't access AtsProgram extension point");
         return null;
      }
      IExtension[] extensions = point.getExtensions();
      for (IExtension extension : extensions) {
         IConfigurationElement[] elements = extension.getConfigurationElements();
         String classname = null;
         String bundleName = null;
         for (IConfigurationElement el : elements) {
            if (el.getName().equals("AtsProgramManager")) {
               classname = el.getAttribute("classname");
               bundleName = el.getContributor().getName();
               if (classname != null && bundleName != null) {
                  Bundle bundle = Platform.getBundle(bundleName);
                  try {
                     Class taskClass = bundle.loadClass(classname);
                     Object obj = taskClass.newInstance();
                     programItems.add((IAtsProgramManager) obj);
                  } catch (Exception ex) {
                     OseeLog.log(Activator.class, Level.SEVERE, "Error loading AtsProgramManager extension", ex);
                  }
               }
            }
         }
      }
      return programItems;
   }
}