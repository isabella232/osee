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
package org.eclipse.osee.framework.branch.management.exchange.export;

import java.sql.Connection;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractDbExportItem extends AbstractExportItem {

   private Connection connection;
   private int joinQueryId;

   public AbstractDbExportItem(int priority, String name, String source) {
      super(priority, name, source);
      this.joinQueryId = -1;
      this.connection = null;
   }

   public void setJoinQueryId(int joinQueryId) {
      this.joinQueryId = joinQueryId;
   }

   public void setConnection(Connection connection) {
      this.connection = connection;
   }

   protected Connection getConnection() {
      return this.connection;
   }

   protected int getJoinQueryId() {
      return this.joinQueryId;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.branch.management.export.AbstractExportItem#cleanUp()
    */
   @Override
   public void cleanUp() {
      this.joinQueryId = -1;
      this.connection = null;
      super.cleanUp();
   }
}
