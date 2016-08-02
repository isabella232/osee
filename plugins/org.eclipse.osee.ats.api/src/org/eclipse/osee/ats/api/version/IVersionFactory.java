/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.version;

import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface IVersionFactory {

   IAtsVersion createVersion(String title, String guid, long uuid, IAtsChangeSet changes, IAtsServices services) throws OseeCoreException;

   IAtsVersion createVersion(String name, IAtsChangeSet changes, IAtsServices services) throws OseeCoreException;

}
