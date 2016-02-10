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
package org.eclipse.osee.ats.api.util;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.framework.core.data.ArtifactId;

public interface IAtsStoreService {

   IAtsChangeSet createAtsChangeSet(String comment, IAtsUser user);

   IAtsChangeSet createAtsChangeSet(String comment);

   List<IAtsWorkItem> reload(Collection<IAtsWorkItem> workItems);

   boolean isDeleted(IAtsObject atsObject);

   Long getUuidFromGuid(String guid);

   String getTypeName(ArtifactId artifact);
   
   String getGuid(IAtsObject atsObject);

}
