/*********************************************************************
 * Copyright (c) 2019 Boeing
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

package org.eclipse.osee.framework.core.data;

import java.util.Collection;

/**
 * @author Donald G. Dunne
 */
public interface IUserGroupService {

   IUserGroup getUserGroup(IUserGroupArtifactToken userGroup);

   Collection<IUserGroupArtifactToken> getMyUserGroups();

   boolean isInUserGroup(IUserGroupArtifactToken... userGroups);

   /**
    * Checks for existence of user group, then if member
    */
   public boolean isUserMember(IUserGroupArtifactToken amsUsers, Long id);

   public Collection<UserToken> getUsers(IUserGroupArtifactToken userGroup);
}
