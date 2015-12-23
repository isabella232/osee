/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.internal.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.query.AtsSearchData;
import org.eclipse.osee.ats.api.query.IAtsQuery;
import org.eclipse.osee.ats.api.query.IAtsQueryService;
import org.eclipse.osee.ats.api.query.IAtsSearchDataProvider;
import org.eclipse.osee.ats.api.query.IAtsWorkItemFilter;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.IAttribute;
import org.eclipse.osee.ats.api.workflow.WorkItemType;
import org.eclipse.osee.ats.core.client.IAtsClient;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.query.AtsWorkItemFilter;
import org.eclipse.osee.ats.core.util.AtsJsonFactory;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class AtsQueryServiceIimpl implements IAtsQueryService {

   private final IAtsClient atsClient;
   private static final Pattern namespacePattern = Pattern.compile("\"namespace\":\"(.*?)\"");

   public AtsQueryServiceIimpl(IAtsClient atsClient) {
      this.atsClient = atsClient;
   }

   @Override
   public IAtsQuery createQuery(WorkItemType workItemType, WorkItemType... workItemTypes) {
      AtsQueryImpl query = new AtsQueryImpl(atsClient);
      query.isOfType(workItemType);
      for (WorkItemType type : workItemTypes) {
         query.isOfType(type);
      }
      return query;
   }

   @Override
   public IAtsWorkItemFilter createFilter(Collection<? extends IAtsWorkItem> workItems) {
      return new AtsWorkItemFilter(workItems, atsClient.getServices());
   }

   @Override
   public ArrayList<AtsSearchData> getSavedSearches(IAtsUser atsUser, String namespace) {
      ArrayList<AtsSearchData> searches = new ArrayList<>();
      ArtifactId userArt = atsUser.getStoreObject();
      for (IAttribute<Object> attr : atsClient.getAttributeResolver().getAttributes(userArt,
         AtsAttributeTypes.QuickSearch)) {
         String jsonValue = (String) attr.getValue();
         if (jsonValue.contains("\"namespace\":\"" + namespace + "\"")) {
            try {
               AtsSearchData data = fromJson(jsonValue);
               if (data != null) {
                  searches.add(data);
               }
            } catch (Exception ex) {
               // do nothing
            }
         }
      }
      return searches;
   }

   @Override
   public void saveSearch(IAtsUser atsUser, AtsSearchData data) {
      ArtifactId userArt = atsUser.getStoreObject();
      IAtsChangeSet changes =
         atsClient.getStoreService().createAtsChangeSet("Save ATS Search", atsClient.getUserService().getCurrentUser());

      try {
         IAttribute<Object> attr = getAttrById(userArt, data.getUuid());
         if (attr == null) {
            changes.addAttribute(atsUser, AtsAttributeTypes.QuickSearch, getStoreString(data));
         } else {
            changes.setAttribute(userArt, attr.getId(), getStoreString(data));
         }
         if (!changes.isEmpty()) {
            changes.execute();
         }
      } catch (Exception ex) {
         throw new OseeCoreException("Unable to store ATS Search", ex);
      }

   }

   private IAttribute<Object> getAttrById(ArtifactId artifact, Long attrId) {
      for (IAttribute<Object> attr : atsClient.getAttributeResolver().getAttributes(artifact,
         AtsAttributeTypes.QuickSearch)) {
         String jsonValue = (String) attr.getValue();
         try {
            AtsSearchData data = fromJson(jsonValue);
            if (attrId.equals(data.getUuid())) {
               return attr;
            }
         } catch (Exception ex) {
            // do nothing
         }
      }
      return null;
   }

   @Override
   public void removeSearch(IAtsUser atsUser, AtsSearchData data) {
      ArtifactId userArt = atsUser.getStoreObject();
      IAtsChangeSet changes = atsClient.getStoreService().createAtsChangeSet("Remove ATS Search",
         atsClient.getUserService().getCurrentUser());

      try {
         IAttribute<Object> attr = getAttrById(userArt, data.getUuid());
         if (attr != null) {
            changes.deleteAttribute(userArt, attr);
            changes.execute();
         }
      } catch (Exception ex) {
         throw new OseeCoreException("Unable to remove ATS Search", ex);
      }
   }

   @Override
   public AtsSearchData getSearch(IAtsUser atsUser, Long uuid) {
      try {
         ArtifactId userArt = atsUser.getStoreObject();
         IAttribute<Object> attr = getAttrById(userArt, uuid);
         if (attr != null) {
            AtsSearchData existing = fromJson((String) attr.getValue());
            if (existing != null) {
               return existing;
            }
         }
         return null;
      } catch (Exception ex) {
         throw new OseeCoreException("Unable to get ATS Search", ex);
      }
   }

   private String getStoreString(AtsSearchData data) throws Exception {
      return AtsJsonFactory.getMapper().writeValueAsString(data);
   }

   private AtsSearchData fromJson(String jsonValue) {
      Matcher m = namespacePattern.matcher(jsonValue);
      if (m.find()) {
         return fromJson(m.group(1), jsonValue);
      }
      return null;
   }

   private AtsSearchData fromJson(String namespace, String jsonValue) {
      AtsSearchData data = null;
      try {
         for (IAtsSearchDataProvider provider : atsClient.getSearchDataProviders()) {
            data = provider.fromJson(namespace, jsonValue);
            if (data != null) {
               break;
            }
         }
      } catch (Exception ex) {
         OseeLog.logf(Activator.class, Level.WARNING, ex,
            "Can't deserialize ATS Quick Search value [%s] for namespace [%s]", jsonValue, namespace);
      }
      return data;
   }

   @Override
   public AtsSearchData getSearch(String jsonStr) {
      return fromJson(jsonStr);
   }

   @Override
   public AtsSearchData createSearchData(String namespace, String searchName) {
      AtsSearchData data = null;
      try {
         for (IAtsSearchDataProvider provider : atsClient.getSearchDataProviders()) {
            data = provider.createSearchData(namespace, searchName);
            if (data != null) {
               break;
            }
         }
      } catch (Exception ex) {
         OseeLog.logf(Activator.class, Level.WARNING, ex,
            "Can't create ATS Quick Search namespace [%s] and searchName [%s]", namespace, searchName);
      }
      return data;
   }

}
