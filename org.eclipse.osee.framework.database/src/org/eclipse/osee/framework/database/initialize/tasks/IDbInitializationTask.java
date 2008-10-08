/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.database.initialize.tasks;

import java.sql.Connection;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;

public interface IDbInitializationTask {
   public abstract void run(Connection connection) throws OseeCoreException;

   public abstract boolean canRun();
}
