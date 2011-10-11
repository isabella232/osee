/*******************************************************************************
 * Copyright (c) 20011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.view.web;

import org.eclipse.osee.display.view.web.OseeFooter;
import org.eclipse.osee.vaadin.widgets.AccountMenuBar;
import org.eclipse.osee.vaadin.widgets.HasViews;
import org.eclipse.osee.vaadin.widgets.Navigator;
import org.eclipse.osee.vaadin.widgets.Navigator.View;
import org.eclipse.osee.vaadin.widgets.Navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Shawn F. Cook
 */
@SuppressWarnings("serial")
public class AtsWindowFactory {

   public Window createNavigatableWindow(HasViews provider, AtsNavigator navigator) {

      VerticalLayout layout = new VerticalLayout();
      final Window w = new Window("OSEE", layout);

      for (final Class<?> viewClass : provider.getViews()) {
         navigator.addView(viewClass.getSimpleName(), viewClass);
      }
      w.addComponent(createNavigationBar(navigator, provider));
      w.addComponent(navigator);
      w.addComponent(createFooter());

      layout.setMargin(false);
      layout.setSpacing(true);
      layout.setSizeFull();
      layout.setExpandRatio(navigator, 1.0f);

      navigator.addListener(new ViewChangeListener() {
         @Override
         public void navigatorViewChange(View previous, View current) {
            Window mainWindow = w.getApplication().getMainWindow();
            mainWindow.setCaption("OSEE - " + current.getClass().getSimpleName());
            //            mainWindow.showNotification("Navigated to " + current.getClass().getName());
         }
      });
      return w;
   }

   private Component createFooter() {
      return new OseeFooter();
   }

   private Component createNavigationBar(final Navigator navigator, HasViews provider) {
      HorizontalLayout layout = new HorizontalLayout();

      MenuBar menu = new MenuBar();
      layout.addComponent(menu);

      menu.setWidth("100%");
      layout.setWidth("100%");

      MenuBar menuBar = new AccountMenuBar();
      layout.addComponent(menuBar);

      layout.setComponentAlignment(menu, Alignment.BOTTOM_LEFT);
      layout.setComponentAlignment(menuBar, Alignment.BOTTOM_RIGHT);
      layout.setExpandRatio(menu, 1.0f);
      return layout;
   }
}
