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

package org.eclipse.osee.ats.ide.demo.traceability;

import java.nio.CharBuffer;
import java.util.Collection;
import java.util.HashSet;
import org.eclipse.osee.define.ide.traceability.ITraceParser;
import org.eclipse.osee.define.ide.traceability.data.TraceMark;

/**
 * @author John R. Misinco
 */
public class DemoTraceParser implements ITraceParser {

   @Override
   public Collection<TraceMark> getTraceMarks(CharBuffer fileBuffer) {
      Collection<TraceMark> traceMarks = new HashSet<>();
      String type = "Uses";
      traceMarks.add(new TraceMark(type, "Collaborative Robot"));
      traceMarks.add(new TraceMark(type, "Robot Object"));
      traceMarks.add(new TraceMark(type, "Robot Interfaces"));
      traceMarks.add(new TraceMark(type, "Individual robot events"));
      traceMarks.add(new TraceMark(type, "Haptic Constraints"));

      return traceMarks;
   }

   @Override
   public CharBuffer removeTraceMarks(CharBuffer fileBuffer) {
      return fileBuffer;
   }

   @Override
   public boolean isTraceRemovalAllowed() {
      return false;
   }

   @Override
   public boolean addIfEmpty() {
      return false;
   }
}
