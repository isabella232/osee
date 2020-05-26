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

package org.eclipse.osee.framework.jdk.core.type;

import java.io.InputStream;
import java.util.Date;

/**
 * @author Roberto E. Escobar
 */
public interface IVariantData {

   public String get(String key);

   public boolean isEmpty(String key);

   public String[] getArray(String key);

   public boolean getBoolean(String key);

   public double getDouble(String key) throws NumberFormatException;

   public float getFloat(String key) throws NumberFormatException;

   public int getInt(String key) throws NumberFormatException;

   public long getLong(String key) throws NumberFormatException;

   public Date getDate(String key) throws IllegalArgumentException;

   public Date getDateOrNull(String key) throws IllegalArgumentException;

   public String getStreamAsString(String key) throws Exception;

   public InputStream getStream(String key) throws IllegalArgumentException;

   public void put(String key, String[] value);

   public void put(String key, double value);

   public void put(String key, float value);

   public void put(String key, int value);

   public void put(String key, long value);

   public void put(String key, String value);

   public void put(String key, boolean value);

   public void put(String key, Date date);

   public void put(String key, byte[] bytes);
}
