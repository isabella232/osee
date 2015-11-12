/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.osee.ats.core.client.artifact.CollectorArtifact;
import org.eclipse.osee.ats.core.client.util.IArtifactMembersCache;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.jdk.core.type.DoubleKeyHashMap;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.ArtifactEventFilter;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;

/**
 * @author John Misinco
 */
public class ArtifactCollectorsCache<T extends CollectorArtifact> implements IArtifactMembersCache<T> {

   private static Map<Long, List<Artifact>> cache;
   private static DoubleKeyHashMap<Long, Long, String> collectorMemberOrderMap;
   private static Set<Long> registered;
   private static volatile boolean initialized = false;
   private final IRelationTypeSide memberRelationType;

   public ArtifactCollectorsCache(IRelationTypeSide memberRelationType) {
      this.memberRelationType = memberRelationType;
   }

   private void initializeStructures() {
      if (!initialized) {
         initialized = true;
         cache = new HashMap<>();
         registered = new HashSet<>();
         collectorMemberOrderMap = new DoubleKeyHashMap<>();
      }
   }

   private void registerForEvents(final T collectorArt) {
      if (!registered.contains(collectorArt.getUuid())) {
         IArtifactEventListener eventListener = new IArtifactEventListener() {

            @Override
            public List<? extends IEventFilter> getEventFilters() {
               return Arrays.asList(new ArtifactEventFilter(collectorArt));
            }

            @Override
            public void handleArtifactEvent(ArtifactEvent artifactEvent, Sender sender) {
               synchronized (cache) {
                  cache.remove(collectorArt.getUuid());
               }
               synchronized (collectorMemberOrderMap) {
                  Map<Long, String> subHash = collectorMemberOrderMap.getSubHash(collectorArt.getUuid());
                  if (subHash != null) {
                     List<Long> keys = new ArrayList<>(subHash.keySet());
                     for (Long key1 : keys) {
                        collectorMemberOrderMap.remove(collectorArt.getUuid(), key1);
                     }
                  }
               }
               synchronized (registered) {
                  registered.remove(collectorArt.getUuid());
               }
            }
         };
         OseeEventManager.addListener(eventListener);
         synchronized (registered) {
            registered.add(collectorArt.getUuid());
         }
      }
   }

   @Override
   public List<Artifact> getMembers(T collector) throws OseeCoreException {
      initializeStructures();
      registerForEvents(collector);
      List<Artifact> members = cache.get(collector.getUuid());
      if (members == null) {
         members = collector.getRelatedArtifacts(memberRelationType, DeletionFlag.EXCLUDE_DELETED);
         synchronized (cache) {
            cache.put(collector.getUuid(), members);
            fillOrderCache(collector, members);
         }
      }
      LinkedList<Artifact> linkedList = new LinkedList<>(members);
      return linkedList;
   }

   @Override
   public void decache(T collectorArt) {
      if (initialized) {
         synchronized (cache) {
            cache.remove(collectorArt.getUuid());
         }
         synchronized (collectorMemberOrderMap) {
            clearOrderCache(collectorArt);
         }
      }
   }

   @Override
   public void invalidate() {
      if (initialized) {
         synchronized (cache) {
            cache.clear();
         }
         synchronized (collectorMemberOrderMap) {
            collectorMemberOrderMap.clear();
         }
      }
   }

   @Override
   public String getMemberOrder(T collectorArt, Artifact member) throws OseeCoreException {
      initializeStructures();
      if (collectorMemberOrderMap.getSubHash(collectorArt.getUuid()) == null) {
         fillOrderCache(collectorArt, getMembers(collectorArt));
      }
      String order = collectorMemberOrderMap.get(collectorArt.getUuid(), member.getUuid());
      return order == null ? "" : order;
   }

   private void fillOrderCache(T collectorArt, List<Artifact> members) {
      initializeStructures();
      synchronized (collectorMemberOrderMap) {
         clearOrderCache(collectorArt);
         int x = 1;
         for (Artifact artifact : members) {
            collectorMemberOrderMap.put(collectorArt.getUuid(), artifact.getUuid(), String.valueOf(x++));
         }
      }
   }

   private void clearOrderCache(T collectorArt) {
      if (initialized) {
         List<Long> memberIds = new ArrayList<>();
         Map<Long, String> subHash = collectorMemberOrderMap.getSubHash(collectorArt.getUuid());
         if (subHash != null) {
            memberIds.addAll(subHash.keySet());
            for (Long memberId : memberIds) {
               collectorMemberOrderMap.remove(collectorArt.getUuid(), memberId);
            }
         }
      }
   }
}
