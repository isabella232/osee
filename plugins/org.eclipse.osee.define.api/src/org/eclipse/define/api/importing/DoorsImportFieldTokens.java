/*******************************************************************************
 * Copyright (c) 2019 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.define.api.importing;

import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;

/**
 * @author David W. Miller
 */
public final class DoorsImportFieldTokens {
   private static final String BLOCK_ATTR_REGEX = "^[^:]+:\\s*(.*)";
   private static final String IDEN_REGEX = "^Object I[dD].*";
   private static final String LEVE_REGEX = "^Object Leve.*";
   private static final String TYPE_REGEX = "^Object Type.*";
   private static final String HEAD_REGEX = "^Object Head.*";
   private static final String NUMB_REGEX = "^Object Numb.*";
   private static final String REQ_REGEX = "^Requirement.*";
   private static final String TEXT_REGEX = "^Object Text.*";
   private static final String OBJECT_TEXT_REGEX = "(.*?)<w:r>(.*?)</w:r>(.*)";

   // @formatter:off
   public static final BlockFieldToken blockAttrIdentifier =  BlockFieldToken.valueOf(1, "Identifier", IDEN_REGEX, BLOCK_ATTR_REGEX, BlockField::new, CoreAttributeTypes.DoorsID);
   public static final BlockFieldToken blockAttrLevel =       BlockFieldToken.valueOf(2, "Level", LEVE_REGEX, BLOCK_ATTR_REGEX, BlockField::new);
   public static final BlockFieldToken blockAttrHeading =     BlockFieldToken.valueOf(3, "Heading", HEAD_REGEX, BLOCK_ATTR_REGEX, BlockField::new);
   public static final BlockFieldToken blockAttrNumber =      BlockFieldToken.valueOf(4, "Number", NUMB_REGEX, BLOCK_ATTR_REGEX, BlockField::new, CoreAttributeTypes.DoorsHierarchy);
   public static final BlockFieldToken blockAttrRequirement = BlockFieldToken.valueOf(5, "Requirement", REQ_REGEX, BLOCK_ATTR_REGEX, BlockField::new);
   public static final BlockFieldToken blockAttrType =        BlockFieldToken.valueOf(6, "Type", TYPE_REGEX, BLOCK_ATTR_REGEX, BlockField::new);
   public static final BlockFieldToken blockAttrText =        BlockFieldToken.valueOf(7, "Text", TEXT_REGEX, OBJECT_TEXT_REGEX, BlockFieldText::new, CoreAttributeTypes.WordTemplateContent);
   // @formatter:on
}
