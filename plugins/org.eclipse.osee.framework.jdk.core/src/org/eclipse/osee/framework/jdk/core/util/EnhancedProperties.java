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
package org.eclipse.osee.framework.jdk.core.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Ken J. Aguilar
 */
public class EnhancedProperties implements Serializable {
   private static final long serialVersionUID = 4105281128024379352L;

   private final HashMap<String, Serializable> map;

   public EnhancedProperties() {
      map = new HashMap<>();
   }

   public EnhancedProperties(int initialCapacity) {
      map = new HashMap<>(initialCapacity);
   }

   public EnhancedProperties(EnhancedProperties props) {
      this();
      addAll(props);
   }

   public void setProperty(String key, Serializable value) {
      map.put(key, value);
   }

   public Serializable getProperty(String key) {
      return map.get(key);
   }

   public Serializable getProperty(String key, Serializable defaultValue) {
      Serializable value = map.get(key);
      return value == null ? defaultValue : value;
   }

   public Set<Map.Entry<String, Serializable>> entrySet() {
      return map.entrySet();
   }

   public void addAll(EnhancedProperties otherProps) {
      this.map.putAll(otherProps.map);
   }

   public void addAll(Map<String, Serializable> otherMap) {
      this.map.putAll(otherMap);
   }

   public void clear() {
      map.clear();
   }

   public Collection<String> differences(EnhancedProperties otherProps) {
      LinkedList<String> differences = new LinkedList<>();
      for (Entry<String, Serializable> entry : map.entrySet()) {
         Serializable value = otherProps.getProperty(entry.getKey());
         if (value == null) {
            if (entry.getValue() != null) {
               differences.add(entry.getKey());
            }
         } else {
            if (!value.equals(entry.getValue())) {
               differences.add(entry.getKey());
            }
         }
      }
      map.clear();
      map.putAll(otherProps.map);
      return differences;
   }

   public Map<String, Serializable> asMap() {
      return map;
   }
}
