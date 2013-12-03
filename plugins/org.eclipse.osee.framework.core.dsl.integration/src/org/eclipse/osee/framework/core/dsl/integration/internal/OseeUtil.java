/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.dsl.integration.internal;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AccessPermissionEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ObjectRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationSideEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationType;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.util.HexUtil;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Roberto E. Escobar
 */
public final class OseeUtil {

   private OseeUtil() {
      // Utility Class
   }

   private static long checkAndGetUuid(OseeType type) throws OseeCoreException {
      String uuid = type.getUuid();
      Conditions.checkNotNull(uuid, "uuid", "for type [%s]", type.getName());
      return HexUtil.toLong(uuid);
   }

   public static IArtifactType toToken(XArtifactType model) throws OseeCoreException {
      return TokenFactory.createArtifactType(checkAndGetUuid(model), Strings.unquote(model.getName()));
   }

   public static IAttributeType toToken(XAttributeType model) throws OseeCoreException {
      return TokenFactory.createAttributeType(checkAndGetUuid(model), Strings.unquote(model.getName()));
   }

   public static IRelationType toToken(XRelationType model) throws OseeCoreException {
      return TokenFactory.createRelationType(checkAndGetUuid(model), Strings.unquote(model.getName()));
   }

   public static boolean isRestrictedSide(XRelationSideEnum relationSideEnum, RelationSide relationSide) throws OseeCoreException {
      Conditions.checkNotNull(relationSideEnum, "relation side restriction");
      Conditions.checkNotNull(relationSide, "relation side");

      boolean toReturn = false;
      switch (relationSideEnum) {
         case BOTH:
            toReturn = true;
            break;
         case SIDE_A:
            toReturn = relationSide.isSideA();
            break;
         case SIDE_B:
            toReturn = !relationSide.isSideA();
            break;
         default:
            break;
      }
      return toReturn;
   }

   public static PermissionEnum getPermission(ObjectRestriction restriction) throws OseeCoreException {
      Conditions.checkNotNull(restriction, "restriction");
      AccessPermissionEnum modelPermission = restriction.getPermission();
      Conditions.checkNotNull(modelPermission, "restriction permission");
      PermissionEnum toReturn;
      if (modelPermission == AccessPermissionEnum.ALLOW) {
         toReturn = PermissionEnum.WRITE;
      } else {
         toReturn = PermissionEnum.READ;
      }
      return toReturn;
   }

   public static String getRelationOrderType(String guid) throws OseeCoreException {
      IRelationSorterId type = RelationOrderBaseTypes.getFromGuid(guid);
      return type.getName().replaceAll(" ", "_");
   }

   public static String orderTypeNameToGuid(String orderTypeName) throws OseeCoreException {
      Conditions.checkNotNull(orderTypeName, "orderTypeName");
      return RelationOrderBaseTypes.getFromOrderTypeName(orderTypeName.replaceAll("_", " ")).getGuid();
   }
}
