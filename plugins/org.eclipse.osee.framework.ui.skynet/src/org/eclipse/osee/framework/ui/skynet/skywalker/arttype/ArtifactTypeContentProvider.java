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

package org.eclipse.osee.framework.ui.skynet.skywalker.arttype;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;

/**
 * @author Donald G. Dunne
 */
public class ArtifactTypeContentProvider implements IGraphEntityContentProvider {
   // private static final Collection<Artifact>EMPTY_LIST = new ArrayList<>(0);

   public ArtifactTypeContentProvider() {
      super();
   }

   @Override
   public Object[] getConnectedTo(Object entity) {
      try {
         if (entity instanceof ArtifactType) {
            return ((ArtifactType) entity).getFirstLevelDescendantTypes().toArray();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

   @Override
   public Object[] getElements(Object inputElement) {
      try {
         if (inputElement instanceof ArtifactType) {
            Set<ArtifactType> artifactTypes = new HashSet<>();
            artifactTypes.add((ArtifactType) inputElement);
            getDecendents((ArtifactType) inputElement, artifactTypes);
            return artifactTypes.toArray();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

   public void getDecendents(ArtifactType artifactType, Set<ArtifactType> decendents) throws OseeCoreException {
      for (ArtifactType artType : artifactType.getFirstLevelDescendantTypes()) {
         decendents.add(artType);
         getDecendents(artType, decendents);
      }
   }

   public double getWeight(Object entity1, Object entity2) {
      return 0;
   }

   @Override
   public void dispose() {
      // do nothing
   }

   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // do nothing
   }

}
