/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.mock;

import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Donald G. Dunne
 */
public class OseeClassDatabase implements TestRule {

   private final String[] osgiBindings;
   private static TestDatabase db;

   public OseeClassDatabase(String... osgiBindings) {
      this.osgiBindings = osgiBindings;
   }

   @Override
   public Statement apply(final Statement base, final Description description) {
      return new Statement() {

         @Override
         public void evaluate() throws Throwable {
            Assert.assertNotNull("Osgi Binding cannot be null", osgiBindings);
            Assert.assertNotNull("Description cannot be null", description);
            Assert.assertTrue("Osgi Binding cannot be empty", osgiBindings.length > 0);
            if (db == null) {
               db = new TestDatabase(description.getClassName(), description.getMethodName(), osgiBindings);
               db.initialize();
            }
            base.evaluate();
         }
      };
   }

   public static void cleanup() throws Exception {
      if (db != null) {
         db.cleanup();
         db = null;
      }

   }
}
