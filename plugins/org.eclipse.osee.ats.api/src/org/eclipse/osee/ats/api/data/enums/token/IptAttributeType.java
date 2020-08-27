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

import javax.ws.rs.core.MediaType;
import org.eclipse.osee.ats.api.data.AtsTypeTokenProvider;
import org.eclipse.osee.ats.api.data.enums.token.IptAttributeType.IptEnum;
import org.eclipse.osee.framework.core.data.AttributeTypeEnum;
import org.eclipse.osee.framework.core.data.NamespaceToken;
import org.eclipse.osee.framework.core.data.TaggerTypeToken;
import org.eclipse.osee.framework.core.enums.EnumToken;

/**
 * @author Stephen J. Molaro
 */
public class IptAttributeType extends AttributeTypeEnum<IptEnum> {

   public final IptEnum CommNavAse = new IptEnum(0, "Comm/Nav/Ase");
   public final IptEnum CrewSystems = new IptEnum(1, "Crew Systems");
   public final IptEnum Integration = new IptEnum(2, "Integration");
   public final IptEnum Software = new IptEnum(3, "Software");
   public final IptEnum CEE = new IptEnum(4, "CEE");
   public final IptEnum WeaponsSights = new IptEnum(5, "Weapons/Sights");
   public final IptEnum ProcessorsDisplays = new IptEnum(6, "Processors/Displays");
   public final IptEnum Ah6 = new IptEnum(7, "AH-6");
   public final IptEnum NCO = new IptEnum(8, "NCO");

   public IptAttributeType(NamespaceToken namespace, int enumCount) {
      super(6025996821081174931L, namespace, "ats.IPT", MediaType.TEXT_PLAIN, "", TaggerTypeToken.PlainTextTagger,
         enumCount);
   }

   public IptAttributeType() {
      this(AtsTypeTokenProvider.ATS, 9);
   }

   public class IptEnum extends EnumToken {
      public IptEnum(int ordinal, String name) {
         super(ordinal, name);
         addEnum(this);
      }
   }
}