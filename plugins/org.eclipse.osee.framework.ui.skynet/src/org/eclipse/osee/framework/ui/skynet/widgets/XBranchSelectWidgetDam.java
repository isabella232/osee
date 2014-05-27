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

import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;

/**
 * Branch Selection with branch uuid storage as String
 * 
 * @author Donald G. Dunne
 */
public class XBranchSelectWidgetDam extends XBranchSelectWidget implements IAttributeWidget {
   public static final String WIDGET_ID = XBranchSelectWidgetDam.class.getSimpleName();

   private Artifact artifact;
   private IAttributeType attributeType;

   public XBranchSelectWidgetDam() {
      this("Branch");
   }

   public XBranchSelectWidgetDam(String label) {
      super(label);
      addXModifiedListener(new DirtyListener());
   }

   public Long getStoredUuid() throws OseeCoreException {
      long resultUuid = 0;
      String uuidStr = artifact.getSoleAttributeValue(attributeType, null);
      if (Strings.isValid(uuidStr)) {
         resultUuid = Long.valueOf(uuidStr);
      }
      return resultUuid;
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public void saveToArtifact() throws OseeCoreException {
      IOseeBranch selection = getSelection();
      if (selection == null) {
         artifact.deleteAttributes(attributeType);
      } else {
         artifact.setSoleAttributeValue(attributeType, String.valueOf(selection.getUuid()));
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
            IOseeBranch widgetInput = getSelection();
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
   public void setAttributeType(Artifact artifact, IAttributeType attributeTypeName) throws OseeCoreException {
      setLabel(attributeTypeName.getUnqualifiedName());
      this.artifact = artifact;
      this.attributeType = attributeTypeName;
      Long storedUuid = getStoredUuid();
      if (storedUuid != null && storedUuid > 0) {
         IOseeBranch branch = BranchManager.getBranchByUuid(storedUuid);
         if (branch != null) {
            setSelection(branch);
         }
      }
   }

   @Override
   public IAttributeType getAttributeType() {
      return attributeType;
   }

   private class DirtyListener implements XModifiedListener {
      @Override
      public void widgetModified(XWidget widget) {
         isDirty();
      }
   }

}
