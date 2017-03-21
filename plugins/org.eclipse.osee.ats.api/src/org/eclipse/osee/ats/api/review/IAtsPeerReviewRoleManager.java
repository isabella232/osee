/*******************************************************************************
 * Copyright (c) 2017 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.review;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;

/**
 * @author Donald G. Dunne
 */
public interface IAtsPeerReviewRoleManager {

   List<UserRole> getUserRoles();

   List<UserRole> getUserRoles(Role role);

   List<IAtsUser> getRoleUsers(Role role);

   List<IAtsUser> getRoleUsers(Collection<UserRole> roles);

   void addOrUpdateUserRole(UserRole userRole);

   void removeUserRole(UserRole userRole);

   void saveToArtifact(IAtsChangeSet changes);

}