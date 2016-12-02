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
package org.eclipse.osee.framework.core.data;

import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.Named;
import org.eclipse.osee.framework.jdk.core.type.NamedIdBase;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Ryan D. Brooks
 */
public interface IOseeBranch extends BranchId, Named {
   static final int SHORT_NAME_LIMIT = 35;
   IOseeBranch SENTINEL = create(Id.SENTINEL, "SENTINEL");

   default String getShortName() {
      return getShortName(SHORT_NAME_LIMIT);
   }

   default String getShortName(int length) {
      return Strings.truncate(getName(), length);
   }

   public static IOseeBranch create(String name) {
      return create(Lib.generateUuid(), name);
   }

   public static IOseeBranch create(BranchId id, String name) {
      return create(id.getId(), name, ArtifactId.SENTINEL);
   }

   public static IOseeBranch create(long id, String name) {
      return create(Long.valueOf(id), name, ArtifactId.SENTINEL);
   }

   public static IOseeBranch create(Long id, String name, ArtifactId branchView) {
      final class BranchTokenImpl extends NamedIdBase implements IOseeBranch {
         private ArtifactId branchView;

         public BranchTokenImpl(Long id, String name, ArtifactId branchView) {
            super(id, name);
            this.branchView = branchView;
         }

         @Override
         public ArtifactId getView() {
            return branchView;
         }

         @Override
         public boolean equals(Object obj) {
            if (obj instanceof BranchId) {
               return super.equals(obj) && branchView.equals(((BranchId) obj).getView());
            }

            return false;
         }
      }
      return new BranchTokenImpl(id, name, branchView);
   }

   public static IOseeBranch create(Long id, String name) {
      return create(id, name, ArtifactId.SENTINEL);
   }
}