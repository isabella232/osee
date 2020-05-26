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

package org.eclipse.osee.framework.ui.skynet.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Jeff C. Phillips
 */
public class CompareInput extends CompareEditorInput {
   private static final String CONFIRM_SAVE_PROPERTY = "org.eclipse.compare.internal.CONFIRM_SAVE_PROPERTY";
   private final CompareItem leftCompareItem;
   private final CompareItem rightCompareItem;
   private final CompareItem parentCompareItem;
   private Object differences;
   private final String title;

   public CompareInput(String title, CompareConfiguration compareConfiguration, CompareItem leftCompareItem, CompareItem rightCompareItem, CompareItem parentCompareItem) {
      super(compareConfiguration);
      this.title = title;

      this.leftCompareItem = leftCompareItem;
      this.rightCompareItem = rightCompareItem;
      this.parentCompareItem = parentCompareItem;

      getCompareConfiguration().setProperty(CONFIRM_SAVE_PROPERTY, Boolean.TRUE);
      getCompareConfiguration().setProperty(CompareConfiguration.USE_OUTLINE_VIEW, Boolean.TRUE);
   }

   @Override
   protected Object prepareInput(IProgressMonitor pm) {
      initTitle();

      Differencer differencer = new Differencer();
      differences = differencer.findDifferences(parentCompareItem != null, pm, null, parentCompareItem, leftCompareItem,
         rightCompareItem);
      return differences;
   }

   private void initTitle() {
      CompareConfiguration configuration = getCompareConfiguration();
      String nameLeft = leftCompareItem.getName();
      String nameRight = rightCompareItem.getName();

      configuration.setLeftLabel(nameLeft);
      configuration.setLeftImage(leftCompareItem.getImage());

      configuration.setRightLabel(nameRight);
      configuration.setRightImage(rightCompareItem.getImage());
      if (Strings.isValid(title)) {
         setTitle(title);
      } else {
         setTitle("Compare (" + nameLeft + " - " + nameRight + ")");
      }
   }

   @Override
   public void saveChanges(IProgressMonitor monitor) throws CoreException {
      super.saveChanges(monitor);
      leftCompareItem.persistContent();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof CompareInput) {
         return super.equals(obj);
      }
      return false;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }
}