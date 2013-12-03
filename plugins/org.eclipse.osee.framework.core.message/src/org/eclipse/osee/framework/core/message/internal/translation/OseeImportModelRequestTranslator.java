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
package org.eclipse.osee.framework.core.message.internal.translation;

import org.eclipse.osee.framework.core.model.OseeImportModelRequest;
import org.eclipse.osee.framework.core.translation.ITranslator;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;

/**
 * @author Roberto E. Escobar
 */
public class OseeImportModelRequestTranslator implements ITranslator<OseeImportModelRequest> {

   private static enum Fields {
      GENERATE_EMF_COMPARE,
      GENERATE_DIRTY_REPORT,
      MODEL_NAME,
      MODEL;
   }

   @Override
   public OseeImportModelRequest convert(PropertyStore store) {
      String model = store.get(Fields.MODEL.name());
      String modelName = store.get(Fields.MODEL_NAME.name());
      boolean createTypeChangeReport = store.getBoolean(Fields.GENERATE_DIRTY_REPORT.name());
      boolean createCompareReport = store.getBoolean(Fields.GENERATE_EMF_COMPARE.name());

      return new OseeImportModelRequest(modelName, model, createTypeChangeReport, createCompareReport);
   }

   @Override
   public PropertyStore convert(OseeImportModelRequest object) {
      PropertyStore store = new PropertyStore();
      store.put(Fields.MODEL_NAME.name(), object.getModelName());
      store.put(Fields.MODEL.name(), object.getModel());
      store.put(Fields.GENERATE_DIRTY_REPORT.name(), object.isCreateTypeChangeReport());
      store.put(Fields.GENERATE_EMF_COMPARE.name(), object.isCreateCompareReport());
      return store;
   }
}
