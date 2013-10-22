/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.coverage.editor.params;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageOption;
import org.eclipse.osee.coverage.model.CoveragePackageBase;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.model.IWorkProductRelatable;
import org.eclipse.osee.coverage.store.OseeCoverageUnitStore;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;

/**
 * @author Donald G. Dunne
 */
public class CoverageParameters {

   private final CoveragePackageBase coveragePackageBase;
   private final List<CoverageOption> coverageMethods = new ArrayList<CoverageOption>();
   private String name;
   private String namespace;
   private String rationale;
   private String notes;
   private String workProductTasks;
   private User assignee;
   private boolean showAll = true;

   public CoverageParameters(CoveragePackageBase coveragePackageBase) {
      this.coveragePackageBase = coveragePackageBase;
   }

   /**
    * Returns a collection of ICoverage items that matched and a collection of all top level ICoverage parents
    */
   public Pair<Set<ICoverage>, Set<ICoverage>> performSearchGetResults() throws OseeCoreException {
      Set<ICoverage> items = new HashSet<ICoverage>();
      for (ICoverage coverageItem : coveragePackageBase.getChildren(false)) {
         performSearchGetResults(items, coverageItem);
      }
      Set<ICoverage> parents = new HashSet<ICoverage>();
      for (ICoverage coverage : items) {
         parents.add(CoverageUtil.getTopLevelCoverageUnit(coverage));
      }
      return new Pair<Set<ICoverage>, Set<ICoverage>>(items, parents);
   }

   // CoverageUnit - name, namespace, assignees, notes
   // CoverageItem - name, namespace, rationale, coverageMethod
   public void performSearchGetResults(Set<ICoverage> matchItems, ICoverage coverage) throws OseeCoreException {
      if (coverage instanceof CoverageItem) {
         if (isCoverageMethodMatch(coverage) &&
         //
         isRationaleMatch(coverage) &&
         //
         isNameMatch(coverage) &&
         //
         isNamespaceMatch(coverage)) {
            // CoverageItem matches search criteria; validate CoverageUnits up the hierarchy
            if (doesParentMatchCriteria(coverage.getParent())) {
               matchItems.add(coverage);
            }
         }
      } else if (coverage instanceof CoverageUnit) {
         if (Strings.isValid(name) || Strings.isValid(namespace) || Strings.isValid(notes) || Strings.isValid(workProductTasks) || assignee != null) {
            boolean folder = ((CoverageUnit) coverage).isFolder();
            boolean nameMatch = isNameMatch(coverage);
            boolean namespaceMatch = isNamespaceMatch(coverage);
            boolean notesMatch = isNotesMatch(coverage);
            boolean workProductTasksMatch = isWorkProductTasksMatch(coverage);
            boolean assigneeMatch = isAssigneeMatch(coverage);
            if (!folder && nameMatch && namespaceMatch && notesMatch && workProductTasksMatch && assigneeMatch) {
               matchItems.add(coverage);
               // If CoverageUnit matches, include all coverage items in match
               for (CoverageItem coverageItem : ((CoverageUnit) coverage).getCoverageItems(true)) {
                  // Don't check name cause name of coverge unit won't match name of item
                  // Checking namespace shouldn't matter cause children will have namespace of parent
                  if (isCoverageMethodMatch(coverageItem) &&
                  //
                  isRationaleMatch(coverageItem) &&
                  //
                  isNamespaceMatch(coverageItem) &&
                  //
                  isWorkProductTasksMatch(coverageItem)) {
                     matchItems.add(coverageItem);
                  }
               }
            }
         }
      }
      for (ICoverage child : coverage.getChildren()) {
         performSearchGetResults(matchItems, child);
      }
   }

   /**
    * Recurse up parent tree to ensure all parents match criteria
    */
   private boolean doesParentMatchCriteria(ICoverage coverage) throws OseeCoreException {
      if ((isNameMatch(coverage) || isNamespaceMatch(coverage)) && isNotesMatch(coverage) && isWorkProductTasksMatch(coverage) && isAssigneeMatch(coverage)) {
         return true;
      } else if (coverage.getParent() instanceof CoverageUnit) {
         return doesParentMatchCriteria(coverage.getParent());
      }
      return false;
   }

   /**
    * Match if no assignee specified OR<br>
    * coverage isn't CoverageUnit OR<br>
    * CoverageUnit assignee equals search assignee
    */
   public boolean isAssigneeMatch(ICoverage coverage) {
      if (assignee == null || !(coverage instanceof CoverageUnit)) {
         return true;
      }
      if (OseeCoverageUnitStore.getAssignees((CoverageUnit) coverage).contains(assignee)) {
         return true;
      }
      return false;
   }

   /**
    * Match if no notes specified OR<br>
    * coverage isn't CoverageUnit OR<br>
    * CoverageUnit notes contains search string
    */
   public boolean isNotesMatch(ICoverage coverage) {
      if (!Strings.isValid(notes) || !(coverage instanceof CoverageUnit)) {
         return true;
      }
      if (!Strings.isValid(((CoverageUnit) coverage).getNotes())) {
         return false;
      }
      if (((CoverageUnit) coverage).getNotes().contains(notes)) {
         return true;
      }
      return false;
   }

   /**
    * Match if no workProductTasks specified OR<br>
    * ICoverage workProductTasks name contains search string
    */
   public boolean isWorkProductTasksMatch(ICoverage coverage) {
      if (!Strings.isValid(workProductTasks) || !(coverage instanceof IWorkProductRelatable)) {
         return true;
      }
      if (coverage.getWorkProductTaskStr().contains(workProductTasks)) {
         return true;
      }
      if (coverage instanceof CoverageUnit) {
         for (CoverageItem childCoverageItem : ((CoverageUnit) coverage).getCoverageItems()) {
            if (isWorkProductTasksMatch(childCoverageItem)) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Match if no name specified OR<br>
    * item name contains search string
    */
   public boolean isNameMatch(ICoverage coverage) {
      if (!Strings.isValid(name)) {
         return true;
      }
      if (!Strings.isValid(coverage.getName())) {
         return false;
      }
      if (coverage.getName().contains(name)) {
         return true;
      }
      return false;
   }

   /**
    * Match if no namespace specified OR<br>
    * item namespace contains search string
    */
   public boolean isNamespaceMatch(ICoverage coverage) {
      if (!Strings.isValid(namespace)) {
         return true;
      }
      if (!Strings.isValid(coverage.getNamespace())) {
         return false;
      }
      if (coverage.getNamespace().contains(namespace)) {
         return true;
      }
      return false;
   }

   /**
    * Match if no rationale specified OR<br>
    * coverage isn't CoverageItem OR<br>
    * CoverageItem rationale contains search string
    */
   public boolean isRationaleMatch(ICoverage coverage) {
      if (!Strings.isValid(rationale) || !(coverage instanceof CoverageItem)) {
         return true;
      }
      if (!Strings.isValid(((CoverageItem) coverage).getRationale())) {
         return false;
      }
      if (((CoverageItem) coverage).getRationale().contains(rationale)) {
         return true;
      }
      return false;
   }

   /**
    * Match if no coverageMethods specified OR<br>
    * coverage isn't CoverageItem OR<br>
    * CoverageItem is method specified
    */
   public boolean isCoverageMethodMatch(ICoverage coverage) {
      if (coverageMethods.isEmpty() || !(coverage instanceof CoverageItem)) {
         return true;
      }
      if (coverageMethods.contains(((CoverageItem) coverage).getCoverageMethod())) {
         return true;
      }
      return false;
   }

   public String getSelectedName(/* SearchType searchType */) {
      StringBuffer sb = new StringBuffer();
      if (getAssignee() != null) {
         sb.append(" - Assignee: " + getAssignee());
      }
      if (Strings.isValid(getName())) {
         sb.append(" - Name: " + getName());
      }
      if (Strings.isValid(getNamespace())) {
         sb.append(" - Namespace: " + getNamespace());
      }
      if (Strings.isValid(getRationale())) {
         sb.append(" - Rationale: " + getRationale());
      }
      if (Strings.isValid(getNotes())) {
         sb.append(" - Notes: " + getNotes());
      }
      if (getSelectedCoverageMethods().size() > 1) {
         sb.append(" - Coverage Method: " + org.eclipse.osee.framework.jdk.core.util.Collections.toString(", ",
            getSelectedCoverageMethods()));
      }
      return "Coverage Items " + sb.toString();
   }

   public String getNotesStr() {
      return notes;
   }

   public User getAssignee() {
      return assignee;
   }

   public Collection<CoverageOption> getSelectedCoverageMethods() {
      return coverageMethods;
   }

   public Result isParameterSelectionValid() {
      try {
         return Result.TrueResult;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         return new Result("Exception: " + ex.getLocalizedMessage());
      }
   }

   public CoveragePackageBase getCoveragePackageBase() {
      return coveragePackageBase;
   }

   public Collection<CoverageOption> getCoverageMethods() {
      return coverageMethods;
   }

   public void setCoverageMethods(Collection<CoverageOption> coverageMethods) {
      if (coverageMethods == null) {
         this.coverageMethods.clear();
      }
      this.coverageMethods.clear();
      this.coverageMethods.addAll(coverageMethods);
      updateShowAll();
   }

   public String getNotes() {
      return notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
      updateShowAll();
   }

   public void clearAll() {
      setAssignee(null);
      setNotes(null);
      setWorkProductTasks(null);
      setNamespace(null);
      setRationale(null);
      setName(null);
      this.coverageMethods.clear();
   }

   private void updateShowAll() {
      this.showAll =
         getSelectedCoverageMethods().isEmpty() && getAssignee() == null && !Strings.isValid(getNotesStr()) && !Strings.isValid(getNamespace()) && !Strings.isValid(getWorkProductTasks()) && !Strings.isValid(getRationale()) && !Strings.isValid(getName());
   }

   public boolean isShowAll() {
      return this.showAll;
   }

   public void setAssignee(User assignee) {
      this.assignee = assignee;
      updateShowAll();
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
      updateShowAll();
   }

   public String getNamespace() {
      return namespace;
   }

   public void setNamespace(String namespace) {
      this.namespace = namespace;
      updateShowAll();
   }

   public String getRationale() {
      return rationale;
   }

   public void setRationale(String rationale) {
      this.rationale = rationale;
      updateShowAll();
   }

   public String getWorkProductTasks() {
      return workProductTasks;
   }

   public void setWorkProductTasks(String workProductTasks) {
      this.workProductTasks = workProductTasks;
      updateShowAll();
   }

}
