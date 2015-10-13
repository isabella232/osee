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
package org.eclipse.osee.framework.jdk.core.type;

import java.util.Comparator;
import java.util.List;

/**
 * @author Ryan D. Brooks
 * @author Roberto E. Escobar
 */
public interface ResultSet<T> extends Iterable<T> {

   T getOneOrNull() throws OseeCoreException;

   T getExactlyOne() throws OseeCoreException;

   T getAtMostOneOrNull() throws OseeCoreException;

   int size();

   boolean isEmpty();

   ResultSet<T> sort(Comparator<T> comparator);

   List<T> getList();

}
