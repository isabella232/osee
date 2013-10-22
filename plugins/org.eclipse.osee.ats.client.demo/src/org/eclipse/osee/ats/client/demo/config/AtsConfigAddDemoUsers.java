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
package org.eclipse.osee.ats.client.demo.config;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.client.demo.DemoUsers;
import org.eclipse.osee.ats.core.client.util.AtsGroup;
import org.eclipse.osee.framework.core.data.IUserToken;
import org.eclipse.osee.framework.database.init.IDbInitializationTask;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.SystemGroup;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

/**
 * @author Donald G. Dunne
 */
public class AtsConfigAddDemoUsers implements IDbInitializationTask {

   @Override
   public void run() throws OseeCoreException {
      List<User> admins = new ArrayList<User>();

      SkynetTransaction transaction =
         TransactionManager.createTransaction(BranchManager.getCommonBranch(), "Add Dev Users");
      for (IUserToken userEnum : DemoUsers.values()) {
         User user = UserManager.createUser(userEnum, transaction);
         if (userEnum.isAdmin()) {
            admins.add(user);
         }
      }

      transaction.execute();

      SkynetTransaction transaction1 =
         TransactionManager.createTransaction(BranchManager.getCommonBranch(), "Configure OSEEAdmin");
      SystemGroup.OseeAdmin.getArtifact().persist(transaction1);
      AtsGroup.AtsAdmin.getArtifact().persist(transaction1);
      AtsGroup.AtsTempAdmin.addMember(UserManager.getUser(DemoUsers.Joe_Smith));
      AtsGroup.AtsTempAdmin.getArtifact().persist(transaction1);
      transaction1.execute();
   }
}
