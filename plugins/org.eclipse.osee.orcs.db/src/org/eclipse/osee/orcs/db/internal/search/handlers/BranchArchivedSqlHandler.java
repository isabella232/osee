/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.orcs.db.internal.search.handlers;

import org.eclipse.osee.framework.core.enums.SqlTable;

/**
 * @author Roberto E. Escobar
 */
public final class BranchArchivedSqlHandler extends MainTableFieldSqlHandler {
   public BranchArchivedSqlHandler() {
      super(SqlTable.BRANCH_TABLE, "archived", SqlHandlerPriority.BRANCH_ARCHIVED);
   }
}