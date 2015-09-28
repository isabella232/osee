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
package org.eclipse.osee.orcs.core.internal.search;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.LoadDataHandlerDecorator;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;
import org.eclipse.osee.orcs.core.internal.graph.GraphBuilder;
import org.eclipse.osee.orcs.core.internal.proxy.ExternalArtifactManager;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.AttributeReadable;
import org.eclipse.osee.orcs.search.Match;
import com.google.common.base.Supplier;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactMatchDataHandler extends LoadDataHandlerDecorator {

   private final OrcsSession session;
   private final ExternalArtifactManager proxyManager;

   private Map<Integer, ArtifactMatch> matches;
   private List<Match<ArtifactReadable, AttributeReadable<?>>> results;

   public ArtifactMatchDataHandler(OrcsSession session, GraphBuilder handler, ExternalArtifactManager proxyManager) {
      super(handler);
      this.session = session;
      this.proxyManager = proxyManager;
   }

   @Override
   protected GraphBuilder getHandler() {
      return (GraphBuilder) super.getHandler();
   }

   @Override
   public void onLoadStart() throws OseeCoreException {
      super.onLoadStart();
      matches = new HashMap<>();
      results = null;
   }

   @Override
   public void onData(AttributeData data, MatchLocation match) throws OseeCoreException {
      super.onData(data, match);
      Integer artId = data.getArtifactId();
      synchronized (matches) {
         ArtifactMatch artifactMatch = matches.get(artId);
         if (artifactMatch == null) {
            artifactMatch = new ArtifactMatch();
            matches.put(artId, artifactMatch);
         }
         artifactMatch.addLocation(data.getLocalId(), match);
      }
   }

   @Override
   public void onLoadEnd() throws OseeCoreException {
      super.onLoadEnd();
      buildResults();
   }

   private void buildResults() throws OseeCoreException {
      Iterable<Artifact> loaded = getHandler().getArtifacts();

      for (Artifact item : loaded) {
         ArtifactMatch artifactMatch = matches.get(new Long(item.getUuid()).intValue());
         if (artifactMatch != null) {
            ArtifactReadable readable = proxyManager.asExternalArtifact(session, item);
            artifactMatch.setArtifactReadable(readable);
         }
         if (results == null) {
            results = Lists.newLinkedList();
         }
         results.add(artifactMatch);
      }
      matches = null;
   }

   public List<Match<ArtifactReadable, AttributeReadable<?>>> getResults() {
      return results != null ? results : Collections.<Match<ArtifactReadable, AttributeReadable<?>>> emptyList();
   }

   private static <K, V> ListMultimap<K, V> newLinkedHashListMultimap() {
      Map<K, Collection<V>> map = new LinkedHashMap<>();
      return Multimaps.newListMultimap(map, new Supplier<List<V>>() {
         @Override
         public List<V> get() {
            return Lists.newArrayList();
         }
      });
   }

   private static final class ArtifactMatch implements Match<ArtifactReadable, AttributeReadable<?>> {

      private final ListMultimap<Integer, MatchLocation> matches = newLinkedHashListMultimap();
      private ArtifactReadable item;

      public ArtifactMatch() {
         super();
      }

      public void setArtifactReadable(ArtifactReadable item) {
         this.item = item;
      }

      public void addLocation(Integer attrId, MatchLocation location) {
         matches.put(attrId, location);
      }

      @Override
      public boolean hasLocationData() {
         return !matches.isEmpty();
      }

      @Override
      public ArtifactReadable getItem() {
         return item;
      }

      @Override
      public Collection<AttributeReadable<?>> getElements() throws OseeCoreException {
         Collection<AttributeReadable<?>> filtered = Lists.newLinkedList();
         // look at all attributes since search already filters on deletion flag
         for (AttributeReadable<?> attribute : item.getAttributes(DeletionFlag.INCLUDE_DELETED)) {
            if (matches.containsKey(attribute.getLocalId())) {
               filtered.add(attribute);
            }
         }
         return filtered;
      }

      @Override
      public List<MatchLocation> getLocation(AttributeReadable<?> element) {
         List<MatchLocation> toReturn = matches.get(element.getLocalId());
         return toReturn != null ? toReturn : Collections.<MatchLocation> emptyList();
      }

      @Override
      public String toString() {
         return "ArtifactMatch [item=" + item + ", matches=" + matches + "]";
      }
   }

}