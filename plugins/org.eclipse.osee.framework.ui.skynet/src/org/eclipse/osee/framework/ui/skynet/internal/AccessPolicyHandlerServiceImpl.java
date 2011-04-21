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
package org.eclipse.osee.framework.ui.skynet.internal;

import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.model.access.AccessDataQuery;
import org.eclipse.osee.framework.core.model.access.PermissionStatus;
import org.eclipse.osee.framework.core.services.IAccessControlService;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.artifact.IAccessPolicyHandlerService;

/**
 * @author Jeff C. Phillips
 */
public class AccessPolicyHandlerServiceImpl implements IAccessPolicyHandlerService {
   private final IBasicArtifact<?> user;
   private final IAccessControlService accessControlService;

   public AccessPolicyHandlerServiceImpl(IBasicArtifact<?> user, IAccessControlService accessControlService) {
      super();
      this.user = user;
      this.accessControlService = accessControlService;
   }

   /**
    * @param level - A level of OseeLevel.SEVERE_POPUP will cause an error dialog to be displayed to the user. All
    * others will write to the log.
    */
   @Override
   public PermissionStatus hasAttributeTypePermission(Collection<? extends IBasicArtifact<?>> artifacts, IAttributeType attributeType, PermissionEnum permission, Level level) throws OseeCoreException {
      AccessDataQuery query = accessControlService.getAccessData(user, artifacts);
      PermissionStatus permissionStatus = new PermissionStatus();

      if (artifacts != null) {
         for (IBasicArtifact<?> artifact : artifacts) {
            query.attributeTypeMatches(permission, artifact, attributeType, permissionStatus);

            if (printErrorMessage(artifacts, permissionStatus, level)) {
               break;
            }
         }
      }
      return permissionStatus;
   }

   @Override
   public PermissionStatus hasArtifactPermission(Collection<? extends IBasicArtifact<?>> artifacts, PermissionEnum permission, Level level) throws OseeCoreException {
      AccessDataQuery query = accessControlService.getAccessData(user, artifacts);
      PermissionStatus permissionStatus = new PermissionStatus();

      if (artifacts != null) {
         for (IBasicArtifact<?> artifact : artifacts) {
            query.artifactMatches(PermissionEnum.WRITE, artifact, permissionStatus);

            if (printErrorMessage(artifacts, permissionStatus, level)) {
               break;
            }
         }
      }
      return permissionStatus;
   }

   @Override
   public PermissionStatus hasRelationSidePermission(Collection<? extends IRelationTypeSide> relationTypeSides, PermissionEnum permission, Level level) throws OseeCoreException {
      AccessDataQuery query = accessControlService.getAccessData(user, relationTypeSides);
      PermissionStatus permissionStatus = new PermissionStatus();

      if (relationTypeSides != null) {
         for (IRelationTypeSide relationTypeSide : relationTypeSides) {
            query.relationTypeMatches(permission, relationTypeSide, permissionStatus);

            if (printErrorMessage(relationTypeSides, permissionStatus, level)) {
               break;
            }
         }
      }
      return permissionStatus;
   }

   private boolean printErrorMessage(Collection<?> objects, PermissionStatus permissionStatus, Level level) {
      boolean notMatched = !permissionStatus.matched();

      if (notMatched) {
         OseeLog.log(SkynetGuiPlugin.class, level, String.format(
            "Access Denied - [%s] does not have valid permission to edit item(s) : [%s]%s", user,
            Collections.toString("; ", objects),
            (Strings.isValid(permissionStatus.getReason()) ? "\n reason:[%s]" : "")));
      }
      return notMatched;
   }

   @Override
   public PermissionStatus hasArtifactTypePermission(IOseeBranch branch, Collection<? extends IArtifactType> artifactTypes, PermissionEnum permission, Level level) throws OseeCoreException {
      AccessDataQuery query = accessControlService.getAccessData(user, java.util.Collections.singleton(branch));
      PermissionStatus permissionStatus = new PermissionStatus();

      if (artifactTypes != null) {
         for (IArtifactType artifactType : artifactTypes) {
            query.branchArtifactTypeMatches(permission, branch, artifactType, permissionStatus);

            if (printErrorMessage(artifactTypes, permissionStatus, level)) {
               break;
            }
         }
      }
      return permissionStatus;
   }

   @Override
   public IAccessControlService getAccessService() {
      return accessControlService;
   }
}
