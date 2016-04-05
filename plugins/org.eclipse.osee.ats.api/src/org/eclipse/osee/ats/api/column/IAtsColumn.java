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
package org.eclipse.osee.ats.api.column;

import org.eclipse.osee.ats.api.IAtsObject;

/**
 * @author Donald G. Dunne
 */
public interface IAtsColumn {

   public static final String CELL_ERROR_PREFIX = "!Error";

   public String getColumnText(IAtsObject atsObject);

}
