/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.ats.core.column;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.ats.core.config.WorkPackageUtility;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test case for {@link ProgramColumn}
 *
 * @author Donald G. Dunne
 */
public class ProgramColumnTest extends ProgramColumn {

   // @formatter:off
   @Mock private WorkPackageUtility util;
   @Mock private AtsApi atsApi;
   @Mock private IAtsWorkItem workItem;
   @Mock private IAtsObject object;
   @Mock private IAtsProgram program;
   // @formatter:on

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
   }

   @Test
   public void test() {
      String str = ProgramColumn.getProgramStr(object, atsApi, util);
      assertEquals(str, "");

      when(util.getProgram(atsApi, workItem)).thenReturn(new Pair<IAtsProgram, Boolean>(null, false));

      str = ProgramColumn.getProgramStr(workItem, atsApi, util);
      assertEquals(str, "");

      when(util.getProgram(atsApi, workItem)).thenReturn(new Pair<>(program, false));
      when(program.getName()).thenReturn("Country");
      str = ProgramColumn.getProgramStr(workItem, atsApi, util);
      assertEquals(str, "Country");

      when(util.getProgram(atsApi, workItem)).thenReturn(new Pair<>(program, true));
      str = ProgramColumn.getProgramStr(workItem, atsApi, util);
      assertEquals(str, "Country (I)");

   }
}
