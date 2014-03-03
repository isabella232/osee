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
package org.eclipse.osee.ats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.OperationLogger;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;

/**
 * @author Ryan D. Brooks
 * @author David W. Miller
 */
public final class PortPairsOperation extends AbstractOperation {
   private final List<Pair<String, String>> portPairs;
   private final boolean useAtsID;

   public PortPairsOperation(OperationLogger logger, String portPairs, boolean useAtsID) throws OseeArgumentException {
      this(logger, new ArrayList<Pair<String, String>>(), useAtsID);
      for (String pair : portPairs.split("[\n\r]+")) {
         String[] pairLine = pair.split("[\\s,]+");
         if (pairLine.length != 2) {
            throw new OseeArgumentException("Invalid porting pairs");
         }
         this.portPairs.add(new Pair<String, String>(pairLine[0], pairLine[1]));
      }
   }

   public PortPairsOperation(OperationLogger logger, List<Pair<String, String>> portPairs, boolean useAtsID) {
      super("Port Pair(s)", Activator.PLUGIN_ID, logger);
      this.portPairs = portPairs;
      this.useAtsID = useAtsID;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws OseeCoreException {

      if (portPairs.isEmpty()) {
         throw new OseeArgumentException("Must specify at least one pair.");
      }
      logf("Porting %d pairs of workflows.", portPairs.size());

      double pairPercentage = 100 / portPairs.size();
      for (Pair<String, String> pair : portPairs) {
         portPair(pair);
         monitor.worked(calculateWork(pairPercentage));
      }

   }

   /**
    * <ol>
    * <li>create trax shadowed action for destination</li>
    * <li>create working branch from destination workflow</li>
    * <li>check that source workflow is completed</li>
    * <li>relate source workflow to destination workflow</li>
    * <li>create branch from transaction from the source workflow</li>
    * <li>commit into from port branch to destination working branch</li>
    * <li>report conflicts and commit completions if commit completes, delete port branch.</li>
    */
   private void portPair(Pair<String, String> pair) throws OseeCoreException {
      TeamWorkFlowArtifact sourceWorkflow;
      TeamWorkFlowArtifact destinationWorkflow;
      if (useAtsID) {
         sourceWorkflow = getWorkflowFromAtsID(pair.getFirst());
         destinationWorkflow = getWorkflowFromAtsID(pair.getSecond());

      } else {
         sourceWorkflow = getWorkflowFromRpcr(pair.getFirst());
         destinationWorkflow = getWorkflowFromRpcr(pair.getSecond());
      }
      doPortWork(sourceWorkflow, destinationWorkflow);
   }

   private TeamWorkFlowArtifact getWorkflowFromRpcr(String workflowId) throws OseeCoreException {
      IArtifactType LbaReqTeamWorkflow = TokenFactory.createArtifactType(0x0000BA000000000BL, "Lba Req Team Workflow");

      return (TeamWorkFlowArtifact) ArtifactQuery.getArtifactFromTypeAndAttribute(LbaReqTeamWorkflow,
         AtsAttributeTypes.LegacyPcrId, workflowId, CoreBranches.COMMON);
   }

   private TeamWorkFlowArtifact getWorkflowFromAtsID(String atsID) throws OseeCoreException {
      IArtifactType LbaSubSystemsTeamWorkflow =
         TokenFactory.createArtifactType(0x0000BA0000000009L, "Lba SubSystems Team Workflow");

      return (TeamWorkFlowArtifact) ArtifactQuery.getArtifactFromTypeAndAttribute(LbaSubSystemsTeamWorkflow,
         AtsAttributeTypes.AtsId, atsID, CoreBranches.COMMON);
   }

   private void doPortWork(TeamWorkFlowArtifact sourceWorkflow, TeamWorkFlowArtifact destinationWorkflow) throws OseeCoreException {
      if (destinationWorkflow.getWorkingBranchForceCacheUpdate() == null) {
         AtsBranchManagerCore.createWorkingBranch_Create(destinationWorkflow, true);
      }

      Branch destinationBranch = destinationWorkflow.getWorkingBranchForceCacheUpdate();
      Branch portBranch = getPortBranchFromWorkflow(sourceWorkflow, destinationWorkflow);
      if (portBranch == null) {
         logf("Source workflow [%s] not ready for port to Workflow [%s].", sourceWorkflow, destinationWorkflow);
         return;
      }

      try {
         if (portBranch.getBranchState().isCommitted()) {
            logf("Skipping completed workflow [%s].", destinationWorkflow);
         } else {
            ConflictManagerExternal conflictManager = new ConflictManagerExternal(destinationBranch, portBranch);
            BranchManager.commitBranch(null, conflictManager, false, false);
            logf("Commit complete for workflow [%s].", destinationWorkflow);
         }
      } catch (OseeCoreException ex) {
         logf("Resolve conflicts for workflow [%s].", destinationWorkflow);
      }
   }

   private Branch getPortBranchFromWorkflow(TeamWorkFlowArtifact sourceWorkflow, TeamWorkFlowArtifact destinationWorkflow) throws OseeCoreException {
      if (!sourceWorkflow.isRelated(AtsRelationTypes.Port_To, destinationWorkflow)) {
         sourceWorkflow.addRelation(AtsRelationTypes.Port_To, destinationWorkflow);
         sourceWorkflow.persist("create port relation");
      }

      Collection<Branch> branches =
         BranchManager.getBranchesByName(String.format("Porting [%s] branch", sourceWorkflow.getAtsId()));

      if (branches.isEmpty()) {
         TransactionRecord transRecord = AtsBranchManagerCore.getEarliestTransactionId(sourceWorkflow);
         if (transRecord == null) {
            return null;
         } else {
            return BranchManager.createWorkingBranchFromTx(transRecord,
               String.format("Porting [%s] branch", sourceWorkflow.getAtsId()), null);
         }
      } else {
         return branches.iterator().next();
      }
   }
}