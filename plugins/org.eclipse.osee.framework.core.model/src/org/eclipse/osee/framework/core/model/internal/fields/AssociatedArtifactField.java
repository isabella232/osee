/*********************************************************************
 * Copyright (c) 2009 Boeing
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

package org.eclipse.osee.framework.core.model.internal.fields;

import org.eclipse.osee.framework.core.model.AbstractOseeField;

/**
 * @author Roberto E. Escobar
 */
public class AssociatedArtifactField extends AbstractOseeField<Long> {

   private Long artId;

   public AssociatedArtifactField(Long artId) {
      super();
      this.artId = artId;
   }

   @Override
   public Long get() {
      return artId;
   }

   @Override
   public void set(Long artId) {
      boolean wasDifferent = isDifferent(get(), artId);
      if (wasDifferent) {
         this.artId = artId;
      }
      isDirty |= wasDifferent;
   }

   private boolean isDifferent(Long artId1, Long artId2) {
      if (artId1 == null) {
         if (artId2 == null) {
            return false;
         } else {
            return true;
         }
      } else {
         if (artId2 == null) {
            return true;
         } else {
            return !(artId1.equals(artId2));
         }
      }
   }
}