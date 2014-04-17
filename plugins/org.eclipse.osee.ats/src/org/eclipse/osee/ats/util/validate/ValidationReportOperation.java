/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.util.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilClient;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsBranchManager;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.OperationLogger;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.revision.ChangeData;
import org.eclipse.osee.framework.skynet.core.revision.ChangeData.KindType;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;

/**
 * @author Donald G. Dunne
 */
public class ValidationReportOperation extends AbstractOperation {

   private final TeamWorkFlowArtifact teamArt;
   private final Set<AbstractValidationRule> rules;

   public ValidationReportOperation(OperationLogger logger, TeamWorkFlowArtifact teamArt, Set<AbstractValidationRule> rules) {
      super("Validate Requirement Changes - " + teamArt.getName(), Activator.PLUGIN_ID, logger);
      this.teamArt = teamArt;
      this.rules = rules;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      logf("<b>Validating Requirement Changes for %s</b>\n", teamArt.getName());

      List<AbstractValidationRule> rulesSorted = new ArrayList<AbstractValidationRule>(rules);
      Collections.sort(rulesSorted, new ValidationRuleComparator());

      for (AbstractValidationRule rule : rulesSorted) {
         log(rule.getRuleDescription());
      }
      log("<br><br><b>NOTE: </b>All errors are shown for artifact state on branch or at time of commit.  Select hyperlink to open most recent version of artifact.");

      try {
         ChangeData changeData = AtsBranchManager.getChangeDataFromEarliestTransactionId(teamArt);
         Collection<Artifact> changedArtifacts =
            changeData.getArtifacts(KindType.ArtifactOrRelation, ModificationType.NEW, ModificationType.MODIFIED);
         checkForCancelledStatus(monitor);

         double total = changedArtifacts.size() + rules.size();
         if (total > 0) {
            Collection<String> warnings = new ArrayList<String>();

            int workAmount = calculateWork(1 / total);

            String lastTitle = "";
            int ruleIndex = 1;
            for (AbstractValidationRule rule : rulesSorted) {
               checkForCancelledStatus(monitor);

               //check to see if we should print a header for sorted (and grouped) rule types
               String currentTitle = rule.getRuleTitle();
               if (!lastTitle.equals(currentTitle)) {
                  logf("\n%s", currentTitle);
                  lastTitle = currentTitle;
               }

               int artIndex = 1;
               for (Artifact art : changedArtifacts) {
                  monitor.setTaskName(String.format("Validating: Rule[%s of %s] Artifact[%s of %s]", ruleIndex,
                     rules.size(), artIndex, changedArtifacts.size()));
                  checkForCancelledStatus(monitor);

                  ValidationResult result = rule.validate(art, monitor);
                  if (!result.didValidationPass()) {
                     for (String errorMsg : result.getErrorMessages()) {
                        if (art.isOfType(CoreArtifactTypes.DirectSoftwareRequirement)) {
                           logf("Error: %s", errorMsg);
                        } else {
                           warnings.add(String.format("Warning: %s", errorMsg));
                        }
                     }
                  }
                  monitor.worked(workAmount);
                  artIndex++;
               }
               ruleIndex++;
            }

            // print warnings at the end of the report
            for (String warning : warnings) {
               log(warning);
            }
         }

         log("\nValidation Complete");
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         logf("Error: %s", ex.getLocalizedMessage());
      }
   }

   private final class ValidationRuleComparator implements Comparator<AbstractValidationRule> {

      @Override
      public int compare(AbstractValidationRule o1, AbstractValidationRule o2) {
         String title1 = o1.getRuleTitle();
         String title2 = o2.getRuleTitle();
         if (title1 == null) {
            title1 = "";
         }
         if (title2 == null) {
            title2 = "";
         }
         return title1.compareTo(title2);
      }
   }

   public static String getRequirementHyperlink(Artifact art) throws OseeCoreException {
      String atsId = AtsUtilClient.getAtsId(art);
      String linkName = String.format("%s(%s)", art.getName(), atsId);
      return XResultDataUI.getHyperlink(linkName, atsId, art.getFullBranch().getUuid());
   }
}