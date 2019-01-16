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
package org.eclipse.osee.orcs.db.internal.search.handlers;

import org.eclipse.osee.framework.core.enums.TableEnum;

/**
 * @author Roberto E. Escobar
 */
public final class TxBranchIdsSqlHandler extends MainTableFieldSqlHandler {
   public TxBranchIdsSqlHandler() {
      super(TableEnum.TX_DETAILS_TABLE, "branch_id", SqlHandlerPriority.TX_BRANCH_ID);
   }
}