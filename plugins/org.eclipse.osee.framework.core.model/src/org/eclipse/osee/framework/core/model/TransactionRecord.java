/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.core.model;

import java.util.Date;
import org.eclipse.osee.framework.core.data.Adaptable;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.data.UserId;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.jdk.core.type.BaseId;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Jeff C. Phillips
 */
public class TransactionRecord extends BaseId implements TransactionToken, Adaptable {
   public static TransactionRecord SENTINEL = new TransactionRecord(Id.SENTINEL, BranchId.SENTINEL, null, null,
      UserId.SENTINEL, 0, TransactionDetailsType.INVALID, 0L);
   private final TransactionDetailsType txType;
   private final BranchId branch;
   private String comment;
   private Date time;
   private UserId authorArtId;
   private int commitArtId;
   private Long buildId;

   public TransactionRecord(Long id, BranchId branch, String comment, Date time, UserId authorArtId, int commitArtId, TransactionDetailsType txType, Long buildId) {
      super(id);
      this.branch = branch;
      this.buildId = buildId;
      this.comment = Strings.intern(comment);
      this.time = time;
      this.authorArtId = authorArtId;
      this.commitArtId = commitArtId;
      this.txType = txType;
   }

   @Override
   public BranchId getBranch() {
      return branch;
   }

   public String getComment() {
      return comment;
   }

   public Date getTimeStamp() {
      return time;
   }

   public UserId getAuthor() {
      return authorArtId;
   }

   public int getCommit() {
      return commitArtId;
   }

   public TransactionDetailsType getTxType() {
      return txType;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public void setTimeStamp(Date time) {
      this.time = time;
   }

   public void setAuthor(UserId authorArtId) {
      this.authorArtId = authorArtId;
   }

   public void setCommit(int commitArtId) {
      this.commitArtId = commitArtId;
   }

   public Long getBuildId() {
      return buildId;
   }

   public void setBuildId(Long buildId) {
      this.buildId = buildId;
   }
}