/*******************************************************************************
 * Copyright (c) 2017 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.agile;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.config.JaxAtsObject;

/**
 * @author Donald G. Dunne
 */
public class JaxAgileProgram extends JaxAtsObject implements IAgileProgram {

   String id;
   List<Long> teamIds = new LinkedList<>();

   public JaxAgileProgram() {
      // for jax-rs
   }

   @Override
   public List<Long> getTeamIds() {
      return teamIds;
   }

   public void setTeamids(List<Long> teamIds) {
      this.teamIds = teamIds;
   }

}
