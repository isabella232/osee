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

package org.eclipse.osee.framework.skynet.core.artifact;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import static org.eclipse.osee.framework.core.enums.CoreBranches.SYSTEM_ROOT;
import static org.eclipse.osee.framework.core.enums.SystemUser.OseeSystem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.ws.rs.core.Response;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.core.client.OseeClientProperties;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.BranchDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleBranchesExist;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.MergeBranch;
import org.eclipse.osee.framework.core.model.TransactionDelta;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.BranchFilter;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.OperationBuilder;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionDefinedObjects;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.operation.UpdateBranchOperation;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.update.ConflictResolverOperation;
import org.eclipse.osee.framework.skynet.core.commit.actions.CommitAction;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.skynet.core.httpRequests.CommitBranchHttpRequestOperation;
import org.eclipse.osee.framework.skynet.core.httpRequests.CreateBranchHttpRequestOperation;
import org.eclipse.osee.framework.skynet.core.httpRequests.PurgeBranchHttpRequestOperation;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.framework.skynet.core.internal.accessors.DatabaseBranchAccessor;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.skynet.core.utility.ConnectionHandler;
import org.eclipse.osee.framework.skynet.core.utility.Id4JoinQuery;
import org.eclipse.osee.framework.skynet.core.utility.JoinUtility;
import org.eclipse.osee.framework.skynet.core.utility.OseeInfo;
import org.eclipse.osee.orcs.rest.client.OseeClient;
import org.eclipse.osee.orcs.rest.model.BranchEndpoint;

/**
 * Provides access to all branches as well as support for creating branches of all types
 *
 * @author Ryan D. Brooks
 */
public final class BranchManager {
   private static final String LAST_DEFAULT_BRANCH = "LastDefaultBranchUuid";
   public static final String COMMIT_COMMENT = "Commit Branch ";
   private static final String SELECT_BRANCH_BY_NAME = "select * from osee_branch where branch_name = ?";
   private static IOseeBranch lastBranch;

   private BranchManager() {
      // this private empty constructor exists to prevent the default constructor from allowing public construction
   }

   private static BranchCache getCache() throws OseeCoreException {
      return ServiceUtil.getOseeCacheService().getBranchCache();
   }

   public static List<Branch> getBranchesAndViews(Predicate<Branch> branchFilter) throws OseeCoreException {
      return getCache().getBranchesAndViews(branchFilter);
   }

   public static List<Branch> getBranches(Predicate<Branch> branchFilter) throws OseeCoreException {
      return getCache().getBranches(branchFilter);
   }

   public static Branch getBranch(Predicate<Branch> branchFilter) throws OseeCoreException {
      List<Branch> branches = BranchManager.getBranches(branchFilter);
      if (branches.isEmpty()) {
         return null;
      } else if (branches.size() == 1) {
         return branches.get(0);
      } else {
         throw new MultipleBranchesExist("More than 1 branch exists that matches the filter: " + branchFilter);
      }
   }

   public static void refreshBranches() throws OseeCoreException {
      String refreshWindow = OseeInfo.getValue("cache.reload.throttle.millis");
      boolean reload = true;
      if (Strings.isNumeric(refreshWindow)) {
         long timeInMillis = Long.parseLong(refreshWindow);
         long diff = System.currentTimeMillis() - getCache().getLastLoaded();
         if (diff < timeInMillis) {
            reload = false;
         }
      }
      if (reload) {
         getCache().reloadCache();
      }
   }

   public static IOseeBranch getBranch(String branchName) throws OseeCoreException {
      Collection<IOseeBranch> branches = getBranchesByName(branchName);
      if (branches.isEmpty()) {
         throw new BranchDoesNotExist("No branch exists with the name: [%s]", branchName);
      }
      if (branches.size() > 1) {
         throw new MultipleBranchesExist("More than 1 branch exists with the name: [%s]", branchName);
      }
      return branches.iterator().next();
   }

   public static Collection<IOseeBranch> getBranchesByName(String branchName) throws OseeCoreException {
      Collection<IOseeBranch> branches = new ArrayList<>(1);
      ConnectionHandler.getJdbcClient().runQuery(stmt -> branches.add(getBranch(stmt.getLong("branch_id"))),
         SELECT_BRANCH_BY_NAME, branchName);
      return branches;
   }

   public static IOseeBranch getBranchToken(BranchId branch) throws OseeCoreException {
      return getBranch(branch);
   }

   public static IOseeBranch getBranchToken(Long branchId) throws OseeCoreException {
      return getBranch(branchId);
   }

   public static Branch getBranch(BranchId branch) throws OseeCoreException {
      if (branch instanceof Branch) {
         return (Branch) branch;
      } else {
         return getBranch(branch.getId(), branch.getView());
      }
   }

   public static Branch getBranch(Long branchId) throws OseeCoreException {
      return getBranch(branchId, ArtifactId.SENTINEL);
   }

   public static Branch getBranch(Long branchId, ArtifactId view) throws OseeCoreException {
      if (branchId == null) {
         throw new BranchDoesNotExist("Branch Uuid is null");
      }

      Branch branch = null;
      if (view.notEqual(ArtifactId.SENTINEL)) {
         List<Branch> views = getCache().getViews();
         for (Branch branchView : views) {
            if (branchView.getId().equals(branchId) && branchView.getView().equals(view)) {
               branch = branchView;
               break;
            }
         }
      } else {
         branch = getCache().getById(branchId);
         if (branch == null) {
            branch = loadBranchToCache(branchId);
         }
      }

      return branch;
   }

   /**
    * Do not call this method unless absolutely neccessary due to performance impacts.
    */
   public static synchronized void checkAndReload(BranchId branch) throws OseeCoreException {
      if (!branchExists(branch)) {
         loadBranchToCache(branch.getId());
      }
   }

   private static Branch loadBranchToCache(Long branchId) {
      Branch branch = DatabaseBranchAccessor.loadBranch(getCache(), branchId);
      getCache().cache(branch);
      return branch;
   }

   public static boolean branchExists(BranchId branch) throws OseeCoreException {
      return getCache().get(branch) != null;
   }

   public static boolean branchExists(long uuid) throws OseeCoreException {
      return getCache().getById(uuid) != null;
   }

   /**
    * returns the merge branch for this source destination pair from the cache or null if not found
    */
   public static IOseeBranch getMergeBranch(BranchId sourceBranch, BranchId destinationBranch) throws OseeCoreException {
      return getCache().findMergeBranch(sourceBranch, destinationBranch);
   }

   /**
    * returns the first merge branch for this source destination pair from the cache or null if not found
    */
   public static MergeBranch getFirstMergeBranch(BranchId sourceBranch) throws OseeCoreException {
      return getCache().findFirstMergeBranch(sourceBranch);
   }

   /**
    * returns a list tof all the merge branches for this source branch from the cache or null if not found
    */
   public static List<MergeBranch> getMergeBranches(BranchId sourceBranch) throws OseeCoreException {
      List<MergeBranch> mergeBranches = getCache().findAllMergeBranches(sourceBranch);
      return mergeBranches;
   }

   /**
    * returns whether a source branch has existing merge branches
    */
   public static boolean hasMergeBranches(BranchId sourceBranch) throws OseeCoreException {
      return !getMergeBranches(sourceBranch).isEmpty();
   }

   /**
    * returns whether a merge branch exists for a source and dest branch pair
    */
   public static boolean doesMergeBranchExist(BranchId sourceBranch, BranchId destBranch) throws OseeCoreException {
      return getMergeBranch(sourceBranch, destBranch) != null;
   }

   public static void reloadBranch(BranchId branch) {
      loadBranchToCache(branch.getId());
   }

   public static IStatus isDeleteable(Collection<Artifact> artifacts) throws OseeCoreException {
      List<ArtifactId> artIdsToCheck = new LinkedList<>();
      for (Artifact art : artifacts) {
         if (art.isOnBranch(CoreBranches.COMMON)) {
            artIdsToCheck.add(art);
         }
      }

      if (!artIdsToCheck.isEmpty()) {
         for (IOseeBranch branch : getCache().getAll()) {
            ArtifactId associatedArtifactId = getAssociatedArtifactId(branch);
            if (getState(branch) != BranchState.DELETED && artIdsToCheck.contains(associatedArtifactId)) {
               return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                  String.format("Cannot delete artId [%d] because it is the associated artifact of branch [%s]",
                     associatedArtifactId, branch.getName()));
            }
         }
      }

      return ArtifactCheck.OK_STATUS;
   }

   /**
    * returns a list tof all the merge branches for this source branch from the cache or null if not found
    */
   public static boolean isUpdatable(BranchId branchToUpdate) throws OseeCoreException {
      if (!hasMergeBranches(branchToUpdate) || getState(branchToUpdate).isRebaselineInProgress()) {
         return true;
      }
      return false;
   }

   /**
    * Update branch
    */
   public static Job updateBranch(IOseeBranch branch, final ConflictResolverOperation resolver) {
      IOperation operation = new UpdateBranchOperation(branch, resolver);
      return Operations.executeAsJob(operation, true);
   }

   public static Job updateBranch(Branch branch, BranchId fromBranch, ConflictResolverOperation resolver) {
      IOperation operation = new UpdateBranchOperation(branch, fromBranch, resolver);
      return Operations.executeAsJob(operation, true);
   }

   public static void purgeBranch(BranchId branch) throws OseeCoreException {
      Operations.executeWorkAndCheckStatus(new PurgeBranchHttpRequestOperation(branch, false));
   }

   public static void setType(BranchId branch, BranchType type) throws OseeCoreException {
      BranchEndpoint proxy = ServiceUtil.getOseeClient().getBranchEndpoint();
      Response response = proxy.setBranchType(branch, type);
      if (response.getStatus() == javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
         BranchManager.getBranch(branch).setBranchType(type);
         OseeEventManager.kickBranchEvent(BranchManager.class, new BranchEvent(BranchEventType.TypeUpdated, branch));
      }
   }

   public static void setState(BranchId branch, BranchState state) {
      BranchEndpoint proxy = ServiceUtil.getOseeClient().getBranchEndpoint();
      Response response = proxy.setBranchState(branch, state);
      if (response.getStatus() == javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
         BranchManager.getBranch(branch).setBranchState(state);
         OseeEventManager.kickBranchEvent(BranchManager.class, new BranchEvent(BranchEventType.StateUpdated, branch));
      }
   }

   public static void setArchiveState(BranchId branch, BranchArchivedState state) throws OseeCoreException {
      BranchEndpoint proxy = ServiceUtil.getOseeClient().getBranchEndpoint();
      if (state.isArchived()) {
         Response response = proxy.archiveBranch(branch);
         if (response.getStatus() == javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
            BranchManager.getBranch(branch).setArchived(true);
            OseeEventManager.kickBranchEvent(BranchManager.class,
               new BranchEvent(BranchEventType.ArchiveStateUpdated, branch));
         }
      } else {
         Response response = proxy.unarchiveBranch(branch);
         if (response.getStatus() == javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
            BranchManager.getBranch(branch).setArchived(false);
            OseeEventManager.kickBranchEvent(BranchManager.class,
               new BranchEvent(BranchEventType.ArchiveStateUpdated, branch));
         }
      }
   }

   public static void setName(BranchId branch, String newBranchName) {
      BranchEndpoint proxy = ServiceUtil.getOseeClient().getBranchEndpoint();
      Response response = proxy.setBranchName(branch, newBranchName);
      if (response.getStatus() == javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
         BranchManager.getBranch(branch).setName(newBranchName);
         OseeEventManager.kickBranchEvent(BranchManager.class, new BranchEvent(BranchEventType.Renamed, branch));
      }
   }

   /**
    * Delete a branch from the system. (This operation will set the branch state to deleted. This operation is
    * undo-able)
    */
   public static Job deleteBranch(final BranchId branch) {
      return Operations.executeAsJob(new DeleteBranchOperation(branch), true);
   }

   public static IStatus deleteBranchAndPend(final BranchId branch) {
      return Operations.executeWork(new DeleteBranchOperation(branch));
   }

   /**
    * Delete branches from the system. (sets branch state to deleted. operation is undo-able)
    *
    * @throws OseeCoreException
    */
   public static Job deleteBranch(final List<? extends BranchId> branches) {
      List<IOperation> ops = new ArrayList<>();
      for (BranchId branch : branches) {
         ops.add(new DeleteBranchOperation(branch));
      }
      OperationBuilder builder = Operations.createBuilder("Deleting multiple branches...");
      builder.addAll(ops);
      return Operations.executeAsJob(builder.build(), false);
   }

   /**
    * Commit the net changes from the source branch into the destination branch. If there are conflicts between the two
    * branches, the source branch changes will override those on the destination branch.
    */
   public static void commitBranch(IProgressMonitor monitor, ConflictManagerExternal conflictManager, boolean archiveSourceBranch, boolean overwriteUnresolvedConflicts) throws OseeCoreException {
      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }
      if (conflictManager.remainingConflictsExist() && !overwriteUnresolvedConflicts) {
         throw new OseeCoreException("Commit failed due to unresolved conflicts");
      }
      if (!isEditable(conflictManager.getDestinationBranch())) {
         throw new OseeCoreException("Commit failed - unable to commit into a non-editable branch");
      }

      boolean skipCommitChecksAndEvents = OseeClientProperties.isSkipCommitChecksAndEvents();
      if (!skipCommitChecksAndEvents) {
         runCommitExtPointActions(conflictManager);
      }

      IOperation operation =
         new CommitBranchHttpRequestOperation(UserManager.getUser(), conflictManager.getSourceBranch(),
            conflictManager.getDestinationBranch(), archiveSourceBranch, skipCommitChecksAndEvents);
      Operations.executeWorkAndCheckStatus(operation, monitor);
   }

   private static void runCommitExtPointActions(ConflictManagerExternal conflictManager) throws OseeCoreException {
      ExtensionDefinedObjects<CommitAction> extensions = new ExtensionDefinedObjects<CommitAction>(
         "org.eclipse.osee.framework.skynet.core.CommitActions", "CommitActions", "className");
      for (CommitAction commitAction : extensions.getObjects()) {
         commitAction.runCommitAction(conflictManager.getSourceBranch(), conflictManager.getDestinationBranch());
      }
   }

   /**
    * Calls the getMergeBranch method and if it returns null it will create a new merge branch based on the artIds from
    * the source branch.
    */
   public static IOseeBranch getOrCreateMergeBranch(IOseeBranch sourceBranch, IOseeBranch destBranch, ArrayList<Integer> expectedArtIds) throws OseeCoreException {
      IOseeBranch mergeBranch = getMergeBranch(sourceBranch, destBranch);
      if (mergeBranch == null) {
         mergeBranch = createMergeBranch(sourceBranch, destBranch, expectedArtIds);
      } else {
         UpdateMergeBranch op = new UpdateMergeBranch(ConnectionHandler.getJdbcClient(), mergeBranch, expectedArtIds,
            destBranch, sourceBranch);
         Operations.executeWorkAndCheckStatus(op);
      }
      return mergeBranch;
   }

   private static IOseeBranch createMergeBranch(final IOseeBranch sourceBranch, final IOseeBranch destBranch, final ArrayList<Integer> expectedArtIds) throws OseeCoreException {
      Id4JoinQuery joinQuery = JoinUtility.createId4JoinQuery();
      for (int artId : expectedArtIds) {
         joinQuery.add(sourceBranch, ArtifactId.valueOf(artId), TransactionId.SENTINEL, sourceBranch.getView());
      }
      BranchId branch;
      try {
         joinQuery.store();

         TransactionToken parentTx = getBaseTransaction(sourceBranch);
         String creationComment = String.format("New Merge Branch from %s(%s) and %s", sourceBranch.getName(),
            parentTx.getId(), destBranch.getName());
         String branchName = "Merge " + sourceBranch.getShortName() + " <=> " + destBranch.getShortName();
         branch = createBranch(BranchType.MERGE, parentTx, branchName, UserManager.getUser(), creationComment,
            joinQuery.getQueryId(), destBranch);
      } finally {
         joinQuery.delete();
      }

      MergeBranch mergeBranch = (MergeBranch) BranchManager.getBranch(branch);
      mergeBranch.setSourceBranch(sourceBranch);
      mergeBranch.setDestinationBranch(destBranch);
      return mergeBranch;
   }

   /**
    * Creates a new Branch based on the most recent transaction on the parent branch.
    */
   public static IOseeBranch createWorkingBranchFromTx(TransactionToken parentTransactionId, String childBranchName, Artifact associatedArtifact) throws OseeCoreException {
      String creationComment = String.format("New branch, copy of %s from transaction %s",
         getBranchName(parentTransactionId), parentTransactionId.getId());

      CreateBranchHttpRequestOperation operation = new CreateBranchHttpRequestOperation(BranchType.WORKING,
         parentTransactionId, IOseeBranch.create(childBranchName), associatedArtifact, creationComment);
      operation.setTxCopyBranchType(true);
      Operations.executeWorkAndCheckStatus(operation);
      return operation.getNewBranch();
   }

   public static IOseeBranch createPortBranchFromTx(TransactionToken parentTransactionId, String childBranchName, Artifact associatedArtifact) throws OseeCoreException {
      String creationComment = String.format("New port branch, copy of %s from transaction %s",
         getBranchName(parentTransactionId), parentTransactionId.getId());

      CreateBranchHttpRequestOperation operation = new CreateBranchHttpRequestOperation(BranchType.PORT,
         parentTransactionId, IOseeBranch.create(childBranchName), associatedArtifact, creationComment);
      operation.setTxCopyBranchType(true);
      Operations.executeWorkAndCheckStatus(operation);
      return operation.getNewBranch();
   }

   public static IOseeBranch createWorkingBranch(BranchId parentBranch, String childBranchName) throws OseeCoreException {
      return createWorkingBranch(parentBranch, childBranchName, OseeSystem);
   }

   public static IOseeBranch createWorkingBranch(BranchId parentBranch, String childBranchName, ArtifactId associatedArtifact) throws OseeCoreException {
      TransactionToken parentTransactionId = TransactionManager.getHeadTransaction(parentBranch);
      return createWorkingBranch(parentTransactionId, childBranchName, associatedArtifact);
   }

   public static IOseeBranch createWorkingBranch(BranchId parentBranch, IOseeBranch childBranch) {
      TransactionToken parentTransactionId = TransactionManager.getHeadTransaction(parentBranch);
      return createBranch(BranchType.WORKING, parentTransactionId, childBranch, OseeSystem);
   }

   public static IOseeBranch createWorkingBranch(TransactionToken parentTransaction, String branchName, ArtifactId associatedArtifact) throws OseeCoreException {
      return createBranch(BranchType.WORKING, parentTransaction, IOseeBranch.create(branchName), associatedArtifact);
   }

   public static BranchId createBaselineBranch(BranchId parentBranch, IOseeBranch childBranch) throws OseeCoreException {
      return createBaselineBranch(parentBranch, childBranch, OseeSystem);
   }

   public static BranchId createTopLevelBranch(IOseeBranch branch) throws OseeCoreException {
      return createBaselineBranch(SYSTEM_ROOT, branch, OseeSystem);
   }

   private static BranchId createBaselineBranch(BranchId parentBranch, IOseeBranch childBranch, ArtifactId associatedArtifact) throws OseeCoreException {
      TransactionToken parentTransaction = TransactionManager.getHeadTransaction(parentBranch);
      return createBranch(BranchType.BASELINE, parentTransaction, childBranch, associatedArtifact);
   }

   public static BranchId createTopLevelBranch(final String branchName) throws OseeCoreException {
      return createTopLevelBranch(IOseeBranch.create(branchName));
   }

   private static IOseeBranch createBranch(BranchType branchType, TransactionToken parentTransaction, IOseeBranch childBranch, ArtifactId associatedArtifact) {
      String creationComment =
         String.format("New Branch from %s (%s)", getBranchName(parentTransaction), parentTransaction.getId());
      CreateBranchHttpRequestOperation operation = new CreateBranchHttpRequestOperation(branchType, parentTransaction,
         childBranch, associatedArtifact, creationComment);
      Operations.executeWorkAndCheckStatus(operation);
      return operation.getNewBranch();
   }

   private static IOseeBranch createBranch(BranchType branchType, TransactionToken parentTransaction, String branchName, Artifact associatedArtifact, String creationComment, int mergeAddressingQueryId, BranchId destinationBranch) {
      CreateBranchHttpRequestOperation operation =
         new CreateBranchHttpRequestOperation(branchType, parentTransaction, IOseeBranch.create(branchName),
            associatedArtifact, creationComment, mergeAddressingQueryId, destinationBranch);
      Operations.executeWorkAndCheckStatus(operation);
      return operation.getNewBranch();
   }

   public static List<? extends IOseeBranch> getBaselineBranches() throws OseeCoreException {
      return getBranches(BranchArchivedState.UNARCHIVED, BranchType.BASELINE);
   }

   public static List<Branch> getBranchesAndViews(BranchArchivedState archivedState, BranchType... branchTypes) {
      return getCache().getBranchesAndViews(new BranchFilter(archivedState, branchTypes));
   }

   public static List<Branch> getBranches(BranchArchivedState archivedState, BranchType... branchTypes) {
      return getCache().getBranches(new BranchFilter(archivedState, branchTypes));
   }

   private static BranchId getDefaultInitialBranch() throws OseeCoreException {
      ExtensionDefinedObjects<IDefaultInitialBranchesProvider> extensions =
         new ExtensionDefinedObjects<IDefaultInitialBranchesProvider>(
            "org.eclipse.osee.framework.skynet.core.DefaultInitialBranchProvider", "DefaultInitialBranchProvider",
            "class", true);
      for (IDefaultInitialBranchesProvider provider : extensions.getObjects()) {
         try {
            // Guard against problematic extensions
            for (BranchId branch : provider.getDefaultInitialBranches()) {
               if (branch != null) {
                  return branch;
               }
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.WARNING,
               "Exception occurred while trying to determine initial default branch", ex);
         }
      }
      return COMMON;
   }

   public static IOseeBranch getLastBranch() {
      if (lastBranch == null) {
         try {
            Long branchUuid = Long.valueOf(UserManager.getSetting(LAST_DEFAULT_BRANCH));
            lastBranch = getBranch(branchUuid);
         } catch (Exception ex) {
            try {
               lastBranch = getBranchToken(getDefaultInitialBranch());
               UserManager.setSetting(LAST_DEFAULT_BRANCH, lastBranch.getUuid());
            } catch (OseeCoreException ex1) {
               OseeLog.log(Activator.class, Level.SEVERE, ex1);
            }
         }
      }
      return lastBranch;
   }

   public static void setLastBranch(IOseeBranch branch) {
      lastBranch = branch;
   }

   public static void decache(Branch branch) throws OseeCoreException {
      getCache().decache(branch);
   }

   public static boolean hasChanges(BranchId branch) throws OseeCoreException {
      return !getBaseTransaction(branch).equals(TransactionManager.getHeadTransaction(branch));
   }

   public static boolean isChangeManaged(BranchId branch) throws OseeCoreException {
      ArtifactId associatedArtifactId = getAssociatedArtifactId(branch);
      return associatedArtifactId.isValid() && !associatedArtifactId.equals(OseeSystem);
   }

   public static void setAssociatedArtifactId(BranchId branch, ArtifactId artifactId) {
      OseeClient client = ServiceUtil.getOseeClient();
      BranchEndpoint proxy = client.getBranchEndpoint();

      Response response = proxy.associateBranchToArtifact(branch, artifactId);
      if (javax.ws.rs.core.Response.Status.OK.getStatusCode() == response.getStatus()) {
         getBranch(branch).setAssociatedArtifact(artifactId);
      }
   }

   public static ArtifactId getAssociatedArtifactId(BranchId branch) {
      return getBranch(branch).getAssociatedArtifactId();
   }

   public static Artifact getAssociatedArtifact(BranchId branch) throws OseeCoreException {
      ArtifactId associatedArtifactId = getAssociatedArtifactId(branch);
      if (associatedArtifactId.isInvalid()) {
         return UserManager.getUser(OseeSystem);
      }
      return ArtifactQuery.getArtifactFromId(associatedArtifactId, COMMON);
   }

   public static Artifact getAssociatedArtifact(TransactionDelta txDelta) throws OseeCoreException {
      Artifact associatedArtifact = null;
      if (txDelta.areOnTheSameBranch()) {
         Long commitArtId = TransactionManager.getCommitArtId(txDelta.getEndTx());
         if (!commitArtId.equals(0L)) {
            associatedArtifact = ArtifactQuery.getArtifactFromId(commitArtId, COMMON);
         }
      } else {
         BranchId sourceBranch = txDelta.getStartTx().getBranch();
         associatedArtifact = BranchManager.getAssociatedArtifact(sourceBranch);
      }
      return associatedArtifact;
   }

   public static void invalidateBranches() throws OseeCoreException {
      getCache().invalidate();
   }

   public static BranchId getParentBranch(BranchId branch) {
      return getBranch(branch).getParentBranch();
   }

   public static boolean isParentSystemRoot(BranchId branch) {
      return isParent(branch, SYSTEM_ROOT);
   }

   public static boolean isParent(BranchId branch, BranchId parentBranch) {
      return parentBranch.equals(getParentBranch(branch));
   }

   public static TransactionRecord getBaseTransaction(BranchId branch) {
      return getBranch(branch).getBaseTransaction();
   }

   public static TransactionRecord getSourceTransaction(BranchId branch) {
      return getBranch(branch).getSourceTransaction();
   }

   public static BranchState getState(BranchId branch) {
      return getBranch(branch).getBranchState();
   }

   public static BranchType getType(BranchId branch) {
      return getBranch(branch).getBranchType();
   }

   public static BranchType getType(TransactionToken tx) {
      return getBranch(tx.getBranch()).getBranchType();
   }

   public static boolean isEditable(BranchId branch) {
      Branch fullBranch = getBranch(branch);
      BranchState state = fullBranch.getBranchState();
      return (state.isCreated() || state.isModified() || state.isRebaselineInProgress()) && !fullBranch.isArchived();
   }

   public static Collection<BranchId> getAncestors(BranchId branch) {
      return getBranch(branch).getAncestors();
   }

   public static boolean hasAncestor(BranchId branch, BranchId ancestor) {
      return getBranch(branch).hasAncestor(ancestor);
   }

   public static boolean isArchived(BranchId branch) {
      return getBranch(branch).isArchived();
   }

   public static String getArchivedStr(BranchId branch) {
      return BranchArchivedState.fromBoolean(getBranch(branch).isArchived()).name();
   }

   public static boolean hasChildren(BranchId branch) {
      return !getBranch(branch).getChildren().isEmpty();
   }

   /**
    * @param recurse if true all descendants are processed, otherwise, only direct descendants are.
    * @return all unarchived child branches that are not of type merge
    */
   public static Collection<Branch> getChildBranches(BranchId branch, boolean recurse) {
      Set<Branch> children = new HashSet<>();
      BranchFilter filter = new BranchFilter(BranchArchivedState.UNARCHIVED);
      filter.setNegatedBranchTypes(BranchType.MERGE);
      getBranch(branch).getChildBranches(children, recurse, filter);
      return children;
   }

   public static void resetWasLoaded() {
      getCache().invalidate();
   }

   public static boolean isLoaded() {
      return getCache().isLoaded();
   }

   public static String getBranchName(BranchId branch) {
      return getBranch(branch).getName();
   }

   public static String getBranchName(Long branchId) {
      return getBranch(branchId).getName();
   }

   public static String getBranchShortName(BranchId branch) {
      return getBranch(branch).getShortName();
   }

   public static String getBranchName(TransactionToken tx) {
      return getBranch(tx.getBranch()).getName();
   }

   public static String getBranchShortName(TransactionToken tx) {
      return getBranch(tx.getBranch()).getShortName();
   }
}
