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

package org.eclipse.osee.disposition.rest.enums.token;

import javax.ws.rs.core.MediaType;
import org.eclipse.osee.disposition.rest.DispoTypeTokenProvider;
import org.eclipse.osee.disposition.rest.enums.token.DispoItemStatusAttributeType.DispoItemStatusEnum;
import org.eclipse.osee.framework.core.data.AttributeTypeEnum;
import org.eclipse.osee.framework.core.data.NamespaceToken;
import org.eclipse.osee.framework.core.data.TaggerTypeToken;
import org.eclipse.osee.framework.core.enums.EnumToken;

/**
 * @author Stephen J. Molaro
 */
public class DispoItemStatusAttributeType extends AttributeTypeEnum<DispoItemStatusEnum> {

   public final DispoItemStatusEnum Pass = new DispoItemStatusEnum(0, "PASS");
   public final DispoItemStatusEnum Incomplete = new DispoItemStatusEnum(1, "INCOMPLETE");
   public final DispoItemStatusEnum Complete = new DispoItemStatusEnum(2, "COMPLETE");
   public final DispoItemStatusEnum CompleteAnalyzed = new DispoItemStatusEnum(3, "COMPLETE-ANALYZED");
   public final DispoItemStatusEnum Unspecified = new DispoItemStatusEnum(4, "Unspecified");

   public DispoItemStatusAttributeType(NamespaceToken namespace, int enumCount) {
      super(3458764513820541336L, namespace, "dispo.item.Status", MediaType.TEXT_PLAIN, "",
         TaggerTypeToken.PlainTextTagger, enumCount);
   }

   public DispoItemStatusAttributeType() {
      this(DispoTypeTokenProvider.DISPO, 5);
   }

   public class DispoItemStatusEnum extends EnumToken {
      public DispoItemStatusEnum(int ordinal, String name) {
         super(ordinal, name);
         addEnum(this);
      }
   }
}