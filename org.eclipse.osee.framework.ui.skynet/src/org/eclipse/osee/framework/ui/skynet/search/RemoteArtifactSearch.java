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
package org.eclipse.osee.framework.ui.skynet.search;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.framework.jdk.core.type.ObjectPair;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoad;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.linking.HttpProcessor;
import org.eclipse.osee.framework.skynet.core.linking.HttpUrlBuilder;
import org.eclipse.osee.framework.skynet.core.linking.HttpProcessor.AcquireResult;

/**
 * @author Roberto E. Escobar
 */
public class RemoteArtifactSearch extends AbstractArtifactSearchQuery {
   private Map<String, String> parameters;

   public RemoteArtifactSearch(String query, Map<String, Boolean> options) {
      this.parameters = new HashMap<String, String>();
      this.parameters.put("query", query);
      if (options != null) {
         for (String optionName : options.keySet()) {
            this.parameters.put(optionName, options.get(optionName).toString());
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.search.AbstractArtifactSearchQuery#getArtifacts()
    */
   @Override
   public Collection<Artifact> getArtifacts() throws Exception {
      ObjectPair<Integer, Integer> queryIdAndSize = executeSearch();
      if (queryIdAndSize != null && queryIdAndSize.object2 > 0) {
         return ArtifactLoader.loadArtifactsFromQuery(queryIdAndSize.object1, ArtifactLoad.FULL, null,
               queryIdAndSize.object2, false);
      }
      return java.util.Collections.emptyList();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.search.AbstractArtifactSearchQuery#getCriteriaLabel()
    */
   @Override
   public String getCriteriaLabel() {
      return parameters.get("query");
   }

   private ObjectPair<Integer, Integer> executeSearch() throws Exception {
      ObjectPair<Integer, Integer> toReturn = null;
      String url = HttpUrlBuilder.getInstance().getOsgiServletServiceUrl("search", parameters);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      AcquireResult httpRequestResult = HttpProcessor.acquire(new URL(url), outputStream);
      if (httpRequestResult.wasSuccessful()) {
         String queryIdString = outputStream.toString("UTF-8");
         if (Strings.isValid(queryIdString)) {
            String[] entries = queryIdString.split(",\\s*");
            if (entries.length >= 2) {
               toReturn = new ObjectPair<Integer, Integer>(new Integer(entries[0]), new Integer(entries[1]));
            }
         }
      }
      return toReturn;
   }

}
