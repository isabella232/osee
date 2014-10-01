/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.config;

import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.junit.Assert;
import org.junit.Test;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Unit Test for {@link ProgramResource}
 * 
 * @author Donald G. Dunne
 */
public class ProgramResourceTest extends AbstractConfigurationRestTest {

   @Test
   public void testAtsProgramsRestCall() throws Exception {
      JsonArray array = getAndCheck("/ats/program");
      Assert.assertEquals(2, array.size());
      JsonObject obj = getObjectNamed("SAW Program", array);
      Assert.assertNotNull("Did not find value SAW Program", obj);
      Assert.assertFalse(obj.has("ats.Description"));
   }

   @Test
   public void testAtsProgramsDetailsRestCall() throws Exception {
      JsonArray array = getAndCheck("/ats/program/details");
      Assert.assertEquals(2, array.size());
      JsonObject obj = getObjectNamed("SAW Program", array);
      Assert.assertNotNull("Did not find value SAW Program", obj);
      Assert.assertTrue(obj.has("ats.Description"));
   }

   @Test
   public void testAtsProgramRestCall() throws Exception {
      JsonArray array = getAndCheck("/ats/program/" + getSawProgram().getArtId());
      Assert.assertEquals(1, array.size());
      JsonObject obj = getObjectNamed("SAW Program", array);
      Assert.assertNotNull("Did not find value SAW Program", obj);
      Assert.assertFalse(obj.has("ats.Description"));
   }

   @Test
   public void testAtsProgramDetailsRestCall() throws Exception {
      JsonArray array = getAndCheck("/ats/program/" + getSawProgram().getArtId() + "/details");
      Assert.assertEquals(1, array.size());
      JsonObject obj = getObjectNamed("SAW Program", array);
      Assert.assertNotNull("Did not find value SAW Program", obj);
      Assert.assertTrue(obj.has("ats.Description"));
   }

   private Artifact getSawProgram() {
      return ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Program, "SAW Program",
         AtsUtilCore.getAtsBranch());
   }

}
