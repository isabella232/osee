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

package org.eclipse.osee.framework.skynet.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.access.AccessDataQuery;
import org.eclipse.osee.framework.core.access.IAccessControlService;
import org.eclipse.osee.framework.core.access.PermissionStatus;
import org.eclipse.osee.framework.core.client.OseeClientProperties;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.AccessPolicy;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;

/**
 * @author Roberto E. Escobar
 */
public class AccessPolicyImpl implements AccessPolicy {

   public IAccessControlService accessControlService;

   public void setAccessControlService(IAccessControlService service) {
      this.accessControlService = service;
   }

   private User getCurrentUser() {
      return UserManager.getUser();
   }

   private IAccessControlService getAccessService() {
      return accessControlService;
   }

   private boolean printErrorMessage(User user, Collection<?> objects, PermissionStatus permissionStatus, Level level) {
      boolean notMatched = !permissionStatus.matched();

      if (notMatched) {
         String objectString = org.eclipse.osee.framework.jdk.core.util.Collections.toString("; ", objects);
         String reasonString = permissionStatus.getReason();
         if (Strings.isValid(reasonString)) {
            reasonString = String.format("\n reason:[%s]", reasonString);
         } else {
            reasonString = "";
         }
         OseeLog.logf(Activator.class, level,
            "Access Denied - [%s] does not have valid permission to edit\n item(s) : [%s]%s", user, objectString,
            reasonString);
      }
      return notMatched;
   }

   @Override
   public void removePermissions(BranchId branch) {
      getAccessService().removePermissions(branch);
   }

   @Override
   public boolean isReadOnly(Artifact artifact) {
      try {
         return artifact.isDeleted() || artifact.isHistorical() || !BranchManager.isEditable(
            artifact.getBranch()) || !getAccessService().hasPermission(artifact, PermissionEnum.WRITE);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return true;
      }
   }

   @Override
   public PermissionStatus hasBranchPermission(BranchId branch, PermissionEnum permission, Level level) {
      User currentUser = getCurrentUser();
      AccessDataQuery query = getAccessService().getAccessData(currentUser, java.util.Collections.singleton(branch));
      PermissionStatus permissionStatus = null;
      if (!OseeClientProperties.isInDbInit()) {
         permissionStatus = new PermissionStatus();
         query.branchMatches(permission, branch, permissionStatus);
         printErrorMessage(currentUser, java.util.Collections.singleton(branch), permissionStatus, level);
      } else {
         permissionStatus = new PermissionStatus(true, "In DB Init; All permission enabled");
      }
      return permissionStatus;
   }

   /**
    * @param level - A level of OseeLevel.SEVERE_POPUP will cause an error dialog to be displayed to the user. All
    * others will write to the log.
    */
   @Override
   public PermissionStatus hasAttributeTypePermission(Collection<? extends ArtifactToken> artifacts, AttributeTypeId attributeType, PermissionEnum permission, Level level) {
      User currentUser = getCurrentUser();
      AccessDataQuery query = getAccessService().getAccessData(currentUser, artifacts);

      PermissionStatus permissionStatus = null;
      if (!OseeClientProperties.isInDbInit()) {
         permissionStatus = new PermissionStatus();
         if (artifacts != null) {
            for (ArtifactToken artifact : artifacts) {
               query.attributeTypeMatches(permission, artifact, attributeType, permissionStatus);

               if (printErrorMessage(currentUser, artifacts, permissionStatus, level)) {
                  break;
               }
            }
         }
      } else {
         permissionStatus = new PermissionStatus(true, "In DB Init; All permission enabled");
      }

      return permissionStatus;
   }

   @Override
   public PermissionStatus hasArtifactPermission(Collection<Artifact> artifacts, PermissionEnum permission, Level level) {
      User currentUser = getCurrentUser();
      AccessDataQuery query = getAccessService().getAccessData(currentUser, artifacts);

      PermissionStatus permissionStatus = null;
      if (!OseeClientProperties.isInDbInit()) {
         permissionStatus = new PermissionStatus();
         if (artifacts != null) {
            for (Artifact artifact : artifacts) {
               if (artifact.isInDb()) {
                  query.artifactMatches(permission, artifact, permissionStatus);
               }
               if (printErrorMessage(currentUser, artifacts, permissionStatus, level)) {
                  break;
               }
            }
         }
      } else {
         permissionStatus = new PermissionStatus(true, "In DB Init; All permission enabled");
      }

      return permissionStatus;
   }

   @Override
   public PermissionStatus hasArtifactTypePermission(BranchId branch, Collection<? extends ArtifactTypeId> artifactTypes, PermissionEnum permission, Level level) {
      User currentUser = getCurrentUser();
      AccessDataQuery query = getAccessService().getAccessData(currentUser, java.util.Collections.singleton(branch));

      PermissionStatus permissionStatus = null;
      if (!OseeClientProperties.isInDbInit()) {
         permissionStatus = new PermissionStatus();
         if (artifactTypes != null) {
            for (ArtifactTypeId artifactType : artifactTypes) {
               query.branchArtifactTypeMatches(permission, branch, artifactType, permissionStatus);

               if (printErrorMessage(currentUser, artifactTypes, permissionStatus, level)) {
                  break;
               }
            }
         }
      } else {
         permissionStatus = new PermissionStatus(true, "In DB Init; All permission enabled");
      }

      return permissionStatus;
   }

   @Override
   public PermissionStatus canRelationBeModified(Artifact subject, Collection<Artifact> toBeRelated, RelationTypeSide relationTypeSide, Level level) {
      PermissionStatus subjectPermission = canRelationBeModifiedHelper(subject, null, relationTypeSide, level);
      if (subjectPermission.matched() && toBeRelated != null && !toBeRelated.isEmpty()) {
         for (Artifact art : toBeRelated) {
            RelationSide otherSide = relationTypeSide.getSide().oppositeSide();
            PermissionStatus toBeRelatedPermission =
               canRelationBeModifiedHelper(art, null, new RelationTypeSide(relationTypeSide, otherSide), level);
            if (!toBeRelatedPermission.matched()) {
               return toBeRelatedPermission;
            }
         }
      }
      return subjectPermission;
   }

   private PermissionStatus canRelationBeModifiedHelper(Artifact subject, Collection<Artifact> toBeRelated, RelationTypeSide relationTypeSide, Level level) {
      PermissionStatus status = hasArtifactRelationPermission(java.util.Collections.singleton(subject),
         java.util.Collections.singleton(relationTypeSide), PermissionEnum.WRITE, level);

      if (!status.matched()) {
         ArrayList<Artifact> artifacts = new ArrayList<>();
         artifacts.add(subject);
         if (toBeRelated != null) {
            artifacts.addAll(toBeRelated);
         }
         status = hasArtifactPermission(artifacts, PermissionEnum.WRITE, level);
      }
      return status;
   }

   private PermissionStatus hasArtifactRelationPermission(Collection<? extends ArtifactToken> artifacts, Collection<? extends RelationTypeSide> relationTypeSides, PermissionEnum permission, Level level) {
      AccessDataQuery query = getAccessService().getAccessData(getCurrentUser(), artifacts);

      PermissionStatus permissionStatus = null;
      if (!OseeClientProperties.isInDbInit()) {
         permissionStatus = new PermissionStatus();
         if (!OseeClientProperties.isInDbInit()) {
            permissionStatus = new PermissionStatus();
            for (ArtifactToken artifact : artifacts) {
               for (RelationTypeSide relationTypeSide : relationTypeSides) {
                  query.relationTypeMatches(permission, artifact, relationTypeSide, permissionStatus);
               }
            }
         } else {
            permissionStatus = new PermissionStatus(true, "In DB Init; All permission enabled");
         }
      } else {
         permissionStatus = new PermissionStatus(true, "In DB Init; All permission enabled");
      }

      return permissionStatus;
   }

   @Override
   public XResultData isDeleteable(Collection<? extends ArtifactToken> artifacts, XResultData results) {
      return accessControlService.isDeleteable(artifacts, results);
   }

   @Override
   public XResultData isRenamable(Collection<? extends ArtifactToken> artifacts, XResultData results) {
      return accessControlService.isRenamable(artifacts, results);
   }

   @Override
   public XResultData isDeleteableRelation(ArtifactToken artifact, RelationTypeToken relationType, XResultData results) {
      return accessControlService.isDeleteableRelation(artifact, relationType, results);
   }

   @Override
   public void ensurePopulated() {
      accessControlService.ensurePopulated();
   }
}
