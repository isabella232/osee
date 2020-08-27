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
import org.eclipse.osee.ats.api.data.enums.token.ClosureStateAttributeType.ClosureStateEnum;
import org.eclipse.osee.framework.core.data.AttributeTypeEnum;
import org.eclipse.osee.framework.core.data.NamespaceToken;
import org.eclipse.osee.framework.core.data.TaggerTypeToken;
import org.eclipse.osee.framework.core.enums.EnumToken;

/**
 * @author Stephen J. Molaro
 */
public class ClosureStateAttributeType extends AttributeTypeEnum<ClosureStateEnum> {

   public final ClosureStateEnum Open = new ClosureStateEnum(0, "Open");
   public final ClosureStateEnum PrepareToClose = new ClosureStateEnum(1, "Prepare to Close");
   public final ClosureStateEnum CloseOut = new ClosureStateEnum(2, "Close Out");
   public final ClosureStateEnum Closed = new ClosureStateEnum(3, "Closed");

   public ClosureStateAttributeType(NamespaceToken namespace, int enumCount) {
      super(1152921504606847452L, namespace, "ats.closure.Closure State", MediaType.TEXT_PLAIN, "",
         TaggerTypeToken.PlainTextTagger, enumCount);
   }

   public ClosureStateAttributeType() {
      this(AtsTypeTokenProvider.ATS, 4);
   }

   public class ClosureStateEnum extends EnumToken {
      public ClosureStateEnum(int ordinal, String name) {
         super(ordinal, name);
         addEnum(this);
      }
   }
}