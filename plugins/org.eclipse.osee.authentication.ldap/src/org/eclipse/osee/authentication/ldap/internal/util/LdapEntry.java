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
package org.eclipse.osee.authentication.ldap.internal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;

/**
 * @author Roberto E. Escobar
 */
public final class LdapEntry {

   public static final String LDAP_ENTRY__DISTINGUISHED_NAME_KEY = "dn";

   private final Map<String, Attribute> attributes;

   public LdapEntry(Map<String, Attribute> attributes) {
      this.attributes = attributes;
   }

   public Set<String> keySet() {
      return attributes.keySet();
   }

   public String get(String key) throws NamingException {
      String toReturn = null;
      Attribute attribute = attributes.get(key);
      if (attribute != null && attribute.size() > 0) {
         toReturn = String.valueOf(attribute.get(0));
      }
      return toReturn;
   }

   public String getDistinguishedName() throws NamingException {
      return get(LDAP_ENTRY__DISTINGUISHED_NAME_KEY);
   }

   public Map<String, String> asMap() throws NamingException {
      Map<String, String> toReturn = new HashMap<String, String>();
      for (String key : keySet()) {
         toReturn.put(key, get(key));
      }
      return toReturn;
   }

   @Override
   public String toString() {
      String toReturn = null;
      try {
         toReturn = getDistinguishedName();
      } catch (Exception ex) {
         toReturn = keySet().toString();
      }
      return toReturn;
   }
}