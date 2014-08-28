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
package org.eclipse.osee.jaxrs.server.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.jaxrs.server.internal.JaxRsConfiguration.JaxRsConfigurationBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test Case for {@link JaxRsConfiguration}
 * 
 * @author Roberto E. Escobar
 */
public class JaxRsConfigurationTest {

   private static final String BASE_CONTEXT_PATH = "adsflkajdfajlsdaj";
   private static final String ACTUAL_BASE_CONTEXT_PATH = "/" + BASE_CONTEXT_PATH;

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   private JaxRsConfigurationBuilder builder;

   @Before
   public void testSetup() {
      initMocks(this);

      builder = JaxRsConfiguration.newBuilder();
   }

   @Test
   public void testBaseContext() {
      builder.baseContext(BASE_CONTEXT_PATH);

      JaxRsConfiguration actual = builder.build();

      assertEquals(ACTUAL_BASE_CONTEXT_PATH, actual.getBaseContext());
   }

   @Test
   public void testDefaultProperties() {
      Map<String, Object> properties = new HashMap<String, Object>();
      builder.properties(properties);

      JaxRsConfiguration actual = builder.build();

      assertEquals("/", actual.getBaseContext());
   }

   @Test
   public void testConfigProperties() {
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put(JaxRsConstants.JAXRS_BASE_CONTEXT, BASE_CONTEXT_PATH);

      builder.properties(properties);

      JaxRsConfiguration actual = builder.build();

      assertEquals(ACTUAL_BASE_CONTEXT_PATH, actual.getBaseContext());
   }

   @Test
   public void testNoChangeAfterBuild() {
      builder.baseContext(BASE_CONTEXT_PATH);

      JaxRsConfiguration actual = builder.build();

      assertEquals(ACTUAL_BASE_CONTEXT_PATH, actual.getBaseContext());

      builder.build();

      assertEquals(ACTUAL_BASE_CONTEXT_PATH, actual.getBaseContext());
   }

}
