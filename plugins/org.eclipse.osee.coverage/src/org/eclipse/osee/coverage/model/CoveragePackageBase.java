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
package org.eclipse.osee.coverage.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.framework.core.data.FullyNamedIdentity;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.core.util.XResultDataFile;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.KeyValueArtifact;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

/**
 * @author Donald G. Dunne
 */
public abstract class CoveragePackageBase extends FullyNamedIdentity<String> implements ICoverage, ICoverageUnitProvider {
   protected final List<CoverageUnit> coverageUnits = new CopyOnWriteArrayList<CoverageUnit>();
   private final XResultDataFile logResultData = new XResultDataFile(false);
   boolean editable = true;
   protected final CoverageOptionManager coverageOptionManager;
   protected ICoverageUnitFileContentsProvider coverageUnitFileContentsProvider;
   private String editableReason;

   public CoveragePackageBase(String guid, String name, CoverageOptionManager coverageOptionManager) {
      super(guid, name);
      this.coverageOptionManager = coverageOptionManager;
   }

   public abstract Date getDate();

   @Override
   public void addCoverageUnit(CoverageUnit coverageUnit) {
      coverageUnit.setParent(this);
      if (!coverageUnits.contains(coverageUnit)) {
         coverageUnits.add(coverageUnit);
      }
   }

   @Override
   public List<CoverageUnit> getCoverageUnits() {
      return coverageUnits;
   }

   public abstract void getOverviewHtmlHeader(XResultData xResultData);

   public List<CoverageItem> getCoverageItems() {
      List<CoverageItem> items = new ArrayList<CoverageItem>();
      for (CoverageUnit coverageUnit : coverageUnits) {
         items.addAll(coverageUnit.getCoverageItems(true));
      }
      return items;
   }

   public ICoverage getCoverage(String guid) {
      for (ICoverage coverage : getChildren(true)) {
         if (coverage.getGuid().equals(guid)) {
            return coverage;
         }
      }
      return null;
   }

   @Override
   public String getCoveragePercentStr() {
      return CoverageUtil.getPercent(getCoverageItemsCovered().size(), getCoverageItems().size(), true).getSecond();
   }

   @Override
   public Double getCoveragePercent() {
      return CoverageUtil.getPercent(getCoverageItemsCovered().size(), getCoverageItems().size(), true).getFirst();
   }

   public List<CoverageItem> getCoverageItemsCovered() {
      List<CoverageItem> items = new ArrayList<CoverageItem>();
      for (CoverageItem coverageItem : getCoverageItems()) {
         if (coverageItem.getCoverageMethod() == null) {
            OseeLog.log(Activator.class, Level.SEVERE,
               String.format("Coverage Item with null Coverage Method; Item [%s]", coverageItem.toString()));
         } else if (!coverageItem.getCoverageMethod().getName().equals(CoverageOptionManager.Not_Covered.name)) {
            items.add(coverageItem);
         }
      }
      return items;
   }

   public int getCoverageUnitCount(boolean recurse) {
      int count = 0;
      if (!recurse) {
         count = coverageUnits.size();
      } else {
         for (CoverageUnit coverageUnit : coverageUnits) {
            count += coverageUnit.getCoverageUnitCount(true);
         }
      }
      return count;
   }

   public int getCoverageItemsCount(CoverageOption coverageOption) {
      int count = 0;
      for (CoverageUnit coverageUnit : coverageUnits) {
         count += coverageUnit.getCoverageItemsCount(true, coverageOption);
      }
      return count;
   }

   @Override
   public Collection<? extends ICoverage> getChildren(boolean recurse) {
      Set<ICoverage> items = new HashSet<ICoverage>();
      for (CoverageUnit coverageUnit : coverageUnits) {
         items.add(coverageUnit);
         if (recurse) {
            items.addAll(coverageUnit.getChildren(recurse));
         }
      }
      return items;
   }

   public synchronized CoverageUnit getOrCreateParent(String namespace) {
      // Look for already existing CU
      for (ICoverage item : getChildren(true)) {
         if (!(item instanceof CoverageUnit)) {
            continue;
         }
         CoverageUnit coverageUnit = (CoverageUnit) item;
         if (coverageUnit.getName().equals(namespace)) {
            return coverageUnit;
         }
      }
      // Create
      String[] names = namespace.split("\\.");
      String nameStr = "";
      for (String name : names) {
         if (nameStr.equals("")) {
            nameStr = name;
         } else {
            nameStr = nameStr + "." + name;
         }
         if (coverageUnits.isEmpty()) {
            CoverageUnit newCoverageUnit =
               CoverageUnitFactory.createCoverageUnit(this, nameStr, "", coverageUnitFileContentsProvider);
            newCoverageUnit.setFolder(true);
            newCoverageUnit.setNamespace(nameStr);
            addCoverageUnit(newCoverageUnit);
            if (nameStr.equals(namespace)) {
               return newCoverageUnit;
            }
            continue;
         }

         // Look for already existing CU
         boolean found = false;
         for (ICoverage item : getChildren(true)) {
            if (!(item instanceof CoverageUnit)) {
               continue;
            }
            if (item.getName().equals(nameStr)) {
               found = true;
               break;
            }
         }
         if (found) {
            continue;
         }

         // Create one if not exists

         // Find parent
         ICoverage parent = null;
         if (nameStr.equals(name)) {
            parent = this;
         } else {
            String parentNamespace = nameStr.replaceFirst("\\." + name + ".*$", "");
            parent = getOrCreateParent(parentNamespace);
         }
         // Create new coverage unit
         CoverageUnit newCoverageUnit =
            CoverageUnitFactory.createCoverageUnit(parent, nameStr, "", coverageUnitFileContentsProvider);
         newCoverageUnit.setNamespace(nameStr);
         newCoverageUnit.setFolder(true);
         // Add to parent
         ((ICoverageUnitProvider) parent).addCoverageUnit(newCoverageUnit);
         // Return if this is our coverage unit
         if (nameStr.equals(namespace)) {
            return newCoverageUnit;
         }
      }
      return null;
   }

   public XResultDataFile getLog() {
      return logResultData;
   }

   public boolean isImportAllowed() {
      return isEditable().isTrue();
   }

   @Override
   public boolean isAssignable() {
      return isEditable().isTrue();
   }

   @Override
   public Collection<? extends ICoverage> getChildren() {
      return getChildren(false);
   }

   @Override
   public boolean isCovered() {
      for (CoverageUnit coverageUnit : coverageUnits) {
         if (!coverageUnit.isCovered()) {
            return false;
         }
      }
      return true;
   }

   @Override
   public Result isEditable() {
      if (!editable) {
         return new Result(editableReason);
      }
      return Result.TrueResult;
   }

   public void setEditable(boolean editable, String editableReason) {
      this.editable = editable;
      this.editableReason = editableReason;
   }

   @Override
   public void removeCoverageUnit(CoverageUnit coverageUnit) {
      coverageUnits.remove(coverageUnit);
   }

   @Override
   public KeyedImage getOseeImage() {
      return null;
   }

   @Override
   public String getLocation() {
      return "";
   }

   @Override
   public String getFileContents() {
      return "";
   }

   @Override
   public String getNamespace() {
      return "";
   }

   @Override
   public String getNotes() {
      return null;
   }

   @Override
   public ICoverage getParent() {
      return null;
   }

   @Override
   public String getAssignees() {
      return "";
   }

   public abstract void saveKeyValues(KeyValueArtifact keyValueArtifact) throws OseeCoreException;

   public abstract void loadKeyValues(KeyValueArtifact keyValueArtifact) throws OseeCoreException;

   @Override
   public boolean isFolder() {
      return false;
   }

   @Override
   public String getOrderNumber() {
      return "";
   }

   public CoverageOptionManager getCoverageOptionManager() {
      return coverageOptionManager;
   }

   public ICoverageUnitFileContentsProvider getCoverageUnitFileContentsProvider() {
      return coverageUnitFileContentsProvider;
   }

   public void setCoverageUnitFileContentsProvider(ICoverageUnitFileContentsProvider coverageUnitFileContentsProvider) {
      this.coverageUnitFileContentsProvider = coverageUnitFileContentsProvider;
   }

   public CoverageUnit createCoverageUnit(ICoverage parent, String name, String location) {
      return CoverageUnitFactory.createCoverageUnit(parent, name, location, coverageUnitFileContentsProvider);
   }

   public String getEditableReason() {
      return editableReason;
   }

}