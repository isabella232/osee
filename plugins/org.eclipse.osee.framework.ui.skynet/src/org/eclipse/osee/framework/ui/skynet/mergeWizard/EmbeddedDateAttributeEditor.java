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

package org.eclipse.osee.framework.ui.skynet.mergeWizard;

import java.util.Collection;
import java.util.Date;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.conflict.AttributeConflict;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.widgets.xmerge.MergeUtility;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Theron Virgin
 */

/*
 * All of the instance of checks are needed to support both artifacts and conflicts. The reason to support both is I
 * created the classes for artifacts so all of the work was already done for them. I then realized that I needed to
 * control the setting of values for conflicts and thus had to call the conflict specific methods instead of simply
 * setting the values.
 */
public class EmbeddedDateAttributeEditor implements IEmbeddedAttributeEditor {

   protected AttributeTypeId attributeType;
   protected String displayName;
   protected Collection<?> attributeHolder;
   protected boolean persist;
   protected EmbeddedDateEditor editor;

   public EmbeddedDateAttributeEditor(String notUsed, Collection<?> attributeHolder, String displayName, AttributeTypeId attributeType, boolean persist) {
      this.attributeType = attributeType;
      this.displayName = displayName;
      this.attributeHolder = attributeHolder;
      this.persist = persist;
   }

   @Override
   public boolean create(Composite composite, GridData gd) {
      if (attributeHolder == null) {
         return false;
      }
      if (attributeHolder.size() < 1) {
         return false;
      }
      Object obj = attributeHolder.iterator().next();
      if (obj instanceof Artifact) {
         String type = ((Artifact) obj).getArtifactTypeName();
         for (Object object : attributeHolder) {
            if (object instanceof Artifact) {
               if (!type.equals(((Artifact) object).getArtifactTypeName())) {
                  AWorkbench.popup("ERROR",
                     "All artifacts must be of the same " + "type when edited in an date editor.");
                  return false;
               }
            } else {
               return false;
            }
         }
      }
      Date date = new Date();
      if (obj instanceof Artifact) {
         try {
            Object object = ((Artifact) obj).getSoleAttributeValue(attributeType);
            if (object instanceof Date) {
               date = (Date) object;
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
      if (obj instanceof AttributeConflict) {
         try {
            Object object = ((AttributeConflict) obj).getMergeObject();
            if (object instanceof Date) {
               date = (Date) object;
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }

      editor = new EmbeddedDateEditor("Edit " + displayName, date);
      editor.createEditor(composite);
      return true;
   }

   @Override
   public void update(Object value) {
      editor.setSelectedDate((Date) value);
   }

   @Override
   public boolean commit() {
      Date selected = editor.getSelectedDate();
      try {
         for (Object object : attributeHolder) {
            if (object instanceof Artifact) {
               if (selected == null) {
                  ((Artifact) object).setSoleAttributeValue(attributeType, "");
               } else {
                  ((Artifact) object).setSoleAttributeValue(attributeType, selected.getTime() + "");
               }
               if (persist) {
                  ((Artifact) object).persist(getClass().getSimpleName());
               }
            }
            if (object instanceof AttributeConflict) {
               if (selected == null) {
                  if (!((AttributeConflict) object).clearValue()) {
                     AWorkbench.popup("Attention", MergeUtility.COMMITED_PROMPT);
                  }
               } else {
                  if (!((AttributeConflict) object).setAttributeValue(selected)) {
                     AWorkbench.popup("Attention", MergeUtility.COMMITED_PROMPT);
                  }
               }
            }
         }
         return true;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      AWorkbench.popup("ERROR", "Could not store the attribute");
      return false;
   }

   @Override
   public boolean canClear() {
      return false;
   }

   @Override
   public boolean canFinish() {
      return true;
   }
}
