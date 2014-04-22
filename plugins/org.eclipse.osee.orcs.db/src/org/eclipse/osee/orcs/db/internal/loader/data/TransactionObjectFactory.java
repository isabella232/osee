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
package org.eclipse.osee.orcs.db.internal.loader.data;

import java.util.Date;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.TxOrcsData;

/**
 * @author Roberto E. Escobar
 */
public interface TransactionObjectFactory extends OrcsDataFactory {

   TxOrcsData createTxData(int localId, TransactionDetailsType type, Date date, String comment, long branchUuid, int authorId, int commitId) throws OseeCoreException;

   TxOrcsData createCopy(TxOrcsData source) throws OseeCoreException;
}
