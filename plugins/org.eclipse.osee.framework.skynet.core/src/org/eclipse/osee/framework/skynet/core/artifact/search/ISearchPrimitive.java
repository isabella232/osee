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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

public interface ISearchPrimitive {

   /**
    * The sql operators that will provide a set of art_id's for the given search.
    * 
    * @return Return SQL string
    * @throws OseeCoreException TODO
    */
   public String getCriteriaSql(List<Object> dataList, IOseeBranch branch) throws OseeCoreException;

   /**
    * The name of the column to use as the art_id column.
    * 
    * @return Return artifact column name string
    */
   public String getArtIdColName();

   /**
    * Returns a list of the tables, comma separated, that are necessary for the sql statement returned from getSql().
    * 
    * @return tables string
    * @see ISearchPrimitive#getCriteriaSql(List, IOseeBranch)
    */
   public String getTableSql(List<Object> dataList, IOseeBranch branch);

   /**
    * Returns a string which can be used to later re-acquire the primitive in full
    * 
    * @return Return storage string
    */
   public String getStorageString();
}
