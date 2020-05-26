/*********************************************************************
 * Copyright (c) 2019 Boeing
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

package org.eclipse.osee.framework.core.enums.token;

import org.eclipse.osee.framework.core.data.AttributeTypeEnum;
import org.eclipse.osee.framework.core.data.NamespaceToken;
import org.eclipse.osee.framework.core.data.TaggerTypeToken;
import org.eclipse.osee.framework.core.enums.EnumToken;
import org.eclipse.osee.framework.core.enums.token.FunctionalGroupingAttributeType.FunctionalGroupingEnum;

/**
 * @author Stephen J. Molaro
 */
public class FunctionalGroupingAttributeType extends AttributeTypeEnum<FunctionalGroupingEnum> {

   public final FunctionalGroupingEnum Avionics = new FunctionalGroupingEnum(0, "Avionics");
   public final FunctionalGroupingEnum VmsFlightControl = new FunctionalGroupingEnum(1, "VMS/Flight Control");
   public final FunctionalGroupingEnum EngineFuelHydraulics = new FunctionalGroupingEnum(2, "Engine/Fuel/Hydraulics");
   public final FunctionalGroupingEnum Electrical = new FunctionalGroupingEnum(3, "Electrical");

   public FunctionalGroupingAttributeType(TaggerTypeToken taggerType, String mediaType, NamespaceToken namespace) {
      super(1741310787702764470L, namespace, "Functional Grouping", mediaType, "", taggerType, 4);
   }

   public class FunctionalGroupingEnum extends EnumToken {
      public FunctionalGroupingEnum(int ordinal, String name) {
         super(ordinal, name);
         addEnum(this);
      }
   }
}