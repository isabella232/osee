/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.internal.state;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.user.IAtsUserService;
import org.eclipse.osee.ats.api.workflow.state.IAtsStateManager;
import org.eclipse.osee.ats.core.model.impl.WorkStateImpl;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test unit for {@link AtsWorkStateFactory}
 * 
 * @author Donald G. Dunne
 */
public class AtsWorkStateFactoryTest {

   // @formatter:off
   @Mock IAtsStateManager stateMgr;
   @Mock IAtsUser Joe, Kay;
   @Mock IAtsUserService userService;
   // @formatter:on

   @Before
   public void setup() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);
   }

   @Test
   public void testToXml() {
      AtsWorkStateFactory atsWorkStateFactory = new AtsWorkStateFactory(userService);
      String xml = atsWorkStateFactory.toStoreStr(stateMgr, "Implement");
      Assert.assertEquals("Implement;;;", xml);

      when(Joe.getUserId()).thenReturn("asdf");
      when(Kay.getUserId()).thenReturn("qwer");
      List<IAtsUser> asList = Arrays.asList(Joe, Kay);
      when(stateMgr.getAssignees("Implement")).thenReturn(asList);

      xml = atsWorkStateFactory.toStoreStr(stateMgr, "Implement");
      Assert.assertEquals("Implement;<asdf><qwer>;;", xml);

      when(stateMgr.getHoursSpent("Implement")).thenReturn(1.3);
      when(stateMgr.getHoursSpentStr("Implement")).thenReturn("1.3");
      xml = atsWorkStateFactory.toStoreStr(stateMgr, "Implement");
      Assert.assertEquals("Implement;<asdf><qwer>;1.3;", xml);

      when(stateMgr.getPercentComplete("Implement")).thenReturn(23);
      xml = atsWorkStateFactory.toStoreStr(stateMgr, "Implement");
      Assert.assertEquals("Implement;<asdf><qwer>;1.3;23", xml);
      when(stateMgr.getPercentComplete("Implement")).thenReturn(0);
      xml = atsWorkStateFactory.toStoreStr(stateMgr, "Implement");
      Assert.assertEquals("Implement;<asdf><qwer>;1.3;", xml);
   }

   @Test
   public void testGetFromXml() {
      AtsWorkStateFactory atsWorkStateFactory = new AtsWorkStateFactory(userService);

      WorkStateImpl state = atsWorkStateFactory.fromStoreStr("");
      Assert.assertEquals("Unknown", state.getName());
      Assert.assertEquals(0, state.getHoursSpent(), 0.01);
      Assert.assertEquals(0, state.getAssignees().size());
      Assert.assertEquals(0, state.getPercentComplete());

      when(userService.getUserById(eq("asdf"))).thenReturn(Joe);
      when(userService.getUserById(eq("qwer"))).thenReturn(Kay);
      state = atsWorkStateFactory.fromStoreStr("Implement;<asdf><qwer>;1.3;20");
      Assert.assertEquals("Implement", state.getName());
      Assert.assertEquals(1.3, state.getHoursSpent(), 0.01);

      Assert.assertEquals(2, state.getAssignees().size());
      Assert.assertEquals(20, state.getPercentComplete());

      state = atsWorkStateFactory.fromStoreStr("Implement;;;");
      Assert.assertEquals("Implement", state.getName());
      Assert.assertEquals(0, state.getHoursSpent(), 0.01);
      Assert.assertEquals(0, state.getAssignees().size());
      Assert.assertEquals(0, state.getPercentComplete());
   }

   @Test(expected = OseeArgumentException.class)
   public void testGetFromXml_Exception() {
      AtsWorkStateFactory atsWorkStateFactory = new AtsWorkStateFactory(userService);
      atsWorkStateFactory.fromStoreStr("asdfa;");
   }

}
