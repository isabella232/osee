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

package org.eclipse.osee.framework.ui.skynet.results.html;

import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.skynet.cm.IOseeCmService;
import org.eclipse.osee.framework.ui.skynet.cm.OseeCmEditor;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.internal.ServiceUtil;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.widgets.xBranch.BranchView;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * @author Donald G. Dunne
 */
public class XResultBrowserListener implements LocationListener {

   public XResultBrowserListener() {
      super();
   }

   @Override
   public void changing(LocationEvent event) {
      try {
         String location = event.location;
         if (location.contains("javascript:print")) {
            return;
         }
         String cmdStr = location.replaceFirst("about:blank", "");
         cmdStr = cmdStr.replaceFirst("blank", "");
         XResultBrowserHyperCmd xResultBrowserHyperCmd = XResultBrowserHyperCmd.getCmdStrHyperCmd(cmdStr);
         String value = XResultBrowserHyperCmd.getCmdStrValue(cmdStr);
         switch (xResultBrowserHyperCmd) {
            case openAction:
               event.doit = false;
               openArtifact(value, OseeCmEditor.CmPcrEditor);
               break;
            case openArtifactBranch:
               event.doit = false;
               try {
                  java.util.regex.Matcher m = Pattern.compile("^(.*?)\\((.*?)\\)$").matcher(value);
                  if (m.find()) {
                     String guid = m.group(1);
                     Integer branchId = Integer.parseInt(m.group(2));
                     Artifact artifact = ArtifactQuery.getArtifactFromId(guid, BranchManager.getBranch(branchId));
                     RendererManager.openInJob(artifact, PresentationType.DEFAULT_OPEN);
                  }
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
               break;

            case openArtifactEditor:
               event.doit = false;
               openArtifact(value, OseeCmEditor.ArtifactEditor);
               break;
            case openBranch:
               event.doit = false;
               int branchId = new Integer(value);
               Branch branch = BranchManager.getBranch(branchId);
               BranchView.revealBranch(branch);
               break;
            case browserInternal:
               event.doit = false;
               IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
               try {
                  IWebBrowser browser = browserSupport.createBrowser("osee.ats.navigator.browser");
                  browser.openURL(new URL(value));
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
               break;
            case browserExternal:
               event.doit = false;
               Program.launch(value);
               break;
            default:
               break;
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Can't process hyperlink.", ex);
      }
   }

   private void openArtifact(String guid, OseeCmEditor view) {
      try {
         IOseeCmService cmService = ServiceUtil.getOseeCmService();
         cmService.openArtifact(guid, view);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   @Override
   public void changed(LocationEvent event) {
      // do nothing
   }

}
