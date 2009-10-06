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
package org.eclipse.osee.coverage.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.coverage.editor.ICoverageEditorItem;
import org.eclipse.osee.coverage.editor.xcover.CoverageXViewerFactory;
import org.eclipse.osee.coverage.util.CoverageImage;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.GeneralData;
import org.eclipse.osee.framework.skynet.core.artifact.KeyValueArtifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.OseeImage;
import org.eclipse.swt.graphics.Image;

/**
 * Single code unit (file/procedure/function) that can contain other Coverage Unit or Coverage Items
 * 
 * @author Donald G. Dunne
 */
public class CoverageUnit implements ICoverageEditorItem {

   private String name;
   private String guid = GUID.create();
   private String text;
   private final List<CoverageItem> coverageItems = new ArrayList<CoverageItem>();
   private String location;
   private final List<CoverageUnit> coverageUnits = new ArrayList<CoverageUnit>();
   private CoverageUnit parentCoverageUnit;
   private Artifact artifact;

   public CoverageUnit(CoverageUnit parentCoverageUnit, String name, String location) {
      super();
      this.parentCoverageUnit = parentCoverageUnit;
      this.name = name;
      this.location = location;
   }

   public CoverageUnit(Artifact artifact) throws OseeCoreException {
      this.artifact = artifact;
      load();
   }

   public void addCoverageUnit(CoverageUnit coverageUnit) {
      coverageUnits.add(coverageUnit);
   }

   public List<CoverageUnit> getCoverageUnits() {
      return coverageUnits;
   }

   public void addCoverageItem(CoverageItem coverageItem) {
      coverageItems.add(coverageItem);
   }

   public List<CoverageItem> getCoverageItems(boolean recurse) {
      if (!recurse) {
         return coverageItems;
      }
      List<CoverageItem> items = new ArrayList<CoverageItem>(coverageItems);
      for (CoverageUnit coverageUnit : coverageUnits) {
         items.addAll(coverageUnit.getCoverageItems(true));
      }
      return items;
   }

   public CoverageItem getCoverageItem(String methodNum, String executionLine) {
      for (CoverageItem coverageItem : getCoverageItems(true)) {
         if (coverageItem.getMethodNum().equals(methodNum) && coverageItem.getExecuteNum().equals(executionLine)) {
            return coverageItem;
         }
      }
      return null;
   }

   public CoverageUnit getCoverageUnit(String index) {
      return coverageUnits.get(new Integer(index).intValue() - 1);
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getLocation() {
      return location;
   }

   public void setLocation(String location) {
      this.location = location;
   }

   public String getText() {
      return text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public String getGuid() {
      return guid;
   }

   public CoverageUnit getParentCoverageUnit() {
      return parentCoverageUnit;
   }

   @Override
   public String toString() {
      return getName();
   }

   @Override
   public User getUser() {
      return null;
   }

   @Override
   public Result isEditable() {
      return null;
   }

   @Override
   public void setUser(User user) {
   }

   @Override
   public boolean isCompleted() {
      for (CoverageItem coverageItem : getCoverageItems(true)) {
         if (!coverageItem.isCompleted()) return false;
      }
      return true;
   }

   @Override
   public Object[] getChildren() {
      List<ICoverageEditorItem> children = new ArrayList<ICoverageEditorItem>();
      children.addAll(getCoverageUnits());
      children.addAll(getCoverageItems(false));
      return children.toArray(new Object[children.size()]);
   }

   @Override
   public OseeImage getOseeImage() {
      if (isCovered()) {
         return CoverageImage.UNIT_GREEN;
      }
      return CoverageImage.UNIT_RED;
   }

   @Override
   public boolean isCovered() {
      for (CoverageItem coverageItem : getCoverageItems(true)) {
         if (!coverageItem.isCovered()) return false;
      }
      return true;
   }

   @Override
   public String getCoverageEditorValue(XViewerColumn xCol) {
      if (xCol.equals(CoverageXViewerFactory.Parent_Coverage_Unit)) return getParentCoverageUnit() == null ? "" : getParentCoverageUnit().getName();
      return "";
   }

   @Override
   public Image getCoverageEditorImage(XViewerColumn xCol) {
      return null;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((guid == null) ? 0 : guid.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      CoverageUnit other = (CoverageUnit) obj;
      if (guid == null) {
         if (other.guid != null) return false;
      } else if (!guid.equals(other.guid)) return false;
      return true;
   }

   @Override
   public ICoverageEditorItem getParent() {
      return parentCoverageUnit;
   }

   public Artifact getArtifact(boolean create) throws OseeCoreException {
      if (artifact == null && create) {
         artifact = ArtifactTypeManager.addArtifact(GeneralData.ARTIFACT_TYPE, BranchManager.getCommonBranch());
      }
      return artifact;
   }

   public void load() throws OseeCoreException {
      coverageItems.clear();
      coverageUnits.clear();
      getArtifact(false);
      if (artifact != null) {
         setName(artifact.getName());
         KeyValueArtifact keyValueArtifact =
               new KeyValueArtifact(artifact, GeneralData.GENERAL_STRING_ATTRIBUTE_TYPE_NAME);
         setGuid(keyValueArtifact.getWorkDataValue("guid"));
         for (String line : artifact.getAttributesToStringList(GeneralData.GENERAL_STRING_ATTRIBUTE_TYPE_NAME)) {
            if (line.startsWith("<CvgItem>")) {
               coverageItems.add(new CoverageItem(this, line));
            }
         }
         for (Artifact childArt : artifact.getChildren()) {
            if (childArt.getArtifactTypeName().equals(GeneralData.ARTIFACT_TYPE)) {
               coverageUnits.add(new CoverageUnit(childArt));
            }
         }
      }
   }

   public void save(SkynetTransaction transaction) throws OseeCoreException {
      List<String> items = new ArrayList<String>();
      for (CoverageItem coverageItem : coverageItems) {
         items.add(coverageItem.toXml());
         coverageItem.save(transaction);
      }
      getArtifact(true);
      artifact.setName(getName());
      artifact.setAttributeValues(GeneralData.GENERAL_STRING_ATTRIBUTE_TYPE_NAME, items);
      KeyValueArtifact keyValueArtifact =
            new KeyValueArtifact(artifact, GeneralData.GENERAL_STRING_ATTRIBUTE_TYPE_NAME);
      keyValueArtifact.addWorkDataKeyValue("text", text);
      keyValueArtifact.addWorkDataKeyValue("guid", guid);
      keyValueArtifact.addWorkDataKeyValue("location", text);
      keyValueArtifact.save();
      if (parentCoverageUnit != null) {
         parentCoverageUnit.getArtifact(true).addChild(artifact);
      }
      for (CoverageUnit coverageUnit : coverageUnits) {
         coverageUnit.save(transaction);
      }
      artifact.persist(transaction);
   }

   public void setParentCoverageUnit(CoverageUnit parentCoverageUnit) {
      this.parentCoverageUnit = parentCoverageUnit;
   }

   public void setGuid(String guid) {
      this.guid = guid;
   }
}
