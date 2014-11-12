/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.jdbc;

import java.io.InputStream;
import java.net.URI;

/**
 * @author Roberto E. Escobar
 */
public interface JdbcSchemaResource {

   boolean isApplicable(JdbcClientConfig config);

   InputStream getContent();

   URI getLocation();

}
