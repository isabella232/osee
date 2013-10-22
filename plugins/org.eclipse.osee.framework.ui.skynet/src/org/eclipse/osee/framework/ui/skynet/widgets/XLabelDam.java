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
package org.eclipse.osee.framework.ui.skynet.widgets;

import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Provided to show the contents of an attribute without any ability to edit
 * 
 * @author Donald G. Dunne
 */
public class XLabelDam extends GenericXWidget implements IAttributeWidget {

   protected Artifact artifact;
   protected IAttributeType attributeType;
   protected Text valueTextWidget;
   private Composite parent;

   public XLabelDam(String displayLabel) {
      super(displayLabel);
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public Control getControl() {
      return valueTextWidget;
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      this.parent = parent;
      if (horizontalSpan < 2) {
         horizontalSpan = 2;
      }
      // Create Data Widgets
      if (isDisplayLabel() && !getLabel().equals("")) {
         labelWidget = new Label(parent, SWT.NONE);
         labelWidget.setText(getLabel() + ":");
         if (getToolTip() != null) {
            labelWidget.setToolTipText(getToolTip());
         }
      }
      valueTextWidget = new Text(parent, SWT.NONE);
      valueTextWidget.setEditable(false);
      refresh();
   }

   @Override
   public IAttributeType getAttributeType() {
      return attributeType;
   }

   @Override
   public void setAttributeType(Artifact artifact, IAttributeType attributeType) {
      this.artifact = artifact;
      this.attributeType = attributeType;
      refresh();
   }

   @Override
   public void saveToArtifact() {
      // Do nothing cause labelDam is read-only
   }

   @Override
   public Result isDirty() {
      return Result.FalseResult;
   }

   @Override
   public void refresh() {
      Artifact artifact = getArtifact();
      if (artifact != null && Widgets.isAccessible(valueTextWidget)) {
         try {
            String value = artifact.getAttributesToString(getAttributeType());
            valueTextWidget.setText(value);
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   @Override
   public void dispose() {
      if (labelWidget != null) {
         labelWidget.dispose();
      }
      if (valueTextWidget != null) {
         valueTextWidget.dispose();
      }
      if (parent != null && !parent.isDisposed()) {
         parent.layout();
      }
   }

   @Override
   public Object getData() {
      return valueTextWidget.getText();
   }

   @Override
   public void revert() {
      // Do nothing cause labelDam is read-only
   }

}
