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
package org.eclipse.osee.framework.ui.skynet;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;

/**
 * This class allows plugins to provide the base images for artifact types by registering via ImageManger.registerImage.
 * It also provides the ability for programatic override of image creation by
 * ImageManager.registerImageOverrideProvider. Registering to be override provider will cause the appropriate setupImage
 * calls to be executed when the image is needed. All overlays and base images are then provided out of this provider.
 * 
 * @author Ryan D. Brooks
 */
public abstract class ArtifactImageProvider {
   /**
    * Providers can return null which will cause null to be returned from the associated getImage or getImageDescriptor
    * call. Alternatively, providers that wish to defer to the basic implementation should call return
    * super.setupImage()
    */
   @SuppressWarnings("unused")
   public String setupImage(IArtifact artifact) throws OseeCoreException {
      return ArtifactImageManager.setupImageNoProviders(artifact);
   }

   @SuppressWarnings("unused")
   public String setupImage(IArtifactType artifactType) throws OseeCoreException {
      return ArtifactImageManager.setupImage(BaseImage.getBaseImageEnum(artifactType));
   }

   /**
    * Provide image artifact type registration by ImageManager.register.* calls
    */
   public abstract void init() throws OseeCoreException;

}