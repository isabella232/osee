/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.ats.api.rule.validation;

import org.eclipse.osee.framework.jdk.core.result.XResultData;

/**
 * @author Shawn F. Cook
 */
public class ValidationResult {

   private XResultData results;

   public ValidationResult() {
      this.results = new XResultData();
   }

   public boolean didValidationPass() {
      return !results.isErrors();
   }

   public XResultData getResults() {
      return results;
   }

   public void setResults(XResultData results) {
      this.results = results;
   }

}
