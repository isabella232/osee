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
package org.eclipse.osee.ats.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.search.AtsArtifactQuery;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;

/**
 * @author Megumi Telles
 */
public class CreateWorkingBranchFromTxBlam extends AbstractBlam {

   private static final String ATS_ID_TX_WIDGET_NAME = "USAGE: Team Workflow ATS ID, TransactionId (pair on each line)";
   private static final int PAIR_SIZE = 2;

   private static final String description =
      "'Copy and paste' or 'type' in Team Workflow ATS ID,Transaction Id from which to create working branch";

   public CreateWorkingBranchFromTxBlam() {
      super(null, description, BlamUiSource.FILE);
   }

   @Override
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      String input = variableMap.getString(ATS_ID_TX_WIDGET_NAME);
      ArrayList<String> idTxs = new ArrayList<String>(Arrays.asList(input.split("\\r?\\n")));
      for (String idTx : idTxs) {
         String[] pairs = idTx.split("[,\\s]+");
         if (pairs.length == PAIR_SIZE) {
            String idNumber = pairs[0];
            TransactionRecord parentTransactionId = TransactionManager.getTransactionId(Integer.valueOf(pairs[1]));
            try {
               Artifact art = AtsArtifactQuery.getArtifactFromId(idNumber);
               if (art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
                  TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) art;
                  if (AtsBranchManagerCore.isCommittedBranchExists(teamArt)) {
                     AWorkbench.popup("Committed branch already exists. Can not create another working branch once changes have been committed.");
                     return;
                  }
                  AtsBranchManagerCore.createWorkingBranch(teamArt, parentTransactionId, true);
               } else {
                  AWorkbench.popup("ERROR", "Must enter a Team Workflow ID");
                  return;
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         } else {
            log(String.format("Skipping Input [%s] - Not in usage format <Team Workflow ID, TransactionId> \n",
               Arrays.toString(pairs)));
         }
      }
   }

   @Override
   public String getXWidgetsXml() throws OseeCoreException {
      return getXWidgetsXmlFromUiFile(getClass().getSimpleName(), Activator.PLUGIN_ID);
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Admin");
   }

}
