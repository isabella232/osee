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
package org.eclipse.osee.activity.internal;

import java.net.URL;
import org.eclipse.osee.jdbc.AbstractJdbcMigrationResource;

/**
 * @author Ryan D. Brooks
 */
public class ActivityMigrationResource extends AbstractJdbcMigrationResource {

   private static final String SCHEMA_PATH = "migration/";

   @Override
   public URL getLocation() {
      return getClass().getResource(SCHEMA_PATH);
   }

}
