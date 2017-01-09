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
package org.eclipse.osee.ats.rest.internal.config;

import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.util.IAtsDatabaseConversion;
import org.eclipse.osee.ats.rest.IAtsServer;
import org.eclipse.osee.framework.core.util.XResultData;

/**
 * See description below
 *
 * @author Donald G Dunne
 */
public class ConvertCreateUpdateAtsConfig implements IAtsDatabaseConversion {

   private final IAtsServer atsServer;

   public ConvertCreateUpdateAtsConfig(IAtsServer atsServer) {
      this.atsServer = atsServer;
   }

   @Override
   public void run(XResultData data, boolean reportOnly, IAtsServices services) {
      if (reportOnly) {
         data.log("No REPORT-ONLY available for this conversion.\n");
      } else {
         UpdateAtsConfiguration update = new UpdateAtsConfiguration(atsServer);
         update.createUpdateConfig(data);
         data.log("Complete");
      }
   }

   @Override
   public String getDescription() {
      StringBuffer data = new StringBuffer();
      data.append("Updates AtsConfig artifact (optional but recommended conversion)\n\n");
      data.append("This will:\n" + "   - Create AtsConfig artifact, if not created\n" //
         + "   - Create Rule Definitions from support/ruleDefinitions.ats, if not created\n" //
         + "   - Create/Update Views from support/views.json\n" //
         + "   - Create Color Team Column, if not created.\n" //
         + "   - Create/Update Valid State Names\n\n" //
         + "Can be run multiple times without corruption.\n" //
         + "Should be run after each release.");
      return data.toString();
   }

   @Override
   public String getName() {
      return "Create or Update AtsConfig";
   }

}
