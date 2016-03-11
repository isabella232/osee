/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.user;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * Non-artifact based user service
 *
 * @author Donald G. Dunne
 */
public interface IAtsUserService {

   IAtsUser getCurrentUser() throws OseeCoreException;

   String getCurrentUserId() throws OseeCoreException;

   IAtsUser getUserById(String userId) throws OseeCoreException;

   boolean isUserIdValid(String userId) throws OseeCoreException;

   boolean isUserNameValid(String name) throws OseeCoreException;

   IAtsUser getUserByName(String name) throws OseeCoreException;

   Collection<IAtsUser> getUsersByUserIds(Collection<String> userIds) throws OseeCoreException;

   boolean isAtsAdmin(IAtsUser user);

   List<IAtsUser> getUsers(Active active);

   List<IAtsUser> getUsersSortedByName(Active active);

   void clearCache();

   void releaseUser();

   Collection<IAtsUser> getSubscribed(IAtsWorkItem workItem);

   IAtsUser getUserById(long accountId);

}
