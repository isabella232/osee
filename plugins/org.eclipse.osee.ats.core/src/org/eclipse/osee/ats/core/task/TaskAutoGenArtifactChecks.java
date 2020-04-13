/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.task;

import java.util.Collection;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.util.AtsUtil;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.core.internal.AtsApiService;
import org.eclipse.osee.framework.core.access.ArtifactCheck;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.jdk.core.result.XResultData;

/**
 * Verify that task is not an auto-generated task prior to deletion or name modification
 *
 * @author Donald G. Dunne
 */
public class TaskAutoGenArtifactChecks extends ArtifactCheck {
   private static boolean deletionChecksEnabled = !AtsUtil.isInTest();

   @Override
   public XResultData isDeleteable(Collection<ArtifactToken> artifacts, XResultData results) {
      if (deletionChecksEnabled) {
         AtsApi atsApi = AtsApiService.get();
         if (artifacts.isEmpty()) {
            return results;
         }
         boolean isAtsAdmin = atsApi.getUserService().isAtsAdmin();
         checkAutoGeneratedTasks(isAtsAdmin, atsApi, artifacts, results);
      }
      return results;
   }

   @Override
   public XResultData isRenamable(Collection<ArtifactToken> artifacts, XResultData results) {
      AtsApi atsApi = AtsApiService.get();
      for (ArtifactToken artifact : artifacts) {
         if (artifact.isOfType(AtsArtifactTypes.Task) && atsApi.getTaskService().isAutoGen((IAtsTask) artifact)) {
            results.errorf("Invalid to rename LBA ATS auto-generated task %s", artifact.toStringWithId());
         }
      }
      return results;
   }

   private void checkAutoGeneratedTasks(boolean isAtsAdmin, AtsApi atsApi, Collection<ArtifactToken> artifacts, XResultData results) {
      for (ArtifactToken artifact : artifacts) {
         if (artifact.isOfType(AtsArtifactTypes.Task) && atsApi.getTaskService().isAutoGen((IAtsTask) artifact)) {
            results.errorf("Invalid to delete LBA ATS auto-generated task %s", artifact.toStringWithId());
         }
      }
   }
}
