/*********************************************************************
 * Copyright (c) 2018 Boeing
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

package org.eclipse.osee.framework.core.dsl.ui.contentassist;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;

/**
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#contentAssist on how to customize content assistant
 * 
 * @author Roberto E. Escobar
 */
public class OseeDslProposalProvider extends AbstractOseeDslProposalProvider {

   @Override
   public void completeXAttributeTypeRef_BranchUuid(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXAttributeTypeRef_BranchUuid(model, assignment, context, acceptor);
      completeIdGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   }

   @Override
   public void complete_AddEnum(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.complete_AddEnum(model, ruleCall, context, acceptor);
   }

   @Override
   public void completeAccessContext_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeAccessContext_Id(model, assignment, context, acceptor);
      completeIdGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   }

   @Override
   public void completeXArtifactType_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXArtifactType_Id(model, assignment, context, acceptor);
      completeRemoteTypeIdGeneration(context, acceptor);
   }

   @Override
   public void completeXAttributeType_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXAttributeType_Id(model, assignment, context, acceptor);
      completeRemoteTypeIdGeneration(context, acceptor);
   }

   @Override
   public void completeXOseeEnumType_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXOseeEnumType_Id(model, assignment, context, acceptor);
      completeRemoteTypeIdGeneration(context, acceptor);
   }

   @Override
   public void completeXRelationType_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXRelationType_Id(model, assignment, context, acceptor);
      completeRemoteTypeIdGeneration(context, acceptor);
   }

   private void completeIdGeneration(RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      if (acceptor.canAcceptMoreProposals()) {
         String generatedId = String.format("\"%s\"", Lib.generateArtifactIdAsInt());
         String displayProposalAs = generatedId + "- ID";
         ICompletionProposal proposal = createCompletionProposal(generatedId, displayProposalAs, null, context);
         acceptor.accept(proposal);
      }
   }

   private void completeRemoteTypeIdGeneration(ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      if (acceptor.canAcceptMoreProposals()) {
         String generatedRemoteId = "0x0000000000000000";
         String displayProposalAs = generatedRemoteId + "- RemoteTypeId";
         ICompletionProposal proposal = createCompletionProposal(generatedRemoteId, displayProposalAs, null, context);
         acceptor.accept(proposal);
      }
   }
}
