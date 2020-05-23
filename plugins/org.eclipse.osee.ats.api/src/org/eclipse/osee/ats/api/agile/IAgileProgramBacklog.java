/*********************************************************************
 * Copyright (c) 2017 Boeing
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

package org.eclipse.osee.ats.api.agile;

import java.util.List;

/**
 * @author Donald G. Dunne
 */
public interface IAgileProgramBacklog extends IAgileObject {

   @Override
   String getName();

   Long getProgramId();

   List<Long> getBacklogItemIds();

   @Override
   Long getId();

}
