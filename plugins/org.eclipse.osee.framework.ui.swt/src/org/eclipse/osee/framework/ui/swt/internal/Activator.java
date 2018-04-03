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
package org.eclipse.osee.framework.ui.swt.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Activator extends AbstractUIPlugin {

   public static final String PLUGIN_ID = "org.eclipse.osee.framework.ui.swt";
   private static Activator instance;

   public Activator() {
      instance = this;
   }

   public static Activator getInstance() {
      return instance;
   }

}
