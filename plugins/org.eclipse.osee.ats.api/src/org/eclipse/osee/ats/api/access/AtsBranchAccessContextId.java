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

package org.eclipse.osee.ats.api.access;

import org.eclipse.osee.framework.core.data.IAccessContextId;

/**
 * @author Donald G. Dunne
 */
public final class AtsBranchAccessContextId {

   public static final IAccessContextId DENY_CONTEXT =
      IAccessContextId.valueOf(4870045005030602805L, "ats.branchobject.deny");

   private AtsBranchAccessContextId() {
      // Branch Object Contexts;
   }
}
