/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.change;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.change.ArtifactChangeItem;
import org.eclipse.osee.framework.core.model.change.AttributeChangeItem;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.core.model.change.ChangeVersion;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * {@link ChangeItem}
 * 
 * <pre>
 *                                        SRC Art
 *                        Delete | Modified | Merged  | Introduce
 *                      -----------------------------------------
 *    DST Art Delete    |    1   |     2    |   3     |   4      |
 *            Modified  |    5   |     6    |   7     |   8      |
 *            Merged    |    9   |     10   |   11    |   12     |
 *            Introduce |    13  |     14   |   15    |   16     |
 *                      -----------------------------------------
 * 
 *                               SRC Attr
 *                        Delete | Modified | Merged
 *                      ------------------------------
 *  DST Attr  Delete    |    1   |     2    |    5   |
 *            Modified  |    3   |     4    |    6   |
 *            Merged    |    7   |     8    |    9   |
 *                      ------------------------------
 * </pre>
 * 
 * @author Jeff C. Phillips
 * @author Karol M. Wilk
 * @author Ryan D. Brooks
 */
@RunWith(Parameterized.class)
public class ChangeItemConflictTest {
   private static final long GAMMA = 100L;

   private final ChangeItemCase srcCase;
   private final ItemType itemType;
   private final ChangeItemCase dstCase;
   private final String testMessage;

   public static enum ChangeItemCase {
      SRC_DEL,
      SRC_MOD,
      SRC_MRG,
      SRC_INT,
      DST_DEL,
      DST_MOD,
      DST_MRG,
      DST_INT
   }

   public static enum ItemType {
      artifact,
      attribute
   }

   public ChangeItemConflictTest(String testMessage, ItemType itemType, ChangeItemCase srcCase, ChangeItemCase dstCase) {
      this.testMessage = testMessage;
      this.srcCase = srcCase;
      this.itemType = itemType;
      this.dstCase = dstCase;
   }

   @Ignore
   @Test
   public void test() {
      // but since we don't seem to handle the mixed case of say artifact on src and attribute on destination
      ChangeItem item;
      if (itemType == ItemType.artifact) {
         item = new ArtifactChangeItem(0, 0, GAMMA, ModificationType.MODIFIED);
      } else {
         item = new AttributeChangeItem(0, 0, 0, GAMMA, ModificationType.MODIFIED, "change");
      }

      buildTestCase(GAMMA, item);
      Assert.assertNotNull(testMessage, null);
   }

   @Parameters
   public static List<Object[]> getData() {
      List<Object[]> data = new LinkedList<Object[]>();

      addArtifactTestCases(data);
      addAttributeTestCases(data);

      return data;
   }

   private static void addArtifactTestCases(List<Object[]> data) {
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_DEL, ChangeItemCase.DST_DEL);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_MOD, ChangeItemCase.DST_DEL);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_MRG, ChangeItemCase.DST_DEL);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_INT, ChangeItemCase.DST_DEL);

      addTest(data, ItemType.artifact, ChangeItemCase.SRC_DEL, ChangeItemCase.DST_MOD);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_MOD, ChangeItemCase.DST_MOD);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_MRG, ChangeItemCase.DST_MOD);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_INT, ChangeItemCase.DST_MOD);

      addTest(data, ItemType.artifact, ChangeItemCase.SRC_DEL, ChangeItemCase.DST_MRG);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_MOD, ChangeItemCase.DST_MRG);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_MRG, ChangeItemCase.DST_MRG);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_INT, ChangeItemCase.DST_MRG);

      addTest(data, ItemType.artifact, ChangeItemCase.SRC_DEL, ChangeItemCase.DST_INT);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_MOD, ChangeItemCase.DST_INT);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_MRG, ChangeItemCase.DST_INT);
      addTest(data, ItemType.artifact, ChangeItemCase.SRC_INT, ChangeItemCase.DST_INT);
   }

   private static void addAttributeTestCases(List<Object[]> data) {
      addTest(data, ItemType.attribute, ChangeItemCase.SRC_DEL, ChangeItemCase.DST_DEL);
      addTest(data, ItemType.attribute, ChangeItemCase.SRC_MOD, ChangeItemCase.DST_DEL);
      addTest(data, ItemType.attribute, ChangeItemCase.SRC_MRG, ChangeItemCase.DST_DEL);

      addTest(data, ItemType.attribute, ChangeItemCase.SRC_DEL, ChangeItemCase.DST_MOD);
      addTest(data, ItemType.attribute, ChangeItemCase.SRC_MOD, ChangeItemCase.DST_MOD);
      addTest(data, ItemType.attribute, ChangeItemCase.SRC_MRG, ChangeItemCase.DST_MOD);

      addTest(data, ItemType.attribute, ChangeItemCase.SRC_DEL, ChangeItemCase.DST_MRG);
      addTest(data, ItemType.attribute, ChangeItemCase.SRC_MOD, ChangeItemCase.DST_MRG);
      addTest(data, ItemType.attribute, ChangeItemCase.SRC_MRG, ChangeItemCase.DST_MRG);
   }

   private static void addTest(Collection<Object[]> data, ItemType itemType, ChangeItemCase srcCase, ChangeItemCase dstCase) {
      String testMessage = srcCase + ", " + dstCase;
      data.add(new Object[] {testMessage, itemType, srcCase, dstCase});
   }

   private void buildTestCase(long gamma, ChangeItem item) {
      changeVersion(item.getBaselineVersion(), gamma, ModificationType.MODIFIED);

      switch (srcCase) {
         case SRC_DEL:
            changeVersion(item.getCurrentVersion(), gamma, ModificationType.DELETED);
            break;
         case SRC_MOD:
            changeVersion(item.getCurrentVersion(), gamma + 1, ModificationType.MODIFIED);
            break;
         case SRC_MRG:
            changeVersion(item.getCurrentVersion(), gamma + 2, ModificationType.MERGED);
            break;
         case SRC_INT:
            changeVersion(item.getCurrentVersion(), gamma, ModificationType.INTRODUCED);
            break;
         default:
            break;
      }

      switch (dstCase) {
         case DST_DEL:
            changeVersion(item.getDestinationVersion(), gamma, ModificationType.DELETED);
            break;
         case DST_MOD:
            changeVersion(item.getDestinationVersion(), gamma + 3, ModificationType.MODIFIED);
            break;
         case DST_MRG:
            changeVersion(item.getCurrentVersion(), gamma + 4, ModificationType.MERGED);
            break;
         case DST_INT:
            changeVersion(item.getCurrentVersion(), gamma, ModificationType.INTRODUCED);
            break;
         default:
            break;
      }
   }

   private void changeVersion(ChangeVersion version, Long gammaId, ModificationType modType) {
      version.setGammaId(gammaId);
      version.setModType(modType);
   }
}