/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.widgets;

import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;

/**
 * Branch Selection with branch uuid storage as String
 *
 * @author Donald G. Dunne
 */
public class XBranchSelectByWidgetDam extends XBranchSelectWidget implements IAttributeWidget {
   public static final String WIDGET_ID = XBranchSelectByWidgetDam.class.getSimpleName();

   private Artifact artifact;
   private AttributeTypeToken attributeType;

   public XBranchSelectByWidgetDam() {
      this("Branch");
   }

   public XBranchSelectByWidgetDam(String label) {
      super(label);
      addXModifiedListener(new DirtyListener());
   }

   public Long getStoredUuid() throws OseeCoreException {
      return Long.valueOf(artifact.getSoleAttributeValue(attributeType, ""));
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public void saveToArtifact() throws OseeCoreException {
      BranchId selection = getSelection();
      if (selection == null) {
         artifact.deleteAttributes(attributeType);
      } else {
         artifact.setSoleAttributeValue(attributeType, selection.getIdString());
      }
   }

   @Override
   public void revert() throws OseeCoreException {
      setAttributeType(getArtifact(), getAttributeType());
   }

   @Override
   public Result isDirty() {
      if (isEditable()) {
         try {
            Long storedUuid = getStoredUuid();
            BranchId widgetInput = getSelection();
            Long widgetUuid = widgetInput == null ? 0L : widgetInput.getUuid();
            if (!storedUuid.equals(widgetUuid)) {
               return new Result(true, getAttributeType() + " is dirty");
            }
         } catch (OseeCoreException ex) {
            // Do nothing
         }
      }
      return Result.FalseResult;
   }

   @Override
   public void setAttributeType(Artifact artifact, AttributeTypeToken attributeTypeName) throws OseeCoreException {
      setLabel(attributeTypeName.getUnqualifiedName());
      this.artifact = artifact;
      this.attributeType = attributeTypeName;
      Long storedUuid = getStoredUuid();
      if (storedUuid != null && getStoredUuid() > 0L) {
         setSelection(BranchManager.getBranchToken(storedUuid));
      }
   }

   @Override
   public AttributeTypeToken getAttributeType() {
      return attributeType;
   }

   private class DirtyListener implements XModifiedListener {
      @Override
      public void widgetModified(XWidget widget) {
         isDirty();
      }
   }

}
