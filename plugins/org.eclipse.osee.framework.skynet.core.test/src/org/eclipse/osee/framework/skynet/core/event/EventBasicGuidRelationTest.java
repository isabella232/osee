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
package org.eclipse.osee.framework.skynet.core.event;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.core.model.event.DefaultBasicGuidArtifact;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidRelation;
import org.eclipse.osee.framework.skynet.core.relation.RelationEventType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class EventBasicGuidRelationTest {

   @Test
   public void testEqualsEventBasicGuidRelation() {
      DefaultBasicGuidArtifact eventArt1 = new DefaultBasicGuidArtifact(COMMON, 0x01L, GUID.create());
      DefaultBasicGuidArtifact eventArt2 =
         new DefaultBasicGuidArtifact(eventArt1.getBranch(), eventArt1.getArtTypeGuid(), eventArt1.getGuid());

      EventBasicGuidRelation eventRel1 = new EventBasicGuidRelation(RelationEventType.Added, eventArt1.getBranchId(),
         0x02L, 234, 333, 34, eventArt1, 33, eventArt2);
      EventBasicGuidRelation eventRel2 = new EventBasicGuidRelation(RelationEventType.Added, eventRel1.getBranchId(),
         eventRel1.getRelTypeGuid(), eventRel1.getRelationId(), eventRel1.getGammaId(), eventRel1.getArtAId(),
         eventRel1.getArtA(), eventRel1.getArtBId(), eventRel1.getArtB());

      Assert.assertEquals(eventRel1.hashCode(), eventRel2.hashCode());
      Assert.assertEquals(eventRel1, eventRel2);

      eventRel2 = new EventBasicGuidRelation(RelationEventType.Deleted, eventRel1.getBranchId(),
         eventRel1.getRelTypeGuid(), eventRel1.getRelationId(), eventRel1.getGammaId(), eventRel1.getArtAId(),
         eventRel1.getArtA(), eventRel1.getArtBId(), eventRel1.getArtB());

      Assert.assertNotSame(eventRel1, eventRel2);

      eventRel2 = new EventBasicGuidRelation(RelationEventType.Deleted, Lib.generateUuid(), eventRel1.getRelTypeGuid(),
         eventRel1.getRelationId(), eventRel1.getGammaId(), eventRel1.getArtAId(), eventRel1.getArtA(),
         eventRel1.getArtBId(), eventRel1.getArtB());

      Assert.assertNotSame(eventRel1, eventRel2);

      eventRel2 = new EventBasicGuidRelation(RelationEventType.Added, eventRel1.getBranchId(),
         eventRel1.getRelTypeGuid(), 99, eventRel1.getGammaId(), eventRel1.getArtAId(), eventRel1.getArtA(),
         eventRel1.getArtBId(), eventRel1.getArtB());

      Assert.assertNotSame(eventRel1, eventRel2);

      eventRel2 = new EventBasicGuidRelation(RelationEventType.Added, eventRel1.getBranchId(),
         eventRel1.getRelTypeGuid(), eventRel1.getRelationId(), 88, eventRel1.getArtAId(), eventRel1.getArtA(),
         eventRel1.getArtBId(), eventRel1.getArtB());

      Assert.assertNotSame(eventRel1, eventRel2);

      eventRel2 = new EventBasicGuidRelation(RelationEventType.Added, eventRel1.getBranchId(),
         eventRel1.getRelTypeGuid(), eventRel1.getRelationId(), eventRel1.getGammaId(), 77, eventRel1.getArtA(),
         eventRel1.getArtBId(), eventRel1.getArtB());

      Assert.assertNotSame(eventRel1, eventRel2);

      eventRel2 = new EventBasicGuidRelation(RelationEventType.Added, eventRel1.getBranchId(),
         eventRel1.getRelTypeGuid(), eventRel1.getRelationId(), eventRel1.getGammaId(), eventRel1.getArtAId(),
         eventRel1.getArtA(), 66, eventRel1.getArtB());

      Assert.assertNotSame(eventRel1, eventRel2);

      Set<EventBasicGuidRelation> toAdd = new HashSet<>();
      toAdd.add(eventRel2);
      toAdd.add(eventRel1);
      Assert.assertEquals(2, toAdd.size());

      toAdd.add(eventRel1);
      Assert.assertEquals(2, toAdd.size());

      Set<EventBasicGuidRelation> eventArts = new HashSet<>();
      eventArts.add(eventRel2);
      eventArts.addAll(toAdd);
      Assert.assertEquals(2, toAdd.size());

   }

   @Test
   public void testEventBasicGuidRelationIs() {
      DefaultBasicGuidArtifact eventArt1 = new DefaultBasicGuidArtifact(COMMON, 0x03L, GUID.create());
      DefaultBasicGuidArtifact eventArt2 =
         new DefaultBasicGuidArtifact(eventArt1.getBranch(), eventArt1.getArtTypeGuid(), eventArt1.getGuid());

      EventBasicGuidRelation eventRel1 = new EventBasicGuidRelation(RelationEventType.Added, eventArt1.getBranchId(),
         0x04L, 234, 333, 34, eventArt1, 33, eventArt2);
      Assert.assertTrue(eventRel1.is(RelationEventType.Added));
      Assert.assertTrue(eventRel1.is(RelationEventType.Added, RelationEventType.Purged));
      Assert.assertFalse(eventRel1.is(RelationEventType.Purged));
      Assert.assertFalse(eventRel1.is(RelationEventType.Deleted, RelationEventType.Purged));
   }

}
