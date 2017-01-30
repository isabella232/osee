/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/

package org.eclipse.osee.define.report.api;

import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionId;

/**
 * @author Morgan E. Cook
 */
public class WordTemplateContentData {

   private Long artId;
   private BranchId branchId;
   private String footer;
   private boolean isEdit;
   private String linkType;
   private TransactionId txId;
   private String sessionId;
   private String oseeLink;

   public Long getArtId() {
      return artId;
   }

   public void setArtId(Long artId) {
      this.artId = artId;
   }

   public BranchId getBranchId() {
      return branchId;
   }

   public void setBranchId(BranchId branchId) {
      this.branchId = branchId;
   }

   public String getFooter() {
      return footer;
   }

   public void setFooter(String footer) {
      this.footer = footer;
   }

   public boolean getIsEdit() {
      return isEdit;
   }

   public void setIsEdit(boolean isEdit) {
      this.isEdit = isEdit;
   }

   public String getLinkType() {
      return linkType;
   }

   public void setLinkType(String linkType) {
      this.linkType = linkType;
   }

   public TransactionId getTxId() {
      return txId;
   }

   public void setTxId(TransactionId txId) {
      this.txId = txId;
   }

   public String getSessionId() {
      return sessionId;
   }

   public void setSessionId(String sessionId) {
      this.sessionId = sessionId;
   }

   public String getOseeLink() {
      return oseeLink;
   }

   public void setOseeLink(String oseeLink) {
      this.oseeLink = oseeLink;
   }
}
