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
package org.eclipse.osee.framework.ui.skynet.util;

import java.util.logging.Level;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.OseeInfo;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author Jeff C. Phillips
 */
public class SkynetViews {

   private static final String MEMENTO_SOURCE_GUID = "sourceDbGuid";

   public static boolean isSourceValid(IMemento memento) throws OseeCoreException {
      boolean result = false;
      if (memento != null) {
         String dbId = memento.getString(MEMENTO_SOURCE_GUID);
         if (Strings.isValid(dbId)) {
            String currentDbId = null;
            try {
               currentDbId = OseeInfo.getDatabaseGuid();
            } catch (OseeDataStoreException ex) {
               OseeLog.log(Activator.class, Level.WARNING, "Unable to set memento source db guid");
            }
            if (dbId.equals(currentDbId)) {
               result = true;
            }
         }
      }
      return result;
   }

   public static void addDatabaseSourceId(IMemento memento) {
      if (memento != null) {
         try {
            memento.putString(MEMENTO_SOURCE_GUID, OseeInfo.getDatabaseGuid());
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.WARNING, "Unable to set memento source db guid");
         }
      }
   }

   public static void closeView(final String viewId, final String secondaryId) {
      if (Strings.isValid(viewId)) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               IWorkbench workbench = PlatformUI.getWorkbench();
               if (workbench != null) {
                  IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
                  if (workbenchWindow != null) {
                     IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
                     if (workbenchPage != null) {
                        workbenchPage.hideView(workbenchPage.findViewReference(viewId, secondaryId));
                     }
                  }
               }
            }
         });
      }
   }
}