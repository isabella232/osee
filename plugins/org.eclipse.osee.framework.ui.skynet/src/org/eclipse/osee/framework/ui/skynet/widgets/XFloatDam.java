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

package org.eclipse.osee.framework.ui.skynet.widgets;

import java.text.NumberFormat;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.exception.AttributeDoesNotExist;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;

/**
 * @author Donald G. Dunne
 */
public class XFloatDam extends XFloat implements IAttributeWidget {

   private Artifact artifact;
   private AttributeTypeToken attributeType;

   public XFloatDam(String displayLabel) {
      super(displayLabel);
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public AttributeTypeToken getAttributeType() {
      return attributeType;
   }

   @Override
   public void setAttributeType(Artifact artifact, AttributeTypeToken attrName) {
      this.artifact = artifact;
      this.attributeType = attrName;
      Double value = artifact.getSoleAttributeValue(getAttributeType(), null);
      super.set(value == null ? "" : NumberFormat.getInstance().format(value));
   }

   @Override
   public void saveToArtifact() {
      try {
         if (!Strings.isValid(text)) {
            getArtifact().deleteSoleAttribute(getAttributeType());
         } else {
            Double enteredValue = getFloat();
            getArtifact().setSoleAttributeValue(getAttributeType(), enteredValue);
         }
      } catch (NumberFormatException ex) {
         // do nothing
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public Result isDirty() {
      if (isEditable()) {
         try {
            Double enteredValue = getFloat();
            Double storedValue = getArtifact().getSoleAttributeValue(getAttributeType());
            if (enteredValue.doubleValue() != storedValue.doubleValue()) {
               return new Result(true, getAttributeType() + " is dirty");
            }
         } catch (AttributeDoesNotExist ex) {
            if (!get().equals("")) {
               return new Result(true, getAttributeType() + " is dirty");
            }
         } catch (NumberFormatException ex) {
            // do nothing
         }
      }
      return Result.FalseResult;
   }

   @Override
   public void revert() {
      setAttributeType(getArtifact(), getAttributeType());
   }
}
