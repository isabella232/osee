/*********************************************************************
 * Copyright (c) 2010 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.core.dsl.ui.integration.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.OrcsTokenService;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.dsl.integration.ArtifactDataProvider;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Roberto E. Escobar
 */
public final class ArtifactDataProviderImpl implements ArtifactDataProvider {
   private OrcsTokenService tokenService;

   public void setOrcsTokenService(OrcsTokenService tokenService) {
      this.tokenService = tokenService;
   }

   @Override
   public boolean isApplicable(Object object) {
      boolean result = false;
      try {
         result = asCastedObject(object) != null;
      } catch (OseeCoreException ex) {
         OseeLog.log(DslUiIntegrationConstants.class, Level.SEVERE, ex);
      }
      return result;
   }

   @Override
   public ArtifactProxy asCastedObject(Object object) {
      XArtifactProxy proxy = null;
      if (object instanceof Artifact) {
         final Artifact artifact = (Artifact) object;
         proxy = new XArtifactProxy(artifact);
      } else if (object instanceof BranchId) {
         BranchId branch = (BranchId) object;
         final Artifact artifact = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(branch);
         proxy = new XArtifactProxy(artifact);
      }
      return proxy;
   }

   private final class XArtifactProxy implements ArtifactProxy {
      private final Artifact self;

      public XArtifactProxy(Artifact self) {
         this.self = self;
      }

      @Override
      public String getGuid() {
         return self.getGuid();
      }

      @Override
      public ArtifactTypeToken getArtifactType() {
         return self.getArtifactType();
      }

      @Override
      public boolean isOfType(ArtifactTypeId... artifactTypes) {
         return self.isOfType(artifactTypes);
      }

      @Override
      public boolean isAttributeTypeValid(AttributeTypeId attributeType) {
         return self.isAttributeTypeValid(attributeType);
      }

      @Override
      public Collection<RelationTypeToken> getValidRelationTypes() {
         return tokenService.getValidRelationTypes(self.getArtifactType());
      }

      @Override
      public Collection<ArtifactProxy> getHierarchy() {
         Collection<ArtifactProxy> hierarchy = new HashSet<>();
         try {
            Artifact artifactPtr = self.getParent();
            while (artifactPtr != null) {
               if (!hierarchy.add(new XArtifactProxy(artifactPtr))) {
                  OseeLog.log(DslUiIntegrationConstants.class, Level.SEVERE,
                     String.format("Cycle detected with artifact: %s", artifactPtr));
                  return Collections.emptyList();
               }
               artifactPtr = artifactPtr.getParent();
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(DslUiIntegrationConstants.class, Level.SEVERE, ex);
         }
         return hierarchy;
      }

      @Override
      public boolean matches(Id... identities) {
         return self.matches(identities);
      }

      @Override
      public BranchId getBranch() {
         return self.getBranch();
      }

      @Override
      public IOseeBranch getBranchToken() {
         return self.getBranchToken();
      }

      @Override
      public String getName() {
         return self.getName();
      }

      @Override
      public int hashCode() {
         return self.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         return self.equals(obj);
      }

      @Override
      public String toString() {
         return self.toString();
      }

      @Override
      public Long getId() {
         return self.getId();
      }
   }
}
