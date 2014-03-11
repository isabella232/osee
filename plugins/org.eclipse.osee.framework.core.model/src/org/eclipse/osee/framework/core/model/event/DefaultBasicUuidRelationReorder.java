/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.model.event;

import org.eclipse.osee.framework.core.data.IRelationType;

/**
 * @author Donald G. Dunne
 */
public class DefaultBasicUuidRelationReorder implements IBasicRelationReorder {

   private DefaultBasicGuidArtifact parentArt;
   private Long branchUuid;
   private Long relTypeUuid;
   private RelationOrderModType modType;

   public DefaultBasicUuidRelationReorder(RelationOrderModType modType, Long branchUuid, Long relTypeUuid, DefaultBasicGuidArtifact artA) {
      this.modType = modType;
      this.branchUuid = branchUuid;
      this.relTypeUuid = relTypeUuid;
      this.parentArt = artA;
   }

   @Override
   public DefaultBasicGuidArtifact getParentArt() {
      return parentArt;
   }

   @Override
   public Long getBranchUuid() {
      return branchUuid;
   }

   @Override
   public Long getRelTypeGuid() {
      return relTypeUuid;
   }

   public void setArtA(DefaultBasicGuidArtifact artA) {
      this.parentArt = artA;
   }

   public void setBranchGuid(Long branchUuid) {
      this.branchUuid = branchUuid;
   }

   public void setRelTypeGuid(Long relTypeGuid) {
      this.relTypeUuid = relTypeGuid;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (parentArt == null ? 0 : parentArt.hashCode());
      result = prime * result + (branchUuid == null ? 0 : branchUuid.hashCode());
      result = prime * result + (relTypeUuid == null ? 0 : relTypeUuid.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      IBasicRelationReorder other = (IBasicRelationReorder) obj;
      if (parentArt == null) {
         if (other.getParentArt() != null) {
            return false;
         }
      } else if (!parentArt.equals(other.getParentArt())) {
         return false;
      }
      if (branchUuid == null) {
         if (other.getBranchUuid() != null) {
            return false;
         }
      } else if (!branchUuid.equals(other.getBranchUuid())) {
         return false;
      }
      if (relTypeUuid == null) {
         if (other.getRelTypeGuid() != null) {
            return false;
         }
      } else if (!relTypeUuid.equals(other.getRelTypeGuid())) {
         return false;
      }
      return true;
   }

   public boolean is(IRelationType... relationTypes) {
      for (IRelationType relType : relationTypes) {
         if (relType.getGuid().equals(getRelTypeGuid())) {
            return true;
         }
      }
      return false;
   }

   @Override
   public RelationOrderModType getModType() {
      return modType;
   }

   public void setModType(RelationOrderModType modType) {
      this.modType = modType;
   }

}
