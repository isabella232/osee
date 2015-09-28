/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.workdef;

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

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.workdef.IAtsCompositeLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workdef.WidgetOption;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.editor.stateItem.AtsStateItemManager;
import org.eclipse.osee.ats.editor.stateItem.IAtsStateItem;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.widgets.commit.XCommitManager;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.IArtifactWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XOption;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IDynamicWidgetLayoutListener;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IXWidgetOptionResolver;
import org.eclipse.osee.framework.ui.skynet.widgets.util.SwtXWidgetRenderer;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetRendererItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.w3c.dom.Element;

/**
 * Instantiation of a StateXWidgetPage for a given StateDefinition to provide for automatic creation and management of
 * the XWidgets
 * 
 * @author Donald G. Dunne
 */
public class StateXWidgetPage implements IDynamicWidgetLayoutListener, IStateToken {

   private static final Pair<IStatus, XWidget> OK_PAIR = new Pair<>(Status.OK_STATUS, null);
   protected SwtXWidgetRenderer dynamicXWidgetLayout;
   protected final IAtsStateDefinition stateDefinition;
   protected final IAtsWorkDefinition workDefinition;
   private AbstractWorkflowArtifact sma;

   public StateXWidgetPage(IAtsWorkDefinition workDefinition, IAtsStateDefinition stateDefinition, IXWidgetOptionResolver optionResolver, IDynamicWidgetLayoutListener dynamicWidgetLayoutListener) {
      this.workDefinition = workDefinition;
      this.stateDefinition = stateDefinition;
      if (dynamicWidgetLayoutListener == null) {
         dynamicXWidgetLayout = new SwtXWidgetRenderer(this, optionResolver);
      } else {
         dynamicXWidgetLayout = new SwtXWidgetRenderer(dynamicWidgetLayoutListener, optionResolver);
      }
   }

   public StateXWidgetPage(IAtsWorkDefinition workFlowDefinition, IAtsStateDefinition stateDefinition, String xWidgetsXml, IXWidgetOptionResolver optionResolver) {
      this(workFlowDefinition, stateDefinition, xWidgetsXml, optionResolver, null);
   }

   /**
    * @param instructionLines input lines of WorkAttribute declarations
    */
   public StateXWidgetPage(IAtsWorkDefinition workDefinition, IAtsStateDefinition stateDefinition, String xWidgetsXml, IXWidgetOptionResolver optionResolver, IDynamicWidgetLayoutListener dynamicWidgetLayoutListener) {
      this(workDefinition, stateDefinition, optionResolver, dynamicWidgetLayoutListener);
      try {
         if (xWidgetsXml != null) {
            processXmlLayoutDatas(xWidgetsXml);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, "Error processing attributes", ex);
      }
   }

   public StateXWidgetPage(IAtsWorkDefinition workDefinition, IAtsStateDefinition stateDefinition, List<XWidgetRendererItem> datas, IXWidgetOptionResolver optionResolver, IDynamicWidgetLayoutListener dynamicWidgetLayoutListener) {
      this(workDefinition, stateDefinition, optionResolver, dynamicWidgetLayoutListener);
      dynamicXWidgetLayout.setLayoutDatas(datas);
   }

   public StateXWidgetPage(List<XWidgetRendererItem> datas, IXWidgetOptionResolver optionResolver, IDynamicWidgetLayoutListener dynamicWidgetLayoutListener) {
      this(null, null, datas, optionResolver, dynamicWidgetLayoutListener);
   }

   public StateXWidgetPage(List<XWidgetRendererItem> datas, IXWidgetOptionResolver optionResolver) {
      this(null, null, datas, optionResolver, null);
   }

   public StateXWidgetPage(String xWidgetsXml, IXWidgetOptionResolver optionResolver) {
      this(null, null, xWidgetsXml, optionResolver, null);
   }

   public StateXWidgetPage(IXWidgetOptionResolver optionResolver) {
      this(null, null, (String) null, optionResolver, null);
   }

   @Override
   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, SwtXWidgetRenderer dynamicXWidgetLayout, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      widgetCreated(xWidget, toolkit, art, stateDefinition, xModListener, isEditable);
   }

   @Override
   public void widgetCreating(XWidget xWidget, FormToolkit toolkit, Artifact art, SwtXWidgetRenderer dynamicXWidgetLayout, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      widgetCreating(xWidget, toolkit, art, stateDefinition, xModListener, isEditable);
   }

   public void dispose() {
      try {
         for (XWidgetRendererItem layoutData : getlayoutDatas()) {
            layoutData.getXWidget().dispose();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof StateXWidgetPage) {
         return getName().equals(((StateXWidgetPage) obj).getName());
      }
      return false;
   }

   public SwtXWidgetRenderer createBody(IManagedForm managedForm, Composite parent, Artifact artifact, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      dynamicXWidgetLayout.createBody(managedForm, parent, artifact, xModListener, isEditable);
      return dynamicXWidgetLayout;
   }

   public Pair<IStatus, XWidget> isPageComplete() {
      try {
         for (XWidgetRendererItem layoutData : dynamicXWidgetLayout.getLayoutDatas()) {
            if (!layoutData.getXWidget().isValid().isOK()) {
               // Check to see if widget is part of a completed OR or XOR group
               if (!dynamicXWidgetLayout.isOrGroupFromAttrNameComplete(layoutData.getStoreName()) && !dynamicXWidgetLayout.isXOrGroupFromAttrNameComplete(layoutData.getStoreName())) {
                  return new Pair<IStatus, XWidget>(layoutData.getXWidget().isValid(), layoutData.getXWidget());
               }
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return OK_PAIR;
   }

   public String getHtml(String backgroundColor) throws OseeCoreException {
      return getHtml(backgroundColor, "", "");
   }

   public String getHtml(String backgroundColor, String preHtml, String postHtml) throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      sb.append(AHTML.startBorderTable(100, backgroundColor, getName()));
      if (preHtml != null) {
         sb.append(preHtml);
      }
      for (XWidgetRendererItem layoutData : dynamicXWidgetLayout.getLayoutDatas()) {
         XWidget xWidget = layoutData.getXWidget();
         if (xWidget instanceof IArtifactWidget) {
            ((IArtifactWidget) xWidget).setArtifact(layoutData.getArtifact());
         }
         sb.append(layoutData.getXWidget().toHTML(AHTML.LABEL_FONT));
         sb.append(AHTML.newline());
      }
      if (postHtml != null) {
         sb.append(postHtml);
      }
      sb.append(AHTML.endBorderTable());
      return sb.toString();
   }

   @Override
   public String toString() {
      StringBuffer sb =
         new StringBuffer(
            stateDefinition.getName() + (stateDefinition.getName() != null ? " (" + stateDefinition.getName() + ") " : "") + "\n");
      try {
         for (IAtsStateDefinition page : stateDefinition.getToStates()) {
            sb.append("-> " + page.getName() + (stateDefinition.getOverrideAttributeValidationStates().contains(page) ? " (return)" : "") + "\n");
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return sb.toString();
   }

   private Set<XWidgetRendererItem> getlayoutDatas() {
      return dynamicXWidgetLayout.getLayoutDatas();
   }

   private void addLayoutData(XWidgetRendererItem data) {
      dynamicXWidgetLayout.addWorkLayoutData(data);
   }

   public XWidgetRendererItem getLayoutData(String layoutName) {
      return dynamicXWidgetLayout.getLayoutData(layoutName);
   }

   protected void processXmlLayoutDatas(String xWidgetXml) throws OseeCoreException {
      dynamicXWidgetLayout.processlayoutDatas(xWidgetXml);
   }

   protected void processLayoutDatas(Element element) throws OseeCoreException {
      dynamicXWidgetLayout.processLayoutDatas(element);
   }

   @Override
   public String getName() {
      return stateDefinition.getName();
   }

   @Override
   public StateType getStateType() {
      return stateDefinition.getStateType();
   }

   public String getFullName() {
      return stateDefinition.getFullName();
   }

   public List<IAtsStateDefinition> getToPages() {
      return stateDefinition.getToStates();
   }

   public IAtsStateDefinition getDefaultToPage() {
      if (stateDefinition.getDefaultToState() != null) {
         return stateDefinition.getDefaultToState();
      }
      return null;
   }

   public IAtsStateDefinition getStateDefinition() {
      return stateDefinition;
   }

   public IAtsWorkDefinition getWorkDefinition() {
      return workDefinition;
   }

   public SwtXWidgetRenderer getDynamicXWidgetLayout() {
      return dynamicXWidgetLayout;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   @Override
   public String getDescription() {
      return null;
   }

   public AbstractWorkflowArtifact getSma() {
      return sma;
   }

   public void setsma(AbstractWorkflowArtifact sma) {
      this.sma = sma;
   }

   public boolean isCurrentState(AbstractWorkflowArtifact sma) {
      return sma.isInState(this);
   }

   public boolean isCurrentNonCompleteCancelledState(AbstractWorkflowArtifact sma) {
      return isCurrentState(sma) && !getStateType().isCompletedOrCancelledState();
   }

   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, IAtsStateDefinition stateDef, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      // Check extension points for page creation
      if (sma != null) {
         for (IAtsStateItem item : AtsStateItemManager.getStateItems()) {
            item.xWidgetCreated(xWidget, toolkit, stateDef, art, isEditable);
         }
      }
   }

   @Override
   public void createXWidgetLayoutData(XWidgetRendererItem layoutData, XWidget xWidget, FormToolkit toolkit, Artifact art, XModifiedListener xModListener, boolean isEditable) {

      // If no tool tip, add global tool tip
      if (!Strings.isValid(xWidget.getToolTip())) {
         String description = "";
         if (layoutData.getXWidgetName().equals(XCommitManager.WIDGET_NAME)) {
            description = XCommitManager.DESCRIPTION;
         }
         IAttributeType type = AtsAttributeTypes.getTypeByName(layoutData.getStoreName());
         if (type != null && Strings.isValid(type.getDescription())) {
            description = type.getDescription();
         }
         if (Strings.isValid(description)) {
            xWidget.setToolTip(description);
            layoutData.setToolTip(description);
         }
      }
      // Store workAttr in control for use by help
      if (xWidget.getControl() != null) {
         xWidget.getControl().setData(layoutData);
      }

   }

   public void widgetCreating(XWidget xWidget, FormToolkit toolkit, Artifact art, IAtsStateDefinition stateDefinition, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      // Check extension points for page creation
      if (sma != null) {
         for (IAtsStateItem item : AtsStateItemManager.getStateItems()) {
            Result result = item.xWidgetCreating(xWidget, toolkit, stateDefinition, art, isEditable);
            if (result.isFalse()) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Error in page creation => " + result.getText());
            }
         }
      }
   }

   public void generateLayoutDatas(AbstractWorkflowArtifact sma) {
      this.sma = sma;
      // Add static layoutDatas to statePage
      for (IAtsLayoutItem stateItem : stateDefinition.getLayoutItems()) {
         if (stateItem instanceof IAtsWidgetDefinition) {
            processWidgetDefinition((IAtsWidgetDefinition) stateItem, sma);
         } else if (stateItem instanceof IAtsCompositeLayoutItem) {
            processComposite((IAtsCompositeLayoutItem) stateItem, sma);
         }
      }
   }

   private void processComposite(IAtsCompositeLayoutItem compositeStateItem, AbstractWorkflowArtifact sma) {
      boolean firstWidget = true;
      List<IAtsLayoutItem> stateItems = compositeStateItem.getaLayoutItems();
      for (int x = 0; x < stateItems.size(); x++) {
         boolean lastWidget = x == stateItems.size() - 1;
         IAtsLayoutItem stateItem = stateItems.get(x);
         if (stateItem instanceof IAtsWidgetDefinition) {
            XWidgetRendererItem data = processWidgetDefinition((IAtsWidgetDefinition) stateItem, sma);
            if (firstWidget) {
               if (compositeStateItem.getNumColumns() > 0) {
                  data.setBeginComposite(compositeStateItem.getNumColumns());
               }
            }
            if (lastWidget) {
               data.setEndComposite(true);
            }
         } else if (stateItem instanceof IAtsCompositeLayoutItem) {
            processComposite((IAtsCompositeLayoutItem) stateItem, sma);
         }
         firstWidget = false;
      }
   }

   /**
    * TODO This will eventually go away and ATS pages will be generated straight from WidgetDefinitions.
    */
   private XWidgetRendererItem processWidgetDefinition(IAtsWidgetDefinition widgetDef, AbstractWorkflowArtifact sma) {
      XWidgetRendererItem data = new XWidgetRendererItem(getDynamicXWidgetLayout());
      data.setDefaultValue(widgetDef.getDefaultValue());
      data.setHeight(widgetDef.getHeight());
      data.setStoreName(widgetDef.getAtrributeName());
      data.setToolTip(widgetDef.getToolTip());
      data.setId(widgetDef.getName());
      data.setXWidgetName(widgetDef.getXWidgetName());
      data.setArtifact(sma);
      data.setName(widgetDef.getName());
      data.setObject(widgetDef);
      if (widgetDef.is(WidgetOption.REQUIRED_FOR_TRANSITION)) {
         data.getXOptionHandler().add(XOption.REQUIRED);
      } else if (widgetDef.is(WidgetOption.REQUIRED_FOR_COMPLETION)) {
         data.getXOptionHandler().add(XOption.REQUIRED_FOR_COMPLETION);
      }
      for (WidgetOption widgetOpt : widgetDef.getOptions().getXOptions()) {
         XOption option = null;
         try {
            option = XOption.valueOf(widgetOpt.name());
         } catch (IllegalArgumentException ex) {
            // do nothing
         }
         if (option != null) {
            data.getXOptionHandler().add(option);
         }
      }
      addLayoutData(data);
      return data;
   }

}
