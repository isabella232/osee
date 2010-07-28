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
package org.eclipse.osee.coverage.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.coverage.model.CoveragePackageBase;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Donald G. Dunne
 */
public class CoverageEditorInput implements IEditorInput {

   private CoveragePackageBase coveragePackageBase;
   private Artifact coveragePackageArtifact;
   private final String preLoadName;
   private final boolean isInTest;

   public CoverageEditorInput(String preLoadName, Artifact coveragePackageArtifact, CoveragePackageBase coveragePackageBase, boolean isInTest) {
      this.preLoadName = preLoadName;
      this.coveragePackageArtifact = coveragePackageArtifact;
      this.coveragePackageBase = coveragePackageBase;
      this.isInTest = isInTest;
   }

   @Override
   public int hashCode() {
      return coveragePackageBase.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof CoverageEditorInput)) {
         return false;
      }
      CoverageEditorInput castObj = (CoverageEditorInput) obj;
      if (castObj.getCoveragePackageBase() != null) {
         return castObj.getCoveragePackageBase().equals(this.coveragePackageBase);
      } else if (castObj.getCoveragePackageArtifact() != null) {
         return castObj.getCoveragePackageArtifact().equals(this.coveragePackageArtifact);
      }
      return false;
   }

   @Override
   public boolean exists() {
      return false;
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return null;
   }

   @Override
   public IPersistableElement getPersistable() {
      return null;
   }

   @Override
   public String getToolTipText() {
      return "";
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class adapter) {
      return null;
   }

   @Override
   public String getName() {
      if (coveragePackageBase == null) {
         return getPreLoadName();
      }
      return coveragePackageBase.getName();
   }

   public CoveragePackageBase getCoveragePackageBase() {
      return coveragePackageBase;
   }

   public Artifact getCoveragePackageArtifact() {
      return coveragePackageArtifact;
   }

   public String getPreLoadName() {
      return preLoadName;
   }

   public void setCoveragePackageBase(CoveragePackageBase coveragePackageBase) {
      this.coveragePackageBase = coveragePackageBase;
   }

   public boolean isInTest() {
      return isInTest;
   }

   public void setCoveragePackageArtifact(Artifact coveragePackageArtifact) {
      this.coveragePackageArtifact = coveragePackageArtifact;
   }

}
