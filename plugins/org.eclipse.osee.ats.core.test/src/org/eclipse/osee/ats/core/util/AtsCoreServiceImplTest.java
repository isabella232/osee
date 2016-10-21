/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.util;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.workdef.IAttributeResolver;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link AtsCoreServiceImpl}
 *
 * @author Donald G. Dunne
 */
public class AtsCoreServiceImplTest {

   // @formatter:off
   @Mock private IAtsObject atsObject;
   @Mock private IAttributeResolver attrResolver;
   // @formatter:on

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
   }

   @Test
   public void testGetAtsId() {
      ArtifactToken artifact = ArtifactToken.valueOf(0, "guid", null, COMMON, null);
      when(attrResolver.getSoleAttributeValue(artifact, AtsAttributeTypes.AtsId, null)).thenReturn(null);
      String result = AtsCoreServiceImpl.getAtsId(attrResolver, artifact);
      assertEquals(result, "guid");

      when(attrResolver.getSoleAttributeValue(atsObject, AtsAttributeTypes.AtsId, null)).thenReturn(null);
      when(atsObject.getStoreObject()).thenReturn(artifact);
      result = AtsCoreServiceImpl.getAtsId(attrResolver, atsObject);
      assertEquals(result, "guid");

      when(attrResolver.getSoleAttributeValue(artifact, AtsAttributeTypes.AtsId, null)).thenReturn("ATS23");
      result = AtsCoreServiceImpl.getAtsId(attrResolver, artifact);
      assertEquals(result, "ATS23");

      when(attrResolver.getSoleAttributeValue(artifact, AtsAttributeTypes.AtsId, null)).thenReturn("ATS23");
      assertEquals(result, "ATS23");
   }
}