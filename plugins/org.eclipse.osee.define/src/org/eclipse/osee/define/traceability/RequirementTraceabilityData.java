/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.define.traceability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.define.internal.Activator;
import org.eclipse.osee.define.traceability.data.RequirementData;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Roberto E. Escobar
 */
public class RequirementTraceabilityData {
   private final IOseeBranch requirementsBranch;
   private final IOseeBranch testProcedureBranch;
   private final TraceabilityProviderOperation traceabilityProvider;
   private RequirementData requirementData;
   private HashCollection<Artifact, String> requirementsToCodeUnits;
   private final HashCollection<String, Artifact> requirementNameToTestProcedures =
      new HashCollection<String, Artifact>();
   private final Set<String> codeUnits = new TreeSet<String>();
   private final Map<String, Artifact> testProcedures = new HashMap<String, Artifact>();
   private final Map<String, Artifact> testScripts = new HashMap<String, Artifact>();
   private File testProcedureFilter;

   public RequirementTraceabilityData(IOseeBranch requirementsBranch, IOseeBranch testProcedureBranch, TraceabilityProviderOperation traceabilityProvider) {
      this.requirementsBranch = requirementsBranch;
      this.testProcedureBranch = testProcedureBranch;
      this.traceabilityProvider = traceabilityProvider;
      this.testProcedureFilter = null;
      reset();
   }

   private void reset() {
      this.codeUnits.clear();
      this.testProcedures.clear();
      this.testScripts.clear();
      this.requirementsToCodeUnits = null;
      this.requirementData = null;
      this.requirementsToCodeUnits = null;
   }

   public IStatus initialize(IProgressMonitor monitor) {
      IStatus status = Status.CANCEL_STATUS;
      try {
         reset();
         monitor.subTask("Loading traceability data...");
         status = Operations.executeWork(traceabilityProvider, monitor);

         this.requirementData = traceabilityProvider.getRequirementData();
         this.requirementsToCodeUnits = traceabilityProvider.getRequirementToCodeUnitsMap();
         this.codeUnits.addAll(traceabilityProvider.getCodeUnits());

         if (monitor.isCanceled() != true) {
            if (status.getSeverity() == IStatus.OK) {
               getTestProcedureTraceability(testProcedureBranch);
            }
         } else {
            status = Status.CANCEL_STATUS;
         }
      } catch (Exception ex) {
         status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error gathering traceability data", ex);
      }
      return status;
   }

   private void getTestProcedureTraceability(IOseeBranch testProcedureBranch) throws OseeCoreException {
      // Map Software Requirements from TestProcedure IOseeBranch to Requirements IOseeBranch
      Map<String, Artifact> testProcedureBranchReqsToReqsBranchMap = new HashMap<String, Artifact>();
      for (Artifact tpRequirement : ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.SoftwareRequirement,
         testProcedureBranch)) {
         testProcedureBranchReqsToReqsBranchMap.put(tpRequirement.getName(), tpRequirement);
      }

      Set<String> testProceduresFilter = getAllowedTestProcedures();
      for (Entry<String, Artifact> entry : testProcedureBranchReqsToReqsBranchMap.entrySet()) {
         Artifact requirement = entry.getValue();
         Set<Artifact> foundProcedures =
            new HashSet<Artifact>(requirement.getRelatedArtifacts(CoreRelationTypes.Validation__Validator));
         Set<Artifact> toAdd = new HashSet<Artifact>();
         if (testProceduresFilter.isEmpty() != true) {
            for (Artifact artifact : foundProcedures) {
               if (testProceduresFilter.contains(artifact.getName())) {
                  toAdd.add(artifact);
               }
            }
         } else {
            toAdd = foundProcedures;
         }
         requirementNameToTestProcedures.put(entry.getKey(), toAdd);
         for (Artifact artifact : toAdd) {
            this.testProcedures.put(artifact.getName(), artifact);
         }
      }
   }

   public Collection<Artifact> getDirectSwRequirements() {
      return requirementData.getDirectRequirements();
   }

   public Collection<Artifact> getAllSwRequirements() {
      return requirementData.getAllRequirements();
   }

   /**
    * Get Requirement Artifact based on traceMark mark if it fails, check if trace mark is a structured requirement and
    * try again
    * 
    * @return requirement artifact
    */
   public Artifact getRequirementFromTraceMark(String traceMark) {
      return this.requirementData.getRequirementFromTraceMarkIncludeStructuredRequirements(traceMark);
   }

   /**
    * @return the requirementsToCodeUnits
    */
   public HashCollection<Artifact, String> getRequirementsToCodeUnits() {
      return requirementsToCodeUnits;
   }

   /**
    * @return the codeUnits
    */
   public Set<String> getCodeUnits() {
      return codeUnits;
   }

   /**
    * @return the testProcedures
    */
   public Set<String> getTestProcedures() {
      return testProcedures.keySet();
   }

   /**
    * @return the requirementNameToTestProcedures
    */
   public HashCollection<String, Artifact> getRequirementNameToTestProcedures() {
      return requirementNameToTestProcedures;
   }

   /**
    * @param artifact requirement to find
    * @return the test procedures associated with this requirement
    */
   public Collection<Artifact> getTestProceduresForRequirement(Artifact requirement) {
      return getRequirementNameToTestProcedures().getValues(requirement.getName());
   }

   /**
    * TODO need to use persisted test script NOTE: These artifact should not be persisted since they are temporary items
    * artifacts
    * 
    * @param artifact requirement to find
    * @return the test scripts associated with this requirement
    * @throws OseeCoreException
    */
   public Collection<Artifact> getTestScriptsForRequirement(Artifact artifact) throws OseeCoreException {
      Collection<Artifact> testUnitArtifacts = traceabilityProvider.getTestUnitArtifacts(artifact);
      for (Artifact art : testUnitArtifacts) {
         testScripts.put(getScriptName(art.getName()), art);
      }
      return testUnitArtifacts;
   }

   public Artifact getTestProcedureByName(String name) {
      return testProcedures.get(name);
   }

   public Artifact getTestScriptByName(String name) {
      return testScripts.get(name);
   }

   private String getScriptName(String name) {
      int endOfPackageName = name.lastIndexOf(".");
      if (endOfPackageName != -1) {
         name = name.substring(endOfPackageName + 1) + ".java";
      }
      return name;
   }

   public void setTestProcedureFilterFile(File filter) {
      this.testProcedureFilter = filter;
   }

   private File getTestProcedureFilterFile() {
      return testProcedureFilter;
   }

   private Set<String> getAllowedTestProcedures() {
      Set<String> toReturn = new HashSet<String>();
      File filter = getTestProcedureFilterFile();
      if (filter != null && filter.exists() && filter.canRead()) {
         BufferedReader reader = null;
         try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filter)));
            String line = "";
            while ((line = reader.readLine()) != null) {
               toReturn.add(line.trim());
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         } finally {
            if (reader != null) {
               try {
                  reader.close();
               } catch (IOException ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               }
            }
         }
      }
      return toReturn;
   }
}
