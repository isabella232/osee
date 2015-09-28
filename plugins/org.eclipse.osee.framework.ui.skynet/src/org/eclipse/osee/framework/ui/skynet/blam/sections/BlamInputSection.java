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
package org.eclipse.osee.framework.ui.skynet.blam.sections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidgetUtility;
import org.eclipse.osee.framework.ui.skynet.widgets.util.DefaultXWidgetOptionResolver;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetPage;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetRendererItem;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Roberto E. Escobar
 */
public class BlamInputSection extends BaseBlamSection {
   private final Collection<XWidgetRendererItem> dynamicInputLayouts = new ArrayList<>();

   public BlamInputSection(FormEditor editor, AbstractBlam abstractBlam, Composite parent, FormToolkit toolkit, int style) {
      super(editor, abstractBlam, parent, toolkit, style);
   }

   @Override
   public void initialize(IManagedForm form) {
      super.initialize(form);
      Section section = getSection();
      section.setText("Parameters");
      section.setLayout(new GridLayout());
      section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

      updateDataPart();
   }

   public VariableMap getData() {
      VariableMap blamVariableMap = new VariableMap();
      List<XWidget> xWidgets = XWidgetUtility.findXWidgetsInControl(getSection());
      for (XWidget xWidget : xWidgets) {
         blamVariableMap.setValue(xWidget.getLabel(), xWidget.getData());
      }
      return blamVariableMap;
   }

   private void updateDataPart() {
      final IManagedForm form = getManagedForm();
      final FormToolkit toolkit = form.getToolkit();
      final Section section = getSection();

      Control control = section.getClient();
      if (Widgets.isAccessible(control)) {
         control.dispose();
      }
      Composite sectionBody = toolkit.createComposite(section, toolkit.getBorderStyle());
      sectionBody.setLayout(new GridLayout());
      sectionBody.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

      createWidgets(sectionBody);

      try {
         getAbstractBlam().addWidgets(getManagedForm(), getEditor(), sectionBody);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

      section.setClient(sectionBody);
      toolkit.paintBordersFor(section);

      section.layout(true);
      form.getForm().getBody().layout(true);
   }

   private void createWidgets(Composite parent) {
      try {
         List<XWidgetRendererItem> layoutDatas = getDynamicXWidgetLayouts();
         XWidgetPage workPage = new XWidgetPage(layoutDatas, new DefaultXWidgetOptionResolver(), getAbstractBlam());
         workPage.createBody(getManagedForm(), parent, null, null, true);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   private List<XWidgetRendererItem> getDynamicXWidgetLayouts() throws Exception {
      List<XWidgetRendererItem> itemsToReturn = new ArrayList<>();
      itemsToReturn.addAll(getAbstractBlam().getLayoutDatas());
      itemsToReturn.addAll(dynamicInputLayouts);
      return itemsToReturn;
   }

   @Override
   public void refresh() {
      super.refresh();
   }
}
