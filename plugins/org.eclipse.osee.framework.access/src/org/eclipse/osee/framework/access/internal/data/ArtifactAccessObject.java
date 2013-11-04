/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.access.internal.data;

import org.eclipse.osee.framework.access.AccessObject;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.jdk.core.type.DoubleKeyHashMap;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;

/**
 * @author Jeff C. Phillips
 */
public class ArtifactAccessObject extends AccessObject {

   private final Integer artId;
   private final Long branchId;
   private static final DoubleKeyHashMap<Integer, Long, ArtifactAccessObject> cache =
      new DoubleKeyHashMap<Integer, Long, ArtifactAccessObject>();

   public ArtifactAccessObject(Integer artId, Long branchId) {
      super();
      this.artId = artId;
      this.branchId = branchId;
   }

   @Override
   public int hashCode() {
      int result = 17;
      result = 31 * result + artId;
      result = 31 * result + branchId.hashCode();
      return result;
   }

   public Integer getArtId() {
      return artId;
   }

   @Override
   public long getId() {
      return branchId;
   }

   @Override
   public void removeFromCache() {
      cache.remove(artId, branchId);
   }

   @Override
   public void removeFromDatabase(int subjectId) throws OseeCoreException {
      final String DELETE_ARTIFACT_ACL =
         "DELETE FROM OSEE_ARTIFACT_ACL WHERE privilege_entity_id = ? AND art_id =? AND branch_id =?";
      ConnectionHandler.runPreparedUpdate(DELETE_ARTIFACT_ACL, subjectId, artId, branchId);
   }

   public static ArtifactAccessObject getArtifactAccessObject(Artifact artifact) throws OseeCoreException {
      Integer artId = artifact.getArtId();
      IOseeBranch branchId = artifact.getBranch();
      return getArtifactAccessObject(artId, branchId);
   }

   public static ArtifactAccessObject getArtifactAccessObject(Integer artId, IOseeBranch branch) throws OseeCoreException {
      long branchId = BranchManager.getBranchId(branch);
      ArtifactAccessObject accessObject = cache.get(artId, branchId);

      if (accessObject == null) {
         accessObject = new ArtifactAccessObject(artId, branchId);
         cache.put(artId, branchId, accessObject);
      }
      return accessObject;
   }

   public static AccessObject getArtifactAccessObjectFromCache(Artifact art) throws OseeCoreException {
      return getArtifactAccessObjectFromCache(art.getArtId(), art.getBranch());
   }

   public static AccessObject getArtifactAccessObjectFromCache(Integer artId2, IOseeBranch branch) throws OseeCoreException {
      long branchId2 = BranchManager.getBranchId(branch);
      return cache.get(artId2, branchId2);
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ArtifactAccessObject)) {
         return false;
      }
      ArtifactAccessObject ao = (ArtifactAccessObject) obj;
      return ao.artId.equals(this.artId) && ao.branchId.equals(this.branchId);
   }
}
