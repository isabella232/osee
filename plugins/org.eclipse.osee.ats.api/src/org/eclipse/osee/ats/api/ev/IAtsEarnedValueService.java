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
package org.eclipse.osee.ats.api.ev;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface IAtsEarnedValueService {

   public IAtsWorkPackage getWorkPackage(IAtsWorkItem workItem) throws OseeCoreException;

   public Collection<IAtsWorkPackage> getWorkPackageOptions(IAtsObject object) throws OseeCoreException;

   public String getWorkPackageId(IAtsWorkItem atsObject);

   public void setWorkPackage(IAtsWorkPackage workPackage, Collection<IAtsWorkItem> workItems);

   public void removeWorkPackage(IAtsWorkPackage workPackage, Collection<IAtsWorkItem> workItems);

}
