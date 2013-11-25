/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.workflow.transition;

public enum TransitionOption {
   None,
   PrivilegedEditEnabled,
   // Override check whether workflow allows transition to state
   OverrideTransitionValidityCheck,
   // Allows transition to occur with UnAssigned, OseeSystem or Guest
   OverrideAssigneeCheck
};
