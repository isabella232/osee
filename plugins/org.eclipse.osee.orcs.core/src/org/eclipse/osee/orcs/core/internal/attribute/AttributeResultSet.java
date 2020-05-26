/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.orcs.core.internal.attribute;

import java.util.List;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSetList;
import org.eclipse.osee.orcs.core.ds.Attribute;

/**
 * @author Roberto E. Escobar
 */
public class AttributeResultSet<T> extends ResultSetList<Attribute<T>> {

   private final AttributeTypeId wasSearched;
   private final AttributeExceptionFactory exceptionFactory;

   public AttributeResultSet(AttributeExceptionFactory exceptionFactory, List<Attribute<T>> data) {
      this(exceptionFactory, null, data);
   }

   public AttributeResultSet(AttributeExceptionFactory exceptionFactory, AttributeTypeId wasSearched, List<Attribute<T>> data) {
      super(data);
      this.exceptionFactory = exceptionFactory;
      this.wasSearched = wasSearched;
   }

   @Override
   protected OseeCoreException createManyExistException(int count) {
      OseeCoreException toReturn = null;
      if (exceptionFactory != null) {
         toReturn = exceptionFactory.createManyExistException(wasSearched, count);
      } else {
         toReturn = super.createManyExistException(count);
      }
      return toReturn;
   }

   @Override
   protected OseeCoreException createDoesNotExistException() {
      OseeCoreException toReturn = null;
      if (exceptionFactory != null) {
         toReturn = exceptionFactory.createDoesNotExistException(wasSearched);
      } else {
         toReturn = super.createDoesNotExistException();
      }
      return toReturn;
   }

}