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
package org.eclipse.osee.ats.access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.client.access.AtsBranchAccessContextId;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilClient;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IAccessContextId;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.dsl.integration.RoleContextProvider;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.event.filter.ArtifactTypeEventFilter;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * This class will return access context ids related to editing artifacts stored on a team workflow's working branch.
 * <br>
 * <br>
 * Access control can be called frequently, thus a cache is used. Events will clear cache as necessary.<br>
 * <br>
 * Access is determined from "Access Context Id" value stored on Team Workflow, if not there, then Actionable Items, if
 * not there, then Team Defs.
 *
 * @author Donald G. Dunne
 */
public class AtsBranchAccessManager implements IArtifactEventListener, EventHandler {

   // Cache to store artifact guid to context id list so don't have to re-compute
   private final Map<Long, Collection<IAccessContextId>> branchUuidToContextIdCache =
      new HashMap<Long, Collection<IAccessContextId>>(50);

   private static final List<Long> atsConfigArtifactTypes =
      Arrays.asList(AtsArtifactTypes.ActionableItem.getGuid(), AtsArtifactTypes.TeamDefinition.getGuid());

   private final RoleContextProvider roleContextProvider;
   private volatile long cacheUpdated = 0;

   public AtsBranchAccessManager() {
      // Available for osgi instantiation
      this(null);
   }

   public AtsBranchAccessManager(RoleContextProvider roleContextProvider) {
      this.roleContextProvider = roleContextProvider;
   }

   /**
    * True if not common branch and branch's associated artifact is a Team Workflow artifact
    */
   public boolean isApplicable(BranchId objectBranch) {
      boolean result = false;
      try {
         if (!AtsClientService.get().getAtsBranch().equals(objectBranch)) {
            ArtifactType assocArtType = BranchManager.getAssociatedArtifact(objectBranch).getArtifactType();
            if (assocArtType != null) {
               result = assocArtType.inheritsFrom(AtsArtifactTypes.AtsArtifact);
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.INFO, "Error determining access applicibility", ex);
      }
      return result;
   }

   public Collection<IAccessContextId> getContextId(BranchId branch) {
      if (branchUuidToContextIdCache.containsKey(branch.getUuid())) {
         return branchUuidToContextIdCache.get(branch.getUuid());
      }

      Collection<IAccessContextId> contextIds = new ArrayList<>();
      branchUuidToContextIdCache.put(branch.getUuid(), contextIds);
      try {
         // don't access control common branch artifacts...yet
         if (!AtsClientService.get().getAtsBranch().equals(branch)) {
            // do this check first since role will supersede others
            if (roleContextProvider != null) {
               contextIds.addAll(roleContextProvider.getContextId(UserManager.getUser()));
            }

            if (contextIds.isEmpty()) {
               // Else, get from associated artifact
               Artifact assocArtifact = BranchManager.getAssociatedArtifact(branch);
               ArtifactType assocArtType = assocArtifact.getArtifactType();
               if (assocArtType.inheritsFrom(AtsArtifactTypes.TeamWorkflow)) {
                  contextIds.addAll(internalGetFromWorkflow((TeamWorkFlowArtifact) assocArtifact));
               } else {
                  contextIds.add(AtsBranchAccessContextId.DENY_CONTEXT);
               }
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, "Exception obtaining Branch Access Context Id; Deny returned", ex);
         contextIds.add(AtsBranchAccessContextId.DENY_CONTEXT);
      }
      return contextIds;
   }

   /**
    * Provided for testing purposes only.
    */
   public static Collection<IAccessContextId> internalGetFromWorkflow(TeamWorkFlowArtifact teamArt) {
      Set<IAccessContextId> contextIds = new HashSet<>();
      try {
         contextIds.addAll(getFromArtifact(teamArt));
         if (contextIds.isEmpty()) {
            for (IAtsActionableItem aia : AtsClientService.get().getWorkItemService().getActionableItemService().getActionableItems(
               teamArt)) {
               Artifact artifact = AtsClientService.get().getConfigArtifact(aia);
               if (artifact != null) {
                  contextIds.addAll(getFromArtifact(artifact));
               }
               if (!contextIds.isEmpty()) {
                  return contextIds;
               }
            }
            if (contextIds.isEmpty()) {
               Artifact artifact = AtsClientService.get().getConfigArtifact(teamArt.getTeamDefinition());
               if (artifact != null) {
                  contextIds.addAll(getFromArtifact(artifact));
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, "Exception obtaining Branch Access Context Id; Deny returned", ex);
         return Arrays.asList(AtsBranchAccessContextId.DENY_CONTEXT);
      }
      return contextIds;
   }

   /**
    * Recursively check artifact and all default hierarchy parents
    */
   private static Collection<IAccessContextId> getFromArtifact(Artifact artifact) {
      Set<IAccessContextId> contextIds = new HashSet<>();
      try {
         for (String guid : artifact.getAttributesToStringList(CoreAttributeTypes.AccessContextId)) {
            // Do not use getOrCreateId here cause name represents where context ids came from
            // Cache above will take care of this not being created on each access request call.
            contextIds.add(TokenFactory.createAccessContextId(convertAccessAttributeToGuid(guid),
               "From [" + artifact.getArtifactTypeName() + "]" + artifact.toStringWithId() + " as [" + guid + "]"));
         }
         if (contextIds.isEmpty() && artifact.getParent() != null) {
            contextIds.addAll(getFromArtifact(artifact.getParent()));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return contextIds;
   }

   /**
    * ATS "Access Context Id" attribute value can be stored as "guid" or "guid,name" for easy reading. This method
    * strips ,name out so only guid is returned.
    */
   private static String convertAccessAttributeToGuid(String value) {
      return value.split(",")[0];
   }

   /**
    * Need to process artifact events for Common branch Team Workflows, Actionable Items and Team Definitions in case
    * Access Context Id attribute is edited.
    */
   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return getAtsObjectEventFilters();
   }

   private static final List<IEventFilter> atsObjectEventFilter = new ArrayList<>(2);
   private static final ArtifactTypeEventFilter atsArtifactTypesFilter = new ArtifactTypeEventFilter(
      AtsArtifactTypes.TeamWorkflow, AtsArtifactTypes.TeamDefinition, AtsArtifactTypes.ActionableItem);

   private synchronized static List<IEventFilter> getAtsObjectEventFilters() {
      try {
         if (atsObjectEventFilter.isEmpty()) {
            atsObjectEventFilter.add(AtsUtilClient.getAtsBranchFilter());
            atsObjectEventFilter.add(atsArtifactTypesFilter);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return atsObjectEventFilter;
   }

   /**
    * Since multiple events of same artifact type can come through, only clear cache every one second
    */
   public synchronized void clearCache() {
      long now = new Date().getTime();
      if (now - cacheUpdated > 1000) {
         branchUuidToContextIdCache.clear();
      }
   }

   @Override
   public void handleArtifactEvent(ArtifactEvent artifactEvent, Sender sender) {
      for (EventBasicGuidArtifact guidArt : artifactEvent.getArtifacts()) {
         if (atsConfigArtifactTypes.contains(guidArt.getArtTypeGuid())) {
            clearCache();
            return;
         }
         try {
            if (ArtifactTypeManager.getType(guidArt).inheritsFrom(AtsArtifactTypes.TeamWorkflow)) {
               TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) ArtifactCache.getActive(guidArt);
               if (teamArt != null && teamArt.getWorkingBranch() != null) {
                  branchUuidToContextIdCache.remove(teamArt.getWorkingBranch().getGuid());
               }
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   @Override
   public void handleEvent(Event event) {
      try {
         clearCache();
      } catch (Exception ex) {
         OseeLog.log(AccessControlManager.class, Level.SEVERE, ex);
      }
   }

}
