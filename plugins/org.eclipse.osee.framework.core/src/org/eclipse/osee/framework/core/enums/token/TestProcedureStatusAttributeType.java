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
import org.eclipse.osee.framework.core.enums.token.TestProcedureStatusAttributeType.TestProcedureStatusEnum;

/**
 * @author Stephen J. Molaro
 */
public class TestProcedureStatusAttributeType extends AttributeTypeEnum<TestProcedureStatusEnum> {

   public final TestProcedureStatusEnum NotPerformed = new TestProcedureStatusEnum(0, "Not Performed");
   public final TestProcedureStatusEnum CompletedAnalysisInWork =
      new TestProcedureStatusEnum(1, "Completed -- Analysis in Work");
   public final TestProcedureStatusEnum CompletedPassed = new TestProcedureStatusEnum(2, "Completed -- Passed");
   public final TestProcedureStatusEnum CompletedWithIssues =
      new TestProcedureStatusEnum(3, "Completed -- With Issues");
   public final TestProcedureStatusEnum CompletedWithIssuesResolved =
      new TestProcedureStatusEnum(4, "Completed -- With Issues Resolved");
   public final TestProcedureStatusEnum PartiallyComplete = new TestProcedureStatusEnum(5, "Partially Complete");

   public TestProcedureStatusAttributeType(TaggerTypeToken taggerType, String mediaType, NamespaceToken namespace) {
      super(1152921504606847075L, namespace, "Test Procedure Status", mediaType, "", taggerType, 6);
   }

   public class TestProcedureStatusEnum extends EnumToken {
      public TestProcedureStatusEnum(int ordinal, String name) {
         super(ordinal, name);
         addEnum(this);
      }
   }
}