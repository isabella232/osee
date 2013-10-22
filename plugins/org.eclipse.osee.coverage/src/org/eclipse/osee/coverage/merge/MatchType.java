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
package org.eclipse.osee.coverage.merge;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.osee.coverage.model.CoverageImport;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoveragePackage;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;

public enum MatchType {
   // Item matches in name only
   Match__Folder,
   // Item matches in name and order number
   Match__Name_And_Order_Num,
   // Items are CoveragePackageBase types
   Match__Coverage_Base,

   // Item has no match; __<reason>
   No_Match__Namespace,
   No_Match__Class,
   No_Match__Name_Or_Order_Num;

   public static Collection<MatchType> FullMatches = Arrays.asList(Match__Folder, Match__Coverage_Base,
      Match__Name_And_Order_Num);

   public static boolean isNoMatch(MatchType matchType) {
      return matchType.toString().startsWith("No_Match__");
   }

   public static boolean isMatch(MatchType matchType) {
      return FullMatches.contains(matchType);
   }

   public static MatchType getMatchType(ICoverage packageItem, ICoverage importItem) throws OseeStateException {
      if (packageItem instanceof CoveragePackage && importItem instanceof CoverageImport) {
         return MatchType.Match__Coverage_Base;
      }
      if (packageItem.getClass() != importItem.getClass()) {
         return MatchType.No_Match__Class;
      }
      if (packageItem.getNamespace() == null || importItem.getNamespace() == null) {
         throw new OseeStateException("Namespaces can't be null");
      }
      if (!packageItem.getNamespace().equals(importItem.getNamespace())) {
         return MatchType.No_Match__Namespace;
      }
      boolean namesEqual = packageItem.getName().equals(importItem.getName());
      boolean orderNumsEqual = packageItem.getOrderNumber().equals(importItem.getOrderNumber());
      if (packageItem instanceof CoverageUnit && importItem instanceof CoverageUnit) {
         if (((CoverageUnit) packageItem).isFolder() && ((CoverageUnit) importItem).isFolder() && namesEqual) {
            return MatchType.Match__Folder;
         }
         if (((CoverageUnit) packageItem).isFolder() && !((CoverageUnit) importItem).isFolder()) {
            return MatchType.No_Match__Class;
         }
         if (!((CoverageUnit) packageItem).isFolder() && ((CoverageUnit) importItem).isFolder()) {
            return MatchType.No_Match__Class;
         }
         // If names equal and method numbers equal
         if (namesEqual && orderNumsEqual) {
            // parent's have to match also to be considered equal
            MatchType matchType = getMatchType(packageItem.getParent(), importItem.getParent());
            // if parents match, then this is a full match
            if (MatchType.isMatch(matchType)) {
               return MatchType.Match__Name_And_Order_Num;
            }
            // if parents don't match, then this is a full no-match
            else {
               return No_Match__Name_Or_Order_Num;
            }
         }
         // If neither names or methods match
         else if (!namesEqual && !orderNumsEqual) {
            return MatchType.No_Match__Name_Or_Order_Num;
         }
      } else if (packageItem instanceof CoverageItem && importItem instanceof CoverageItem) {
         // If neither names or order match
         if (!namesEqual && !orderNumsEqual) {
            return MatchType.No_Match__Name_Or_Order_Num;
         }
         // If names equal and order numbers equal
         else if (namesEqual && orderNumsEqual) {
            // parent's have to match also to be considered equal
            MatchType matchType = getMatchType(packageItem.getParent(), importItem.getParent());
            // if parents match, then this is a full match
            if (MatchType.isMatch(matchType)) {
               return MatchType.Match__Name_And_Order_Num;
            }
         }
      } else if (namesEqual) {
         if (packageItem.getParent() instanceof CoveragePackage && importItem.getParent() instanceof CoverageImport) {
            return MatchType.Match__Name_And_Order_Num;
         } else {
            if (packageItem.getParent() == null && importItem.getParent() == null) {
               return MatchType.Match__Coverage_Base;
            }
            MatchType matchType = getMatchType(packageItem.getParent(), importItem.getParent());
            return matchType;
         }
      }
      return MatchType.No_Match__Name_Or_Order_Num;
   }

};
