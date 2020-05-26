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

package org.eclipse.osee.framework.ui.skynet;

import java.util.Date;
import java.util.GregorianCalendar;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.attribute.BinaryAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.BooleanAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.DateAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.EnumeratedAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.WordAttribute;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.widgets.cellEditor.DateValue;
import org.eclipse.osee.framework.ui.skynet.widgets.cellEditor.EnumeratedValue;
import org.eclipse.osee.framework.ui.skynet.widgets.cellEditor.StringValue;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.widgets.Item;

/**
 * @author Ryan D. Brooks
 */
public class AttributeCellModifier implements ICellModifier {
   private final TableViewer tableViewer;
   private final DateValue dateValue;
   private final EnumeratedValue enumeratedValue;
   private final StringValue stringValue;
   private final IDirtiableEditor editor;

   private final AttributesComposite attrComp;

   public AttributeCellModifier(IDirtiableEditor editor, TableViewer tableViewer, AttributesComposite attrComp) {
      super();
      this.tableViewer = tableViewer;
      this.attrComp = attrComp;
      this.dateValue = new DateValue();
      this.enumeratedValue = new EnumeratedValue();
      this.stringValue = new StringValue();
      this.editor = editor;

      // this.pList = new PermissionList();
      // pList.addPermission(Permission.PermissionEnum.EDITREQUIREMENT);
   }

   @Override
   public boolean canModify(Object element, String property) {
      attrComp.updateLabel("");
      if (element != null) {
         if (element instanceof Item) {
            element = ((Item) element).getData();
         }
         try {
            Attribute<?> attribute = (Attribute<?>) element;

            if (attribute instanceof WordAttribute) {
               return false;
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
      return property.equals("value");
   }

   @Override
   public Object getValue(Object element, String property) {
      try {
         Attribute<?> attribute = (Attribute<?>) element;
         Object object = attribute.getValue();
         if (attribute instanceof EnumeratedAttribute) {
            enumeratedValue.setValue(attribute.getDisplayableString());

            String[] choices =
               AttributeTypeManager.getEnumerationValues(attribute.getAttributeType()).toArray(new String[0]);
            enumeratedValue.setChoices(choices);
            return enumeratedValue;
         } else if (attribute instanceof BooleanAttribute) {
            enumeratedValue.setValue(attribute.getDisplayableString());
            enumeratedValue.setChoices(BooleanAttribute.booleanChoices);
            return enumeratedValue;
         } else if (attribute instanceof DateAttribute) {
            dateValue.setValue((Date) object);
            return dateValue;
         } else {
            stringValue.setValue(attribute.getDisplayableString() != null ? attribute.getDisplayableString() : "");
            return stringValue;
         }
      } catch (OseeCoreException ex) {
         return Lib.exceptionToString(ex);
      }
   }

   @Override
   public void modify(Object element, String property, Object value) {
      if (element != null) {
         // Note that it is possible for an SWT Item to be passed instead of the model element.
         if (element instanceof Item) {
            element = ((Item) element).getData();
         }
         try {
            Attribute<?> attribute = (Attribute<?>) element;

            if (attribute instanceof DateAttribute) {
               if (value instanceof GregorianCalendar) {
                  ((DateAttribute) attribute).setValue(new Date(((GregorianCalendar) value).getTimeInMillis()));
               } else {
                  ((DateAttribute) attribute).setValue((Date) value);
               }
            } else if (!(attribute instanceof BinaryAttribute<?>)) {
               //binary attributes should not be changed.
               attribute.setFromString((String) value);
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
         tableViewer.update(element, null);
         editor.onDirtied();
         attrComp.notifyModifyAttribuesListeners();
      }
   }
}
