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
package org.eclipse.osee.framework.skynet.core.utility;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkArtifactDeletedEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkArtifactModifiedEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkTransactionDeletedEvent;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.exception.OseeCoreException;

/**
 * @author Jeff C. Phillips
 */
public class RemoteArtifactEventFactory {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(RemoteArtifactEventFactory.class);

   public static NetworkArtifactDeletedEvent makeArtifactDeleteEvent(Artifact artifact, int transactionNumber) {
      if (artifact == null || transactionNumber < 0) {
         throw new IllegalArgumentException("Artifact or transactionNumber can not be null.");
      }

      NetworkArtifactDeletedEvent networkArtifactDeletedEvent = null;
      try {
         networkArtifactDeletedEvent =
               new NetworkArtifactDeletedEvent(artifact.getBranch().getBranchId(),

               transactionNumber, artifact.getArtId(), artifact.getArtTypeId(),
                     artifact.getFactory().getClass().getCanonicalName(), SkynetAuthentication.getUser().getArtId());
      } catch (Exception ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      }
      return networkArtifactDeletedEvent;
   }

   public static NetworkTransactionDeletedEvent makeTransactionDeletedEvent(int... transactionIds) {
      if (transactionIds.length > 0) {
         throw new IllegalArgumentException("transactionIds can't be null");
      }

      NetworkTransactionDeletedEvent networkTransactionDeletedEvent = null;
      try {
         networkTransactionDeletedEvent =
               new NetworkTransactionDeletedEvent(SkynetAuthentication.getUser().getArtId(), transactionIds);
      } catch (Exception ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      }
      return networkTransactionDeletedEvent;
   }

   public static NetworkArtifactModifiedEvent makeArtifactModifiedEvent(Artifact artifact, int transactionNumber) throws OseeCoreException, SQLException {
      NetworkArtifactModifiedEvent networkArtifactModifiedEvent = null;

      if (artifact == null || transactionNumber < 0) {
         throw new IllegalArgumentException("Artifact or transactionNumber can not be null.");
      }

      try {
         networkArtifactModifiedEvent =
               new NetworkArtifactModifiedEvent(artifact.getBranch().getBranchId(), transactionNumber,
                     artifact.getArtId(), artifact.getArtTypeId(), artifact.getFactory().getClass().getCanonicalName(),
                     artifact.getDirtySkynetAttributeChanges(), getAuthor());
      } catch (Exception ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      }

      return networkArtifactModifiedEvent;
   }

   public static int getAuthor() {
      return SkynetAuthentication.getUser().isInDb() ? SkynetAuthentication.getUser().getArtId() : -1;
   }
}
