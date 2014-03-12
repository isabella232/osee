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
 * Branch Selection with branch guid storage as String
 * 
 * @author Donald G. Dunne
 */
public class XBranchSelectByUuidWidgetDam extends XBranchSelectWidget implements IAttributeWidget {
   public static final String WIDGET_ID = XBranchSelectByUuidWidgetDam.class.getSimpleName();

   private Artifact artifact;
   private IAttributeType attributeType;

   public XBranchSelectByUuidWidgetDam() {
      this("Branch");
   }

   public XBranchSelectByUuidWidgetDam(String label) {
      super(label);
      addXModifiedListener(new DirtyListener());
   }

   public String getStoredGuid() throws OseeCoreException {
      return artifact.getSoleAttributeValue(attributeType, "");
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
         artifact.setSoleAttributeValue(attributeType, selection.getGuid());
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
            String storedGuid = getStoredGuid();
            IOseeBranch widgetInput = getSelection();
            String widgetGuid = widgetInput == null ? "" : widgetInput.getGuid();
            if (!storedGuid.equals(widgetGuid)) {
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
      String storedGuid = getStoredGuid();
      if (Strings.isValid(storedGuid)) {
         IOseeBranch branch = BranchManager.getBranchByGuid(storedGuid);
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
