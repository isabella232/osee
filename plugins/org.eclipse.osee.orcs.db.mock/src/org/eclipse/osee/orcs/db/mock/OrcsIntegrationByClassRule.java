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

import org.eclipse.osee.framework.core.executor.ExecutorAdmin;
import org.eclipse.osee.logger.Log;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.osgi.service.event.EventAdmin;

/**
 * Rule to provide one AtsDatabase per class. Test Class must provide @AfterClass method and call
 * AtsClassDatabase.cleanup().
 *
 * @author Donald G. Dunne
 */
public final class OrcsIntegrationByClassRule extends OsgiRule {

   private OrcsIntegrationByClassRule() {
      // Utility
   }

   public static TestRule integrationRule(Object testObject) {
      return RuleChain.outerRule(new OseeClassDatabase()).around(new OsgiRule(new CheckServices(), testObject));
   }

   public static class CheckServices {
      // @formatter:off
      @OsgiService public Log log;
      @OsgiService public EventAdmin eventAdmin;
      @OsgiService public ExecutorAdmin executorAdmin;
      // @formatter:on
   }
}