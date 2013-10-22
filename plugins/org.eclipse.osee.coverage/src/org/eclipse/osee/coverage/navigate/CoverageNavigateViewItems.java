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
package org.eclipse.osee.coverage.navigate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.coverage.CoverageManager;
import org.eclipse.osee.coverage.action.ConfigureCoverageMethodsAction;
import org.eclipse.osee.coverage.action.DeleteCoveragePackageAction;
import org.eclipse.osee.coverage.action.NewCoveragePackageAction;
import org.eclipse.osee.coverage.action.OpenCoveragePackageAction;
import org.eclipse.osee.coverage.blam.AbstractCoverageBlam;
import org.eclipse.osee.coverage.dispo.ImportCoverageMethodsBlam;
import org.eclipse.osee.coverage.util.CoverageImage;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionPoints;
import org.eclipse.osee.framework.skynet.core.SystemGroup;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateViewItems;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.action.CompareTwoStringsAction;
import org.eclipse.osee.framework.ui.skynet.widgets.xnavigate.XNavigateItemBlam;
import org.osgi.framework.Bundle;

/**
 * @author Donald G. Dunne
 */
public class CoverageNavigateViewItems implements XNavigateViewItems {

   public CoverageNavigateViewItems() {
      super();
   }

   @Override
   public List<XNavigateItem> getSearchNavigateItems() {
      List<XNavigateItem> items = new ArrayList<XNavigateItem>();
      addExtensionPointItems(items);
      return items;
   }

   private void addExtensionPointItems(List<XNavigateItem> items) {
      items.add(new XNavigateItemAction(null, new NewCoveragePackageAction(), NewCoveragePackageAction.OSEE_IMAGE));
      items.add(new XNavigateItemAction(null, new OpenCoveragePackageAction(), OpenCoveragePackageAction.OSEE_IMAGE));
      items.add(new XNavigateItemAction(null, new DeleteCoveragePackageAction(), DeleteCoveragePackageAction.OSEE_IMAGE));
      items.add(new XNavigateItemAction(null, new ConfigureCoverageMethodsAction(),
         ConfigureCoverageMethodsAction.OSEE_IMAGE));
      items.add(new XNavigateItemAction(null, new CompareTwoStringsAction(), FrameworkImage.EDIT));
      items.add(new XNavigateItemBlam(null, new ImportCoverageMethodsBlam(), CoverageImage.COVERAGE_IMPORT));

      try {
         if (SystemGroup.OseeAdmin.isCurrentUserMember()) {
            for (ICoverageNavigateItem navigateItem : getExtensionPointNavigateItems()) {
               try {
                  items.addAll(navigateItem.getNavigateItems());
               } catch (Throwable th) {
                  OseeLog.log(CoverageNavigateViewItems.class, Level.SEVERE, th);
               }
            }

            for (AbstractCoverageBlam blam : CoverageManager.getCoverageBlams()) {
               if (!blam.getName().startsWith("Test Import")) {
                  items.add(new XNavigateItemBlam(null, blam));
               }
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(CoverageNavigateViewItems.class, Level.SEVERE, ex);
      }
      if (CoverageUtil.isAdmin()) {
         items.add(new DoesNotWorkItemCoverage());
         items.add(new CreateTestCoverageUnits());
      }
   }

   private List<ICoverageNavigateItem> getExtensionPointNavigateItems() throws OseeCoreException {
      List<ICoverageNavigateItem> data = new ArrayList<ICoverageNavigateItem>();
      List<IConfigurationElement> elements =
         ExtensionPoints.getExtensionElements("org.eclipse.osee.coverage.CoverageNavigateItem", "ICoverageNavigateItem");
      for (IConfigurationElement element : elements) {
         String className = element.getAttribute("classname");
         String bundleName = element.getContributor().getName();

         if (Strings.isValid(bundleName) && Strings.isValid(className)) {
            try {
               Bundle bundle = Platform.getBundle(bundleName);
               Class<?> taskClass = bundle.loadClass(className);
               Object object;
               try {
                  Method getInstance = taskClass.getMethod("getInstance", new Class[] {});
                  object = getInstance.invoke(null, new Object[] {});
               } catch (Exception ex) {
                  object = taskClass.newInstance();
               }
               data.add((ICoverageNavigateItem) object);
            } catch (Exception ex) {
               OseeExceptions.wrapAndThrow(ex);
            }
         }
      }
      return data;
   }
}
