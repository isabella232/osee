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
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IDynamicWidgetLayoutListener;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IXWidgetOptionResolver;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetRendererItem;

/**
 * @author Donald G. Dunne
 */
public abstract class TaskEditorParameterSearchItem extends WorldSearchItem implements ITaskEditorProvider, IDynamicWidgetLayoutListener, IXWidgetOptionResolver {

   boolean firstTime = true;

   public TaskEditorParameterSearchItem(String name) {
      super(name, LoadView.TaskEditor, AtsImage.TASK);
   }

   public TaskEditorParameterSearchItem(WorldSearchItem worldSearchItem) {
      super(worldSearchItem, AtsImage.TASK);
   }

   @Override
   public WorldSearchItem copy() {
      return null;
   }

   public abstract String getParameterXWidgetXml() throws OseeCoreException;

   public abstract Result isParameterSelectionValid() throws OseeCoreException;

   @Override
   public abstract Collection<? extends Artifact> getTaskEditorTaskArtifacts() throws OseeCoreException;

   @Override
   public String[] getWidgetOptions(XWidgetRendererItem widgetData) {
      return null;
   }

   public boolean isFirstTime() {
      if (firstTime) {
         firstTime = false;
         return true;
      }
      return firstTime;
   }

}
