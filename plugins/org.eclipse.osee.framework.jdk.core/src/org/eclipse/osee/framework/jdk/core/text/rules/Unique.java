/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.jdk.core.text.rules;

import java.util.LinkedHashSet;
import org.eclipse.osee.framework.jdk.core.text.Rule;
import org.eclipse.osee.framework.jdk.core.text.change.ChangeSet;

/**
 * @author Ryan D. Brooks
 */
public class Unique extends Rule {
   @Override
   public ChangeSet computeChanges(CharSequence seq) {
      LinkedHashSet<Object> set = new LinkedHashSet<>();
      int start = 0;
      int end = 0;
      int length = seq.length();
      ChangeSet changeSet = new ChangeSet(seq);

      for (int i = 0; i < length; i++) {
         if (seq.charAt(i) == '\n') {
            end = i + 1;
            if (!set.add(seq.subSequence(start, end))) { // if set already contains this line
               changeSet.delete(start, end); // then skip over it
            }
            start = end;
         }
      }
      ruleWasApplicable = true;
      return changeSet;
   }
}