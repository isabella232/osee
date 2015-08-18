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

import java.util.Collection;
import java.util.concurrent.Callable;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.ats.world.search.WorldSearchItem;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IDynamicWidgetLayoutListener;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IXWidgetOptionResolver;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetRendererItem;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

/**
 * @author Donald G. Dunne
 */
public abstract class WorldEditorParameterSearchItem extends WorldSearchItem implements IWorldEditorParameterProvider, IDynamicWidgetLayoutListener, IXWidgetOptionResolver {

   private CustomizeData customizeData;
   private TableLoadOption[] tableLoadOptions;

   public WorldEditorParameterSearchItem(String name, KeyedImage oseeImage) {
      super(name, LoadView.WorldEditor, oseeImage);
   }

   public WorldEditorParameterSearchItem(String name, LoadView loadView) {
      this(name, loadView, null);
   }

   public WorldEditorParameterSearchItem(String name, LoadView loadView, KeyedImage oseeImage) {
      super(name, loadView, oseeImage);
   }

   public WorldEditorParameterSearchItem(WorldSearchItem worldSearchItem) {
      this(worldSearchItem, null);
   }

   public WorldEditorParameterSearchItem(WorldSearchItem worldSearchItem, KeyedImage oseeImage) {
      super(worldSearchItem, oseeImage);
   }

   @Override
   public abstract String getParameterXWidgetXml() throws OseeCoreException;

   public abstract Result isParameterSelectionValid() throws OseeCoreException;

   @Override
   public void run(WorldEditor worldEditor, SearchType searchType, boolean forcePend) {
      worldEditor.getWorldComposite().getXViewer().setForcePend(forcePend);
   }

   @Override
   public String[] getWidgetOptions(XWidgetRendererItem widgetData) {
      return null;
   }

   @Override
   public void setCustomizeData(CustomizeData customizeData) {
      this.customizeData = customizeData;
   }

   @Override
   public void setTableLoadOptions(TableLoadOption... tableLoadOptions) {
      this.tableLoadOptions = tableLoadOptions;
   }

   public CustomizeData getCustomizeData() {
      return customizeData;
   }

   public TableLoadOption[] getTableLoadOptions() {
      return tableLoadOptions;
   }

   @Override
   public void handleSaveButtonPressed() {
      // do nothing
   }

   @Override
   public boolean isSaveButtonAvailable() {
      return false;
   }

   public abstract Callable<Collection<? extends Artifact>> createSearch() throws OseeCoreException;

}
