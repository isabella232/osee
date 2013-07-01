/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.exchange.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.osgi.framework.Version;

public class ExchangeTransformProvider implements IExchangeTransformProvider {

   @Override
   public Collection<IOseeExchangeVersionTransformer> getApplicableTransformers(Version versionToCheck) {
      List<IOseeExchangeVersionTransformer> toReturn = new ArrayList<IOseeExchangeVersionTransformer>();

      IOseeExchangeVersionTransformer[] transforms =
         new IOseeExchangeVersionTransformer[] {new V0_9_2Transformer(), new V0_9_4Transformer()};

      for (IOseeExchangeVersionTransformer transformer : transforms) {
         if (isApplicable(transformer.getMaxVersion(), versionToCheck)) {
            toReturn.add(transformer);
         }
      }
      return toReturn;
   }

   private static boolean isApplicable(Version maxVersion, Version versionToCheck) {
      return maxVersion.compareTo(versionToCheck) > 0;
   }
}
