/*********************************************************************
 * Copyright (c) 2014 Boeing
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

package org.eclipse.osee.ats.api.config;

import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;

/**
 * @author Donald G. Dunne
 */
public class AtsConfiguration {

   private boolean isDefault;
   private String name;
   private ArtifactId id;
   private BranchId branchId;

   public boolean isDefault() {
      return isDefault;
   }

   public void setIsDefault(boolean isDefault) {
      this.isDefault = isDefault;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public ArtifactId getId() {
      return id;
   }

   public void setArtifactId(ArtifactId id) {
      this.id = id;
   }

   public BranchId getBranchId() {
      return branchId;
   }

   public void setBranchId(BranchId branchId) {
      this.branchId = branchId;
   }

   @Override
   public String toString() {
      return name;
   }
}