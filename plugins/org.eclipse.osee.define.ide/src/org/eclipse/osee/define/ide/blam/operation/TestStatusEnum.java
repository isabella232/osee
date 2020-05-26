/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.define.ide.blam.operation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan D. Brooks
 */
public enum TestStatusEnum {
   DEFAULT_CODE(""),
   NOT_PERFORMED_CODE("Not Performed"),
   COMPLETED_ANALYSIS_CODE("Completed -- Analysis in Work"),
   COMPLETED_PASSED_CODE("Completed -- Passed"),
   COMPLETED_WITH_ISSUES_CODE("Completed -- With Issues"),
   COMPLETED_WITH_ISSUES_RESOLVED_CODE("Completed -- With Issues Resolved"),
   PARTIALLY_COMPLETED_CODE("Partially Complete");
   private final static Map<String, TestStatusEnum> testStatusToCodeMap = new HashMap<>();

   public String testStatus;

   TestStatusEnum(String testStatus) {
      this.testStatus = testStatus;
   }

   public static TestStatusEnum fromString(String value) {
      if (testStatusToCodeMap.isEmpty()) {
         for (TestStatusEnum enumStatus : TestStatusEnum.values()) {
            TestStatusEnum.testStatusToCodeMap.put(enumStatus.testStatus, enumStatus);
         }
      }
      TestStatusEnum toReturn = testStatusToCodeMap.get(value);
      return toReturn != null ? toReturn : DEFAULT_CODE;
   }
}
