/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.util;

import java.util.Collection;
import java.util.Date;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface IValueProvider {

   public String getName();

   public boolean isEmpty() throws OseeCoreException;

   public Collection<String> getValues() throws OseeCoreException;

   public Collection<Date> getDateValues() throws OseeCoreException;

}
