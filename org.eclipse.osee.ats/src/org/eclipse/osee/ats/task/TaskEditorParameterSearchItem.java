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
package org.eclipse.osee.ats.task;

import java.util.Collection;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.world.search.WorldSearchItem;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.ImageManager;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayoutData;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.IDynamicWidgetLayoutListener;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.IXWidgetOptionResolver;

/**
 * @author Donald G. Dunne
 */
public abstract class TaskEditorParameterSearchItem extends WorldSearchItem implements ITaskEditorProvider, IDynamicWidgetLayoutListener, IXWidgetOptionResolver {

   boolean firstTime = true;

   /**
    * @param name
    */
   public TaskEditorParameterSearchItem(String name) {
      super(name, LoadView.TaskEditor, ImageManager.getImage(AtsImage.TASK));
   }

   /**
    * @param worldSearchItem
    */
   public TaskEditorParameterSearchItem(WorldSearchItem worldSearchItem) {
      super(worldSearchItem, ImageManager.getImage(AtsImage.TASK));
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.search.WorldSearchItem#copy()
    */
   @Override
   public WorldSearchItem copy() {
      return null;
   }

   public abstract String getParameterXWidgetXml() throws OseeCoreException;

   public abstract Result isParameterSelectionValid() throws OseeCoreException;

   public abstract Collection<? extends Artifact> getTaskEditorTaskArtifacts() throws OseeCoreException;

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.ITaskEditorParameterProvider#getWidgetOptions(org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayoutData)
    */
   @Override
   public String[] getWidgetOptions(DynamicXWidgetLayoutData widgetData) {
      return null;
   }

   /**
    * @return the firstTime
    */
   public boolean isFirstTime() {
      if (firstTime) {
         firstTime = false;
         return true;
      }
      return firstTime;
   }

}
