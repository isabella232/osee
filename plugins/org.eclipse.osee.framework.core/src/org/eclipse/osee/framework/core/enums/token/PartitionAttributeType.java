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
import org.eclipse.osee.framework.core.enums.token.PartitionAttributeType.PartitionEnum;

/**
 * @author Stephen J. Molaro
 */
public class PartitionAttributeType extends AttributeTypeEnum<PartitionEnum> {

   public final PartitionEnum AircraftSystems = new PartitionEnum(0, "Aircraft Systems");
   public final PartitionEnum GraphicsHandler = new PartitionEnum(1, "Graphics Handler");
   public final PartitionEnum Communication = new PartitionEnum(2, "Communication");
   public final PartitionEnum FlightControl = new PartitionEnum(3, "Flight Control");
   public final PartitionEnum InputOutputProcessor = new PartitionEnum(4, "Input/Output Processor");
   public final PartitionEnum Navigation = new PartitionEnum(5, "Navigation");
   public final PartitionEnum Unspecified = new PartitionEnum(6, "Unspecified");

   public PartitionAttributeType(TaggerTypeToken taggerType, String mediaType, NamespaceToken namespace) {
      super(1152921504606847111L, namespace, "Partition", mediaType, "", taggerType, 7);
   }

   public class PartitionEnum extends EnumToken {
      public PartitionEnum(int ordinal, String name) {
         super(ordinal, name);
         addEnum(this);
      }
   }
}