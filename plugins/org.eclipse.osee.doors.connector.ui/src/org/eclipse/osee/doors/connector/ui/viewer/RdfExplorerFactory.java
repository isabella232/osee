/*********************************************************************
 * Copyright (c) 2017 Boeing
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

package org.eclipse.osee.doors.connector.ui.viewer;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osee.doors.connector.core.DoorsArtifact;
import org.eclipse.osee.doors.connector.core.Requirement;
import org.eclipse.osee.doors.connector.core.ServiceProvider;
import org.eclipse.osee.doors.connector.core.ServiceProviderCatalog;

/**
 * @author David W. Miller
 */
public class RdfExplorerFactory {

   @SuppressWarnings("unchecked")
   public static <T extends RdfExplorerItem> T getExplorerItem(String name, TreeViewer treeViewer, RdfExplorerItem rootItem, RdfExplorer rdfExplorer, DoorsArtifact item) {
      if (item instanceof ServiceProvider) {
         return (T) new RdfSevPro(name, treeViewer, rootItem, rdfExplorer, item);
      } else if (item instanceof ServiceProviderCatalog) {
         return (T) new RdfSevProCat(name, treeViewer, rootItem, rdfExplorer, item);
      } else if (item instanceof Requirement) {
         return (T) new RdfRequirement(name, treeViewer, rootItem, rdfExplorer, item);
      }
      return (T) new RdfExplorerItem(name, treeViewer, rootItem, rdfExplorer, item);
   }
}
