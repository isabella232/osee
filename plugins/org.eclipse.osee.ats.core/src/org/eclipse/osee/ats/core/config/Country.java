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
package org.eclipse.osee.ats.core.config;

import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.country.IAtsCountry;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.model.impl.AtsConfigObject;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.logger.Log;

/**
 * @author Donald G Dunne
 */
public class Country extends AtsConfigObject implements IAtsCountry {

   public Country(Log logger, IAtsServices services, ArtifactId artifact) {
      super(logger, services, artifact);
   }

   @Override
   public String getTypeName() {
      return "Country";
   }

   @Override
   public Long getId() {
      return artifact.getId();
   }

   @Override
   public String getDescription() {
      return services.getAttributeResolver().getSoleAttributeValue(artifact, AtsAttributeTypes.Description, "");
   }
}
