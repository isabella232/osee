/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.display.view.web.search;

import org.eclipse.osee.display.api.components.SearchHeaderComponent;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Shawn F. Cook
 */
@SuppressWarnings("serial")
public class OseeSearchHeaderComponent extends VerticalLayout implements SearchHeaderComponent {

   @Override
   public void attach() {
      createLayout();
   }

   protected void createLayout() {
      //Do nothing.
   }

   @Override
   public void clearAll() {
      //Do nothing.
   }

   @Override
   public void setErrorMessage(String message) {
      //Do nothing.
   }

}
