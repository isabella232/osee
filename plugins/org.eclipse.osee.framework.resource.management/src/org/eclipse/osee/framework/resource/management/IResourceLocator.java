/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.resource.management;

import java.net.URI;

/**
 * @author Roberto E. Escobar
 */
public interface IResourceLocator {

   /**
    * Location describing a resource
    * 
    * @return uri to resource
    */
   public URI getLocation();

   /**
    * Get this locators protocol
    */
   public String getProtocol();

   /**
    * Get the raw path.
    * 
    * @return raw path
    */
   public String getRawPath();
}
