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
package org.eclipse.osee.template.engine;

import org.eclipse.osee.framework.core.data.Identity;
import org.eclipse.osee.framework.core.data.Named;

/**
 * @author Ryan D. Brooks
 */
public final class IdentifiableOptionsRule<T extends Identity<String> & Named> extends OptionsRule<T> {
   Iterable<T> options;

   public IdentifiableOptionsRule(String ruleName, Iterable<T> options) {
      this(ruleName, options, null);
   }

   public IdentifiableOptionsRule(String ruleName, Iterable<T> options, String listId) {
      super(ruleName, listId);
      this.options = options;
   }

   @Override
   public Iterable<T> getOptions() {
      return options;
   }
}