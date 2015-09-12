/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.insertion;

import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.insertion.IAtsInsertionActivity;
import org.eclipse.osee.ats.core.model.impl.AtsConfigObject;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.logger.Log;

/**
 * @author David W. Miller
 */
public class InsertionActivity extends AtsConfigObject implements IAtsInsertionActivity {

   private long insertionUuid;

   public InsertionActivity(Log logger, IAtsServices atsServer, ArtifactId artifact) {
      super(logger, atsServer, artifact);
   }

   @Override
   public String getTypeName() {
      return "Insertion Activity";
   }

   @Override
   public long getInsertionUuid() {
      return insertionUuid;
   }

   public void setInsertionUuid(long insertionUuid) {
      this.insertionUuid = insertionUuid;
   }

}
