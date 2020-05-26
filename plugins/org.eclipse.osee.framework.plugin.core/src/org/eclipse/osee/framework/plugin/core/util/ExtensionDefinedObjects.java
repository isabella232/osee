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

package org.eclipse.osee.framework.plugin.core.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.OseeActivator;
import org.osgi.framework.Bundle;

/**
 * @author Roberto E. Escobar
 */
public class ExtensionDefinedObjects<T> {

   private List<T> loadedObjects;
   private Map<String, T> objectsByID;

   private final String extensionPointId;
   private final String elementName;
   private final String classNameAttribute;
   private final boolean allowsEmptyOnLoad;

   public ExtensionDefinedObjects(String extensionPointId, String elementName, String classNameAttribute) {
      this(extensionPointId, elementName, classNameAttribute, false);
   }

   public ExtensionDefinedObjects(String extensionPointId, String elementName, String classNameAttribute, boolean allowsEmptyOnLoad) {
      this.extensionPointId = extensionPointId;
      this.elementName = elementName;
      this.classNameAttribute = classNameAttribute;
      this.allowsEmptyOnLoad = allowsEmptyOnLoad;
   }

   public List<T> getObjects() {
      checkInitialized();
      return loadedObjects;
   }

   public T getObjectById(String id) {
      checkInitialized();
      return objectsByID.get(id);
   }

   public Collection<String> getObjectIds() {
      checkInitialized();
      return objectsByID.keySet();
   }

   private synchronized void checkInitialized() {
      if (!isInitialized()) {
         initialize(extensionPointId, elementName, classNameAttribute);
      }
   }

   private boolean isInitialized() {
      return loadedObjects != null && objectsByID != null;
   }

   public synchronized void clear() {
      if (loadedObjects != null) {
         loadedObjects.clear();
         loadedObjects = null;
      }
      if (objectsByID != null) {
         objectsByID.clear();
         objectsByID = null;
      }
   }

   @SuppressWarnings("unchecked")
   private void initialize(String extensionPointId, String elementName, String classNameAttribute) {
      loadedObjects = new ArrayList<>();
      objectsByID = new HashMap<>();
      List<IConfigurationElement> elements = ExtensionPoints.getExtensionElements(extensionPointId, elementName);
      for (IConfigurationElement element : elements) {
         IExtension extension = (IExtension) element.getParent();
         String identifier = extension.getUniqueIdentifier();
         String className = element.getAttribute(classNameAttribute);
         String bundleName = element.getContributor().getName();

         if (Strings.isValid(bundleName, className)) {
            try {
               Bundle bundle = Platform.getBundle(bundleName);
               Class<?> taskClass = bundle.loadClass(className);
               T object = null;
               try {
                  Method getInstance = taskClass.getMethod("getInstance", new Class[] {});
                  object = (T) getInstance.invoke(null, new Object[] {});
               } catch (NoSuchMethodException ex) {
                  object = (T) taskClass.newInstance();
               }
               if (object != null) {
                  loadedObjects.add(object);
                  objectsByID.put(identifier, object);
               }
            } catch (Exception ex) {
               OseeLog.logf(OseeActivator.class, Level.SEVERE, ex, "Unable to Load: [%s - %s]", bundleName, className);
            } catch (LinkageError er) {
               OseeLog.logf(OseeActivator.class, Level.SEVERE, er, "Unable to Load: [%s - %s]", bundleName, className);
            }
         }
      }
      if (!allowsEmptyOnLoad && loadedObjects.isEmpty()) {
         OseeLog.logf(OseeActivator.class, Level.WARNING,
            "No Objects loaded for [%s] with element name [%s] and attribute [%s]", extensionPointId, elementName,
            classNameAttribute);
      }
   }

   @Override
   public String toString() {
      return getObjects().toString();
   }
}
