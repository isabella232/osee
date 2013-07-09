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
package org.eclipse.osee.framework.core.message.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.TransactionRecordFactory;
import org.eclipse.osee.framework.core.model.type.AttributeTypeFactory;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryService;
import org.eclipse.osee.framework.core.services.TempCachingService;
import org.eclipse.osee.framework.core.translation.IDataTranslationService;
import org.eclipse.osee.framework.core.translation.ITranslator;
import org.eclipse.osee.framework.core.translation.ITranslatorId;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;

/**
 * @author Roberto E. Escobar
 */
public class DataTranslationService implements IDataTranslationService {

   private final DataTranslationServiceFactory factoryConfigurator = new DataTranslationServiceFactory();
   private final Map<ITranslatorId, ITranslator<?>> translators =
      new ConcurrentHashMap<ITranslatorId, ITranslator<?>>();

   private IOseeModelFactoryService modelFactory;
   private TempCachingService cachingService;

   public void setModelFactory(IOseeModelFactoryService modelFactory) {
      this.modelFactory = modelFactory;
   }

   public void setTempCachingService(TempCachingService cachingService) {
      this.cachingService = cachingService;
   }

   public void start() throws OseeCoreException {
      TransactionRecordFactory txFactory = modelFactory.getTransactionFactory();
      AttributeTypeFactory attributeTypeFactory = modelFactory.getAttributeTypeFactory();

      translators.clear();
      factoryConfigurator.configureService(this, txFactory, attributeTypeFactory, cachingService);
   }

   public void stop() {
      translators.clear();
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> T convert(PropertyStore propertyStore, ITranslatorId txId) throws OseeCoreException {
      Conditions.checkNotNull(txId, "translator Id");

      T object = null;
      if (propertyStore != null && !propertyStore.isEmpty()) {
         ITranslator<?> translator = getTranslator(txId);
         object = (T) translator.convert(propertyStore);
      }
      return object;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> PropertyStore convert(T object, ITranslatorId txId) throws OseeCoreException {
      PropertyStore propertyStore = null;
      if (object == null) {
         propertyStore = new PropertyStore();
      } else {
         ITranslator<T> translator = (ITranslator<T>) getTranslator(txId);
         propertyStore = translator.convert(object);
      }
      return propertyStore;
   }

   @Override
   public ITranslator<?> getTranslator(ITranslatorId txId) throws OseeCoreException {
      Conditions.checkNotNull(txId, "translator Id");
      ITranslator<?> toReturn = translators.get(txId);
      if (toReturn == null) {
         throw new OseeStateException("Unable to find a match for translator id [%s]", txId);
      }
      return toReturn;

   }

   @Override
   public boolean addTranslator(ITranslator<?> translator, ITranslatorId txId) throws OseeCoreException {
      Conditions.checkNotNull(txId, "translator Id");
      Conditions.checkNotNull(translator, "translator");
      boolean wasAdded = false;
      if (!translators.containsKey(txId)) {
         translators.put(txId, translator);
         wasAdded = true;
      }
      return wasAdded;
   }

   @Override
   public boolean removeTranslator(ITranslatorId txId) throws OseeCoreException {
      Conditions.checkNotNull(txId, "translator Id");
      return translators.remove(txId) != null;
   }

   @Override
   public Collection<ITranslatorId> getSupportedClasses() {
      return new HashSet<ITranslatorId>(translators.keySet());
   }

   @Override
   public <T> T convert(InputStream inputStream, ITranslatorId txId) throws OseeCoreException {
      Conditions.checkNotNull(inputStream, "inputStream");
      Conditions.checkNotNull(txId, "translator Id");

      T toReturn = null;
      PropertyStore propertyStore = new PropertyStore();
      try {
         propertyStore.load(inputStream);
         toReturn = convert(propertyStore, txId);
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return toReturn;
   }

   @Override
   public <T> InputStream convertToStream(T object, ITranslatorId txId) throws OseeCoreException {
      PropertyStore propertyStore = convert(object, txId);
      InputStream inputStream = null;
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      try {
         propertyStore.save(buffer);
         inputStream = new ByteArrayInputStream(buffer.toByteArray());
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return inputStream;
   }
}
