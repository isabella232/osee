/*********************************************************************
 * Copyright (c) 2011 Boeing
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

package org.eclipse.osee.ats.core.validator;

import java.util.ArrayList;
import org.eclipse.osee.ats.api.workdef.WidgetResult;
import org.eclipse.osee.ats.api.workdef.WidgetStatus;
import org.eclipse.osee.ats.core.util.StringValueProvider;
import org.junit.Assert;

/**
 * @author Donald G. Dunne
 */
public class ValidatorTestUtil {

   public static StringValueProvider emptyValueProvider = new StringValueProvider(new ArrayList<String>());

   public static void assertValidResult(WidgetResult result) {
      Assert.assertEquals(WidgetStatus.Success, result.getStatus());
      Assert.assertEquals(WidgetStatus.Success.getName(), result.getDetails());
   }

}
