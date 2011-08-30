/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.support.test.util;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.TokenFactory;

/**
 * @author Ryan D. Brooks
 */
public final class DemoArtifactTypes {

   // @formatter:off
   public static final IArtifactType DemoCodeTeamWorkflow = TokenFactory.createArtifactType(0x000000000000004FL, "Demo Code Team Workflow");
   public static final IArtifactType DemoReqTeamWorkflow = TokenFactory.createArtifactType(0x0000000000000050L, "Demo Req Team Workflow");
   public static final IArtifactType DemoTestTeamWorkflow = TokenFactory.createArtifactType(0x0000000000000051L, "Demo Test Team Workflow");
   // @formatter:on

   private DemoArtifactTypes() {
      // Constants
   }
}
