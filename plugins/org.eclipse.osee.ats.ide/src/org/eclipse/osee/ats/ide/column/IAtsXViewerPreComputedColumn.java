/*********************************************************************
 * Copyright (c) 2016 Boeing
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

package org.eclipse.osee.ats.ide.column;

import org.eclipse.nebula.widgets.xviewer.IXViewerPreComputedColumn;
import org.eclipse.osee.ats.api.IAtsObject;

/**
 * @author Donald G. Dunne
 */
public interface IAtsXViewerPreComputedColumn extends IXViewerPreComputedColumn {

   @Override
   default public String getText(Object obj, Long key, String cachedValue) {
      return cachedValue;
   }

   @Override
   default public Long getKey(Object obj) {
      Long result = 0L;
      if (obj instanceof IAtsObject) {
         result = ((IAtsObject) obj).getId();
      }
      return result;
   }

}
