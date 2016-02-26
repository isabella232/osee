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
package org.eclipse.osee.ats.workflow;

import org.eclipse.osee.ats.api.util.AtsTopicEvent;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.editor.SMAEditorArtifactEventManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * @author Donald G. Dunne
 */
public class AtsWorkItemEventHandler implements EventHandler {

   @Override
   public void handleEvent(Event event) {
      try {
         if (event.getTopic().equals(AtsTopicEvent.WORK_ITEM_MODIFIED)) {
            for (Long workItemUuid : AtsObjects.uuidsToLong(";",
               (String) event.getProperty(AtsTopicEvent.WORK_ITEM_UUDS_KEY))) {
               Artifact artifact = ArtifactCache.getActive(workItemUuid, AtsUtilCore.getAtsBranch().getUuid());
               if (artifact != null) {
                  if (SMAEditorArtifactEventManager.isLoaded(artifact)) {
                     artifact.reloadAttributesAndRelations();
                  } else {
                     ArtifactCache.deCache(artifact);
                  }
               }
            }
         }
      } catch (Exception ex) {
         // do nothing
      }
   }

}
