/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.config;

import javax.ws.rs.Path;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.impl.IAtsServer;
import org.eclipse.osee.ats.impl.config.AbstractConfigResource;

/**
 * Donald G. Dunne
 */
@Path("insertionactivity")
public class InsertionActivityResource extends AbstractConfigResource {

   public InsertionActivityResource(IAtsServer atsServer) {
      super(AtsArtifactTypes.InsertionActivity, atsServer);
   }

}
