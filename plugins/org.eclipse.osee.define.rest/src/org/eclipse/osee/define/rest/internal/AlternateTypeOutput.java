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
package org.eclipse.osee.define.rest.internal;

import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;

/**
 * @author David W. Miller
 */
public class AlternateTypeOutput {
   ArtifactTypeToken alternateArtifact;
   AttributeTypeId alternateAttribute;

   public AlternateTypeOutput(ArtifactTypeToken alternateArtifact, AttributeTypeId alternateAttribute) {
      this.alternateArtifact = alternateArtifact;
      this.alternateAttribute = alternateAttribute;
   }

   public ArtifactTypeToken getAlternateArtifactType() {
      return alternateArtifact;
   }

   public AttributeTypeId getAlternateAttributeType() {
      return alternateAttribute;
   }
}
