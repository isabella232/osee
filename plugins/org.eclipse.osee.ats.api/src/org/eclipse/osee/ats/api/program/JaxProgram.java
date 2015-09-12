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
package org.eclipse.osee.ats.api.program;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.osee.ats.api.config.JaxNewAtsConfigObject;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class JaxProgram extends JaxNewAtsConfigObject {

   long countryUuid;

   public long getCountryUuid() {
      return countryUuid;
   }

   public void setCountryUuid(long countryUuid) {
      this.countryUuid = countryUuid;
   }
}
