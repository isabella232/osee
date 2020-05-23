/*********************************************************************
 * Copyright (c) 2020 Boeing
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

package org.eclipse.osee.ats.api.task.create;

/**
 * @author Donald G. Dunne
 */
public class ArtifactIncluded {

   private boolean included;
   private boolean deleted;

   public ArtifactIncluded() {
      // for jax-rs
   }

   public ArtifactIncluded(boolean included, boolean deleted) {
      this.included = included;
      this.deleted = deleted;
   }

   public boolean isIncluded() {
      return included;
   }

   public boolean isNotIncluded() {
      return !included;
   }

   public void setIncluded(boolean included) {
      this.included = included;
   }

   public boolean isDeleted() {
      return deleted;
   }

   public boolean isNotDeleted() {
      return !deleted;
   }

   public void setDeleted(boolean deleted) {
      this.deleted = deleted;
   }

}
