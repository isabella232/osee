/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.ui.skynet.widgets.cellEditor;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * @author Ryan D. Brooks
 */
public class StringValue extends UniversalCellEditorValue {
   private String value;

   public StringValue() {
      super();
   }

   @Override
   public Control prepareControl(UniversalCellEditor universalEditor) {
      Text textBox = universalEditor.getStringControl();
      if (value != null) {
         textBox.setText(value);
      }
      return textBox;
   }

   public void setValue(String value) {
      this.value = value;
   }
}