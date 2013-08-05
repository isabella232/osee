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
package org.eclipse.osee.orcs.data;

import java.io.InputStream;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 * @author Andrew M. Finkbeiner
 */
public interface AttributeWriteable<T> extends OrcsWriteable, AttributeReadable<T> {

   void setValue(T value) throws OseeCoreException;

   boolean setFromString(String value) throws OseeCoreException;

   boolean setValueFromInputStream(InputStream value) throws OseeCoreException;

   void resetToDefaultValue() throws OseeCoreException;

}