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
package org.eclipse.osee.orcs.core.internal.transaction;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.HasBranch;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;
import org.eclipse.osee.orcs.core.internal.graph.GraphData;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.HasSession;

/**
 * @author Roberto E. Escobar
 * @author Megumi Telles
 */
public class TxData implements HasSession, HasBranch {

   public static enum TxState {
      NEW_TX,
      COMMIT_STARTED,
      COMMITTED,
      COMMIT_FAILED;
   }

   private final OrcsSession session;
   private final GraphData graph;
   private final Map<String, Artifact> writeables = new HashMap<>();
   private final Map<String, ArtifactReadable> readables = new HashMap<>();

   private ArtifactReadable author;
   private String comment;

   private volatile boolean isCommitInProgress;
   private volatile TxState txState;

   public TxData(OrcsSession session, GraphData graph) {
      this.session = session;
      this.graph = graph;
      this.txState = TxState.NEW_TX;
   }

   public void clear() {
      isCommitInProgress = false;
      writeables.clear();
      readables.clear();
   }

   @Override
   public OrcsSession getSession() {
      return session;
   }

   @Override
   public Long getBranchId() {
      return graph.getBranchId();
   }

   public GraphData getGraph() {
      return graph;
   }

   public ArtifactReadable getAuthor() {
      return author;
   }

   public String getComment() {
      return comment;
   }

   public TxState getTxState() {
      return txState;
   }

   public boolean isCommitInProgress() {
      return isCommitInProgress;
   }

   public void setAuthor(ArtifactReadable author) {
      this.author = author;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public void setTxState(TxState txState) {
      this.txState = txState;
   }

   public void setCommitInProgress(boolean isCommitInProgress) {
      this.isCommitInProgress = isCommitInProgress;
   }

   public Artifact add(Artifact artifact) {
      return writeables.put(artifact.getGuid(), artifact);
   }

   public void add(ArtifactReadable artifact) {
      readables.put(artifact.getGuid(), artifact);
   }

   public Iterable<Artifact> getAllWriteables() {
      return writeables.values();
   }

   public Artifact getWriteable(ArtifactId artifactId) {
      return writeables.get(artifactId.getGuid());
   }

   public ArtifactReadable getReadable(ArtifactId artifactId) {
      return readables.get(artifactId.getGuid());
   }

   @Override
   public String toString() {
      return "TxData [session=" + session + ", graph=" + graph + ", author=" + author + ", comment=" + comment + ", isCommitInProgress=" + isCommitInProgress + ", txState=" + txState + "]";
   }

}
