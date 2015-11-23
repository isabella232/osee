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
package org.eclipse.osee.ats.world;

import java.util.List;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.task.TaskXViewer;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public interface IAtsWorldEditorItem {

   public List<XViewerColumn> getXViewerColumns() throws OseeCoreException;

   public boolean isXColumnProvider(XViewerColumn xCol) throws OseeCoreException;

   public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException;

   public Color getForeground(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException;

   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException;

   /**
    * Return menu item objects to add to the World Editor pull-down menu only if applicable for the given
    * worldSearchItem
    */
   public List<? extends Action> getWorldEditorMenuActions(IWorldEditorProvider worldEditorProvider, WorldEditor worldEditor) throws OseeCoreException;

   public void updateTaskEditMenuActions(TaskXViewer taskXViewer);

   public default boolean isWorldEditorSearchProviderNamespaceMatch(String namespace) {
      return false;
   }

   public default IAdaptable getNewWorldEditorInputFromNamespace(String namespace, long atsSearchUuid) {
      return null;
   }
}
