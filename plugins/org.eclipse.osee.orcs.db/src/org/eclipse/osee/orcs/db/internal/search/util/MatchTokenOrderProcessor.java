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
package org.eclipse.osee.orcs.db.internal.search.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;

/**
 * @author John Misinco
 */
public class MatchTokenOrderProcessor implements TokenOrderProcessor {

   private final List<MatchLocation> locations;
   private final List<String> tokensToSearch;
   private int matchIdx = 0;
   private int matchStart = -1;
   private int numTokensToMatch = 0;

   public MatchTokenOrderProcessor() {
      super();
      locations = new LinkedList<MatchLocation>();
      tokensToSearch = new ArrayList<String>();
   }

   @Override
   public int getTotalTokensToMatch() {
      return numTokensToMatch;
   }

   @Override
   public void acceptTokenToMatch(String token) {
      tokensToSearch.add(token);
      numTokensToMatch++;
   }

   @Override
   public boolean processToken(String token, MatchLocation match) {
      if (!tokensToSearch.get(matchIdx).equals(token)) {
         matchIdx = 0;
      }

      if (tokensToSearch.get(matchIdx).equals(token)) {
         if (matchIdx == 0) {
            matchStart = match.getStartPosition();
         }
         matchIdx++;
         if (matchIdx == numTokensToMatch) {
            locations.add(createMatchLocation(matchStart, match.getEndPosition()));
            matchIdx = 0;
         }
      }

      return !locations.isEmpty();
   }

   private MatchLocation createMatchLocation(int start, int end) {
      return new MatchLocation(start + 1, end);
   }

   @Override
   public List<MatchLocation> getLocations() {
      return locations;
   }

   @Override
   public void clearAllLocations() {
      locations.clear();
   }

}
