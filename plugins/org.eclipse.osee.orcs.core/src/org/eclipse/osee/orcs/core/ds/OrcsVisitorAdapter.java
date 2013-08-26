/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.ds;

import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public class OrcsVisitorAdapter implements OrcsVisitor {

   @SuppressWarnings("unused")
   @Override
   public void visit(ArtifactData data) throws OseeCoreException {
      //
   }

   @SuppressWarnings("unused")
   @Override
   public void visit(AttributeData data) throws OseeCoreException {
      //
   }

   @SuppressWarnings("unused")
   @Override
   public void visit(RelationData data) throws OseeCoreException {
      //
   }

}