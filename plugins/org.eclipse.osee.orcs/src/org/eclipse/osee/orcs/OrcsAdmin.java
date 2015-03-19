/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Roberto E. Escobar
 */
public interface OrcsAdmin {

   Callable<OrcsMetaData> createFetchOrcsMetaData();

   Callable<OrcsMetaData> createDatastore(Map<String, String> parameters);

   boolean isDataStoreInitialized();
}
