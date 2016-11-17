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
package org.eclipse.osee.orcs.rest.model.writer.reader;

import org.eclipse.osee.framework.core.data.BranchId;

/**
 * Data Transfer object for Orcs Writer
 *
 * @author Donald G. Dunne
 */
public class OwBranch extends OwBase {

   public OwBranch() {
      // For jax-rs instantiation
      super(BranchId.SENTINEL.getId(), "");
   }

   public OwBranch(Long id, String name) {
      super(id, name);
   }

   @Override
   public String toString() {
      return "OwBranch [id=" + getId() + ", data=" + data + "]";
   }
}
