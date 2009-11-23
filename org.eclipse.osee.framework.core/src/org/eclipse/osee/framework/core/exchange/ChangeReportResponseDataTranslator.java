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
package org.eclipse.osee.framework.core.exchange;

import java.util.ArrayList;

import org.eclipse.osee.framework.core.IDataTranslationService;
import org.eclipse.osee.framework.core.data.ChangeItem;
import org.eclipse.osee.framework.core.data.ChangeReportResponseData;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;


/**
 * @author Jeff C. Phillips
 */
public class ChangeReportResponseDataTranslator implements IDataTranslator<ChangeReportResponseData> {
   public enum Entry {
      MAX_COUNT;
   }

   private final IDataTranslationService service;
   
   public ChangeReportResponseDataTranslator(IDataTranslationService service) {
      super();
      this.service = service;
   }

   @Override
   public ChangeReportResponseData convert(PropertyStore propertyStore) throws OseeCoreException {
      ArrayList<ChangeItem> changeItems = new ArrayList<ChangeItem>();
      int maxCount = propertyStore.getInt(Entry.MAX_COUNT.name());
      
      for(int i=0; i<maxCount ; i++){
         ChangeItem changeItem = service.convert(propertyStore.getPropertyStore(String.valueOf(i)), ChangeItem.class);
         changeItems.add(changeItem);
      }
      return new ChangeReportResponseData(changeItems);
   }

   @Override
   public PropertyStore convert(ChangeReportResponseData changeReportResponseData) throws OseeCoreException {
      PropertyStore store = new PropertyStore();
      store.put(Entry.MAX_COUNT.name(), changeReportResponseData.getChangeItems().size());
      
      int index = 0;
      for(ChangeItem changeItem : changeReportResponseData.getChangeItems()){
         store.put(String.valueOf(index++), service.convert(changeItem));
      }
      return store;
   }
}
