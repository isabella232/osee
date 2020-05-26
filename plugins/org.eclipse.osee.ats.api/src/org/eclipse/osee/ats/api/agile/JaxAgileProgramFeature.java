/*********************************************************************
 * Copyright (c) 2017 Boeing
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

package org.eclipse.osee.ats.api.agile;

import org.eclipse.osee.ats.api.config.JaxAtsObject;
import org.eclipse.osee.framework.core.data.ArtifactToken;

/**
 * @author Donald G. Dunne
 */
public class JaxAgileProgramFeature extends JaxAtsObject {

   private long programBacklogItemId;

   public static JaxAgileProgramFeature construct(IAgileProgramBacklogItem programBacklogItem, ArtifactToken programFeature) {
      return construct(programBacklogItem.getId(), programFeature);
   }

   public static JaxAgileProgramFeature construct(Long programBacklogItemid, ArtifactToken programFeature) {
      JaxAgileProgramFeature feature = new JaxAgileProgramFeature();
      feature.setName(programFeature.getName());
      feature.setId(programFeature.getId());
      feature.setProgramBacklogItemId(programBacklogItemid);
      return feature;
   }

   public long getProgramBacklogItemId() {
      return programBacklogItemId;
   }

   public void setProgramBacklogItemId(long programBacklogItemId) {
      this.programBacklogItemId = programBacklogItemId;
   }

}
