/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.workitem.model;

import org.eclipse.osee.ats.api.review.IAtsPeerToPeerReview;
import org.eclipse.osee.ats.rest.IAtsServer;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Donald G Dunne
 */
public class PeerToPeerReview extends AbstractReview implements IAtsPeerToPeerReview {

   public PeerToPeerReview(Log logger, IAtsServer atsServer, ArtifactReadable artifact) {
      super(logger, atsServer, artifact);
   }

}
