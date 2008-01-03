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
package org.eclipse.osee.framework.ui.skynet.widgets.cellEditor;

import java.util.Date;
import org.eclipse.nebula.widgets.datechooser.DateChooserCombo;
import org.eclipse.swt.widgets.Control;

/**
 * @author Ryan D. Brooks
 */
public class DateValue extends UniversalCellEditorValue {
   private Date date;

   /**
    * 
    */
   public DateValue() {
      super();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.jdk.core.swt.universalCellEditor.UniversalCellEditorValue#selectControl(org.eclipse.swt.widgets.Control[])
    */
   @Override
   public Control prepareControl(UniversalCellEditor universalEditor) {
      DateChooserCombo datePicker = universalEditor.getDateControl();
      datePicker.setValue(date);
      return datePicker;
   }

   public void setValue(Date date) {
      this.date = date;
   }
}