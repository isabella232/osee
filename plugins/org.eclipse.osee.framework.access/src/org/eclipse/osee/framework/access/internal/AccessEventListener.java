/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.framework.access.internal;

import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.access.AccessControlServiceImpl;
import org.eclipse.osee.framework.access.internal.data.ArtifactAccessObject;
import org.eclipse.osee.framework.core.data.BranchToken;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.model.EventModType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;

/**
 * @author Donald G. Dunne
 */
public final class AccessEventListener implements IBranchEventListener, IArtifactEventListener {

   private final AccessControlServiceImpl service;
   private final AccessControlCacheHandler accessControlCacheHandler;

   public AccessEventListener(AccessControlServiceImpl service, AccessControlCacheHandler accessControlCacheHandler) {
      this.accessControlCacheHandler = accessControlCacheHandler;
      this.service = service;
   }

   @Override
   public void handleArtifactEvent(ArtifactEvent artifactEvent, Sender sender) {
      for (EventBasicGuidArtifact guidArt : artifactEvent.getArtifacts()) {
         if (guidArt.is(EventModType.Added) && guidArt.isTypeEqual(CoreArtifactTypes.User)) {
            try {
               accessControlCacheHandler.reloadCache(service);
            } catch (OseeCoreException ex) {
               OseeLog.log(AccessControlHelper.class, Level.SEVERE, ex);
            }
         }
         if (guidArt.is(EventModType.Purged)) {
            try {
               Artifact cacheArt = ArtifactCache.getActive(guidArt);
               if (cacheArt != null) {
                  ArtifactAccessObject artifactAccessObject = ArtifactAccessObject.getArtifactAccessObject(cacheArt);
                  accessControlCacheHandler.updateAccessList(service, artifactAccessObject);
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(AccessControlHelper.class, Level.SEVERE, ex);
            }

         }
      }
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return null;
   }

   @Override
   public void handleBranchEvent(Sender sender, final BranchEvent branchEvent) {
      try {
         if (branchEvent.getEventType() == BranchEventType.Deleted) {
            accessControlCacheHandler.updateAccessListForBranchObject(service,
               BranchToken.create(branchEvent.getSourceBranch(), "unknown"));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(AccessControlHelper.class, Level.SEVERE, ex);
      }
   }

}