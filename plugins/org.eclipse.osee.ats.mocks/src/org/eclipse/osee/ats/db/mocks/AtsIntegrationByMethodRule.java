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
package org.eclipse.osee.ats.db.mocks;

import org.eclipse.osee.ats.rest.IAtsServer;
import org.eclipse.osee.event.EventService;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.jdbc.JdbcService;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.db.mock.OsgiRule;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.osgi.service.event.EventAdmin;

/**
 * Provide a new AtsDatabase per method. No extra handling needs to be done by Test Class
 * 
 * @author Donald G. Dunne
 */
public final class AtsIntegrationByMethodRule extends OsgiRule {

   private AtsIntegrationByMethodRule() {
      // Utility
   }

   public static TestRule integrationRule(Object testObject) {
      return RuleChain.outerRule(new AtsMethodDatabase("orcs.jdbc.service")).around(
         new OsgiRule(new CheckServices(), testObject));
   }

   public static class CheckServices {
      // @formatter:off
      @OsgiService public JdbcService jdbcService;
      @OsgiService public Log log;
      @OsgiService public EventAdmin eventAdmin;
      @OsgiService public EventService eventService;
      @OsgiService public ExecutorAdmin executorAdmin;
      @OsgiService public OrcsApi orcsApi;
      @OsgiService public IAtsServer atsServer;
      // @formatter:on
   }

}
