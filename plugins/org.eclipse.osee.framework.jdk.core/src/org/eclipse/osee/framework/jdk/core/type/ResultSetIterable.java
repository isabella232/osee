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

package org.eclipse.osee.framework.jdk.core.type;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Roberto E. Escobar
 */
public class ResultSetIterable<T> implements ResultSet<T> {

   private Iterable<T> data;

   protected ResultSetIterable(Iterable<T> iterable) {
      super();
      this.data = iterable;
   }

   @Override
   public T getOneOrDefault(T defaultVal) {

      int size = size();
      if (size > 0) {
         defaultVal = iterator().next();
      }
      return defaultVal;
   }

   @Override
   public T getAtMostOneOrDefault(T defaultVal) {

      int size = size();
      if (size > 1) {
         throw createManyExistException(size);
      } else if (size == 1) {
         defaultVal = iterator().next();
      }
      return defaultVal;
   }

   private Iterable<T> getData() {
      return data;
   }

   @Override
   public Iterator<T> iterator() {
      return getData().iterator();
   }

   @Override
   public int size() {
      Iterable<T> it = getData();
      int count = 0;
      if (it instanceof Collection) {
         count = ((Collection<?>) it).size();
      } else {
         count = 0;
         Iterator<?> iterator = it.iterator();
         while (iterator.hasNext()) {
            iterator.next();
            count++;
         }
      }
      return count;
   }

   @Override
   public boolean isEmpty() {
      Iterable<T> it = getData();
      return it == null || !it.iterator().hasNext();
   }

   protected OseeCoreException createManyExistException(int count) {
      return new MultipleItemsExist("Multiple items found - total [%s]", count);
   }

   protected OseeCoreException createDoesNotExistException() {
      return new ItemDoesNotExist("No item found");
   }

   @Override
   public ResultSet<T> sort(Comparator<T> comparator) {
      List<T> list = getList();
      Collections.sort(list, comparator);
      data = list;
      return this;
   }

   @Override
   public List<T> getList() {
      List<T> items = new LinkedList<>();
      for (T obj : data) {
         items.add(obj);
      }
      return items;
   }

   @Override
   public T getOneOrNull() {
      T result = null;
      int size = size();
      if (size > 0) {
         result = iterator().next();
      }
      return result;
   }

   @Override
   public T getExactlyOne() {
      T result = getAtMostOneOrNull();
      if (result == null) {
         throw createDoesNotExistException();
      }
      return result;
   }

   @Override
   public T getAtMostOneOrNull() {
      T result = null;
      int size = size();
      if (size > 1) {
         throw createManyExistException(size);
      } else if (size == 1) {
         result = iterator().next();
      }
      return result;
   }
}