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
package org.eclipse.osee.framework.database.core;

import java.sql.DatabaseMetaData;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

public abstract class OseeConnection {

   public abstract void close() throws OseeCoreException;

   public abstract boolean isClosed() throws OseeCoreException;

   public abstract boolean isStale();

   public abstract DatabaseMetaData getMetaData() throws OseeCoreException;

   protected abstract void setAutoCommit(boolean autoCommit) throws OseeCoreException;

   protected abstract boolean getAutoCommit() throws OseeCoreException;

   protected abstract void commit() throws OseeCoreException;

   protected abstract void rollback() throws OseeCoreException;

   protected abstract void destroy() throws OseeCoreException;

}