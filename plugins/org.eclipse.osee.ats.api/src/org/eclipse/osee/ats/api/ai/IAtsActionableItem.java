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
package org.eclipse.osee.ats.api.ai;

import java.util.Collection;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.rule.IAtsRules;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.jdk.core.type.NamedIdBase;

/**
 * @author Donald G. Dunne
 */
public interface IAtsActionableItem extends IAtsConfigObject, IAtsRules {

   /*****************************
    * Name, Full Name, Description
    ******************************/
   void setName(String name);

   IAtsActionableItem SENTINEL = createSentinel();

   void setDescription(String description);

   /*****************************
    * Parent and Children Team Definitions
    ******************************/
   Collection<IAtsActionableItem> getChildrenActionableItems();

   IAtsActionableItem getParentActionableItem();

   IAtsTeamDefinition getTeamDefinition();

   IAtsTeamDefinition getTeamDefinitionInherited();

   /*****************************
    * Misc
    ******************************/
   Collection<String> getStaticIds();

   public Boolean isActionable();

   boolean isAllowUserActionCreation();

   public static IAtsActionableItem createSentinel() {
      final class IAtsActionableItemSentinel extends NamedIdBase implements IAtsActionableItem {

         @Override
         public boolean isActive() {
            return false;
         }

         @Override
         public ArtifactTypeToken getArtifactType() {
            return null;
         }

         @Override
         public Collection<String> getRules() {
            return null;
         }

         @Override
         public boolean hasRule(String rule) {
            return false;
         }

         @Override
         public void setDescription(String description) {
            // Do nothing
         }

         @Override
         public Collection<IAtsActionableItem> getChildrenActionableItems() {
            return null;
         }

         @Override
         public IAtsActionableItem getParentActionableItem() {
            return null;
         }

         @Override
         public IAtsTeamDefinition getTeamDefinition() {
            return null;
         }

         @Override
         public IAtsTeamDefinition getTeamDefinitionInherited() {
            return null;
         }

         @Override
         public Collection<String> getStaticIds() {
            return null;
         }

         @Override
         public Boolean isActionable() {
            return false;
         }

         @Override
         public boolean isAllowUserActionCreation() {
            return false;
         }

         @Override
         public AtsApi getAtsApi() {
            return null;
         }

      }
      return new IAtsActionableItemSentinel();
   }

}
