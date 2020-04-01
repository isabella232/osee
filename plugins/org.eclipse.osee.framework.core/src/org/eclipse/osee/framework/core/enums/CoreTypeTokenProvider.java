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
package org.eclipse.osee.framework.core.enums;

import org.eclipse.osee.framework.core.data.FaceOseeTypes;
import org.eclipse.osee.framework.core.data.NamespaceToken;
import org.eclipse.osee.framework.core.data.OrcsTypeTokenProviderBase;
import org.eclipse.osee.framework.core.data.OrcsTypeTokens;

/**
 * @author Ryan D. Brooks
 */
public final class CoreTypeTokenProvider extends OrcsTypeTokenProviderBase {
   public static final OrcsTypeTokens osee = new OrcsTypeTokens(NamespaceToken.OSEE);
   private static final NamespaceToken FACE =
      NamespaceToken.valueOf(108, "face", "Namespace for Future Airborne Capability Environment Consortium");
   public static final OrcsTypeTokens face = new OrcsTypeTokens(FACE);

   public CoreTypeTokenProvider() {
      super(osee, face);
      loadClasses(CoreArtifactTypes.Artifact, CoreAttributeTypes.Name, CoreRelationTypes.Allocation,
         FaceOseeTypes.UnitOfConformance);
   }
}