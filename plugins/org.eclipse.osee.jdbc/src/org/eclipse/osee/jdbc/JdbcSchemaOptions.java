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
package org.eclipse.osee.jdbc;

import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Roberto E. Escobar
 */
public class JdbcSchemaOptions {

   private final String tableDataSpace;
   private final String indexDataSpace;
   private final boolean useFileSpecifiedSchemas;

   public JdbcSchemaOptions(String tableDataSpace, String indexDataSpace, boolean useFileSpecifiedSchemas) {
      super();
      this.tableDataSpace = tableDataSpace;
      this.indexDataSpace = indexDataSpace;
      this.useFileSpecifiedSchemas = useFileSpecifiedSchemas;
   }

   public String getTableDataSpace() {
      return tableDataSpace;
   }

   public String getIndexDataSpace() {
      return indexDataSpace;
   }

   public boolean isUseFileSpecifiedSchemas() {
      return useFileSpecifiedSchemas;
   }

   public boolean isIndexDataSpaceValid() {
      return Strings.isValid(getIndexDataSpace());
   }

   public boolean isTableDataSpaceValid() {
      return Strings.isValid(getTableDataSpace());
   }

   public static JdbcSchemaOptions defaultOptions() {
      return new JdbcSchemaOptions("", "", true);
   }
}
