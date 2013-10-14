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
package org.eclipse.osee.ats.api.workflow;

import org.eclipse.osee.framework.core.exception.OseeCoreException;

public interface IAttribute<T> {

   public T getValue() throws OseeCoreException;

   public Object getData();

   public void delete() throws OseeCoreException;

   public void setValue(T value) throws OseeCoreException;
}
