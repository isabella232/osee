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

package org.eclipse.osee.ats.api.data.enums.token;

import org.eclipse.osee.ats.api.data.enums.token.IptTeamAttributeType.IptTeamEnum;
import org.eclipse.osee.framework.core.data.AttributeTypeEnum;
import org.eclipse.osee.framework.core.data.NamespaceToken;
import org.eclipse.osee.framework.core.data.TaggerTypeToken;
import org.eclipse.osee.framework.core.enums.EnumToken;

/**
 * @author Stephen J. Molaro
 */
public class IptTeamAttributeType extends AttributeTypeEnum<IptTeamEnum> {

   public final IptTeamEnum AH6 = new IptTeamEnum(0, "AH6");
   public final IptTeamEnum AH6i = new IptTeamEnum(1, "AH6i");
   public final IptTeamEnum ASE = new IptTeamEnum(2, "ASE");
   public final IptTeamEnum CEE = new IptTeamEnum(3, "CEE");
   public final IptTeamEnum CommNavASE = new IptTeamEnum(4, "CommNavASE");
   public final IptTeamEnum CrewStation = new IptTeamEnum(5, "CrewStation");
   public final IptTeamEnum EQA = new IptTeamEnum(6, "EQA");
   public final IptTeamEnum Integration = new IptTeamEnum(7, "Integration");
   public final IptTeamEnum ProcDisp = new IptTeamEnum(8, "ProcDisp");
   public final IptTeamEnum Software = new IptTeamEnum(9, "Software");
   public final IptTeamEnum WpnSight = new IptTeamEnum(10, "WpnSight");

   public IptTeamAttributeType(TaggerTypeToken taggerType, String mediaType, NamespaceToken namespace) {
      super(1364016887343371647L, namespace, "ats.IPT Team", mediaType, "", taggerType, 11);
   }

   public class IptTeamEnum extends EnumToken {
      public IptTeamEnum(int ordinal, String name) {
         super(ordinal, name);
         addEnum(this);
      }
   }
}