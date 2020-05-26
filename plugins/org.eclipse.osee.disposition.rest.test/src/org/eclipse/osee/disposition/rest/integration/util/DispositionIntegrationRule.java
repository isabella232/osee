/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.disposition.rest.integration.util;

import org.eclipse.osee.framework.core.executor.ExecutorAdmin;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.db.mock.OseeDatabase;
import org.eclipse.osee.orcs.db.mock.OsgiRule;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.osgi.service.event.EventAdmin;

/**
 * @author Roberto E. Escobar
 */
public final class DispositionIntegrationRule extends OsgiRule {

   private DispositionIntegrationRule() {
      // Utility
   }

   public static TestRule integrationRule(Object testObject) {
      return RuleChain.outerRule(new OseeDatabase()).around(new OsgiRule(new CheckServices(), testObject));
   }

   public static class CheckServices {
      // @formatter:off
      @OsgiService public Log log;
      @OsgiService public EventAdmin eventAdmin;
      @OsgiService public ExecutorAdmin executorAdmin;
      @OsgiService public OrcsApi orcsApi;
      // @formatter:on
   }
}