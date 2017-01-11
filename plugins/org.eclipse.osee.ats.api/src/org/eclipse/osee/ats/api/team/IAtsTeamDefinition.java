/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.team;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.commit.ICommitConfigItem;
import org.eclipse.osee.ats.api.rule.IAtsRules;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.version.VersionLockedType;
import org.eclipse.osee.ats.api.version.VersionReleaseType;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface IAtsTeamDefinition extends IAtsConfigObject, IAtsRules, ICommitConfigItem {

   boolean isActionable();

   Collection<IAtsActionableItem> getActionableItems();

   IAtsTeamDefinition getParentTeamDef();

   Collection<IAtsTeamDefinition> getChildrenTeamDefinitions();

   /*****************************************************
    * Team Leads, Members, Priviledged Members
    ******************************************************/

   Collection<IAtsUser> getLeads();

   Collection<IAtsUser> getLeads(Collection<IAtsActionableItem> actionableItems) throws OseeCoreException;

   Collection<IAtsUser> getMembers() throws OseeCoreException;

   Collection<IAtsUser> getMembersAndLeads() throws OseeCoreException;

   Collection<IAtsUser> getSubscribed();

   Collection<IAtsUser> getPrivilegedMembers();

   /*****************************
    * Branching Data
    ******************************/

   boolean isAllowCommitBranch();

   @Override
   Result isAllowCommitBranchInherited();

   boolean isAllowCreateBranch();

   @Override
   Result isAllowCreateBranchInherited();

   @Override
   BranchId getBaselineBranchId();

   BranchId getTeamBranchId();

   /*****************************
    * Versions
    ******************************/

   boolean isTeamUsesVersions() throws OseeCoreException;

   IAtsVersion getNextReleaseVersion();

   IAtsTeamDefinition getTeamDefinitionHoldingVersions() throws OseeCoreException;

   IAtsVersion getVersion(String name) throws OseeCoreException;

   Collection<IAtsVersion> getVersions();

   Collection<IAtsVersion> getVersions(VersionReleaseType releaseType, VersionLockedType lockedType) throws OseeCoreException;

   Collection<IAtsVersion> getVersionsFromTeamDefHoldingVersions(VersionReleaseType releaseType, VersionLockedType lockedType) throws OseeCoreException;

   Collection<IAtsVersion> getVersionsLocked(VersionLockedType lockType);

   Collection<IAtsVersion> getVersionsReleased(VersionReleaseType releaseType);

   /*****************************
    * Work Definitions
    ******************************/

   String getWorkflowDefinition();

   String getRelatedTaskWorkDefinition();

   String getRelatedPeerWorkDefinition();

}
