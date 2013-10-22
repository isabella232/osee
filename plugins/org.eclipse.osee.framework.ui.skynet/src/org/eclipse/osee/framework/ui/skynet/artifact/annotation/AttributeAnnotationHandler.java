/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.artifact.annotation;

import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;

/**
 * @author Donald G. Dunne
 */
public class AttributeAnnotationHandler implements IArtifactAnnotation {

   @Override
   public void getAnnotations(Artifact artifact, Set<ArtifactAnnotation> annotations) {
      try {
         if (artifact.isAttributeTypeValid(CoreAttributeTypes.Annotation)) {
            AttributeAnnotationManager mgr = AttributeAnnotationManager.get(artifact);
            annotations.addAll(mgr.getAnnotations());
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }
}
