/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.client.demo;

import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.TokenFactory;

/**
 * @author Roberto E. Escobar
 */
public final class DemoTypes {

   // @formatter:off
   public static final IArtifactType DemoTestRequirement = TokenFactory.createArtifactType(86, "Demo Artifact With Selective Partition");

   public static final IArtifactType DemoDslArtifact = TokenFactory.createArtifactType(204526342635554L, "Demo DSL Artifact");
   public static final AttributeTypeToken DemoDslAttribute = AttributeTypeToken.valueOf(1153126013769613775L, "AxRbLlj+c2ZHjBeumfgA");
   // @formatter:on

   private DemoTypes() {
      // Constants
   }
}