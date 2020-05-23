/*********************************************************************
 * Copyright (c) 2012 Boeing
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

package org.eclipse.osee.vcast.model;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.osee.vcast.VCastDataStore;

/**
 * @author Roberto E. Escobar
 */
public class VCastWritableTable implements VCastTableData<VCastWritable> {

   @Override
   public String getName() {
      return "writable";
   }

   @Override
   public String[] getColumns() {
      return new String[] {"is_writable"};
   }

   @Override
   public Collection<VCastWritable> getRows(VCastDataStore dataStore) {
      return Collections.singleton(dataStore.getWritable());
   }

   @Override
   public Object[] toRow(VCastWritable data) {
      return new Object[] {data.getIsWritable()};
   }
}
