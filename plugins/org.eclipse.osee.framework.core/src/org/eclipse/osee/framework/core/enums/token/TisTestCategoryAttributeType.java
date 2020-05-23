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
import org.eclipse.osee.framework.core.enums.token.TisTestCategoryAttributeType.TisTestCategoryEnum;

/**
 * @author Stephen J. Molaro
 */
public class TisTestCategoryAttributeType extends AttributeTypeEnum<TisTestCategoryEnum> {

   public final TisTestCategoryEnum SPEC_COMP = new TisTestCategoryEnum(0, "SPEC_COMP");
   public final TisTestCategoryEnum DEV = new TisTestCategoryEnum(1, "DEV");
   public final TisTestCategoryEnum USG = new TisTestCategoryEnum(2, "USG");

   public TisTestCategoryAttributeType(TaggerTypeToken taggerType, String mediaType, NamespaceToken namespace) {
      super(1152921504606847119L, namespace, "TIS Test Category", mediaType, "TIS Test Category", taggerType, 3);
   }

   public class TisTestCategoryEnum extends EnumToken {
      public TisTestCategoryEnum(int ordinal, String name) {
         super(ordinal, name);
         addEnum(this);
      }
   }
}