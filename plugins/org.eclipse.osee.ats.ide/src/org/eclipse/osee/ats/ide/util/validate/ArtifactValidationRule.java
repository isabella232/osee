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

package org.eclipse.osee.ats.ide.util.validate;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.rule.validation.AbstractValidationRule;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.validation.IOseeValidator;
import org.eclipse.osee.framework.skynet.core.validation.OseeValidator;

/**
 * @author Shawn F. Cook
 */
public class ArtifactValidationRule extends AbstractValidationRule {

   public ArtifactValidationRule(AtsApi atsApi) {
      super(atsApi);
   }

   @Override
   public void validate(ArtifactToken artToken, XResultData results) {
      Artifact artifact = AtsClientService.get().getQueryServiceClient().getArtifact(artToken);
      Conditions.assertNotNull(artifact, "artifact not found for %s", artToken.toStringWithId());
      IStatus status = OseeValidator.getInstance().validate(IOseeValidator.LONG, artifact);

      if (!status.isOK()) {
         logError(artifact, status.getMessage(), results);
      }
   }

   @Override
   public String getRuleDescription() {
      return "All Errors reported must be fixed.";
   }

   @Override
   public String getRuleTitle() {
      return "Artifact Validation Checks:";
   }

}
