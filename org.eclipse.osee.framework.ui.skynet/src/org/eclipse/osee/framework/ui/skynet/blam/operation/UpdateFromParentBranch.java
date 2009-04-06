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
package org.eclipse.osee.framework.ui.skynet.blam.operation;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactBaselineUpdate;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;

/**
 * @author Ryan D. Brooks
 */
public class UpdateFromParentBranch extends AbstractBlam {
   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.AbstractBlam#getName()
    */
   @Override
   public String getName() {
      return "Update From ParentBranch";
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#runOperation(org.eclipse.osee.framework.ui.skynet.blam.VariableMap, org.eclipse.osee.framework.skynet.core.artifact.Branch, org.eclipse.core.runtime.IProgressMonitor)
    */
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      monitor.beginTask("Update From Parent Branch", IProgressMonitor.UNKNOWN);
      Branch childBranch = variableMap.getBranch("Child Branch Name");
      ArtifactBaselineUpdate.updateArtifacts(childBranch, variableMap.getArtifacts("Parent Branch Artifacts to update to Child Branch"), childBranch.getParentBranch());
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#getXWidgetXml()
    */
   @Override
   public String getXWidgetsXml() {
      return "<xWidgets><XWidget xwidgetType=\"XBranchSelectWidget\" displayName=\"Child Branch Name\" /><XWidget xwidgetType=\"XListDropViewer\" displayName=\"Parent Branch Artifacts to update to Child Branch\" /></xWidgets>";
   }

   public Collection<String> getCategories() {
      return Arrays.asList("Define");
   }
}