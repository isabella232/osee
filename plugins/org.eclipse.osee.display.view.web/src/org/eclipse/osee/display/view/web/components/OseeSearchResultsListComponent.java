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

package org.eclipse.osee.display.view.web.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.osee.display.api.components.SearchResultComponent;
import org.eclipse.osee.display.api.components.SearchResultsListComponent;
import org.eclipse.osee.display.api.data.ViewSearchParameters;
import org.eclipse.osee.display.api.search.SearchNavigator;
import org.eclipse.osee.display.api.search.SearchPresenter;
import org.eclipse.osee.display.view.web.CssConstants;
import org.eclipse.osee.display.view.web.OseeUiApplication;
import org.eclipse.osee.display.view.web.components.OseePagingComponent.PageSelectedEvent;
import org.eclipse.osee.display.view.web.components.OseePagingComponent.PageSelectedListener;
import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * @author Shawn F. Cook
 */
@SuppressWarnings("serial")
public class OseeSearchResultsListComponent extends VerticalLayout implements SearchResultsListComponent, PageSelectedListener {

   private VerticalLayout mainLayout = new VerticalLayout();
   private VerticalLayout bottomSpacer = new VerticalLayout();
   private HorizontalLayout manySearchResultsHorizLayout = new HorizontalLayout();
   private OseePagingComponent pagingComponent = new OseePagingComponent();
   private List<OseeSearchResultComponent> resultList = new ArrayList<OseeSearchResultComponent>();
   private final CheckBox showVerboseCheckBox = new CheckBox("Show Detailed Results", false);
   private final ComboBox manyResultsComboBox = new ComboBox();
   private final int INIT_MANY_RES_PER_PAGE = 15;
   private final Label manySearchResults = new Label();
   private boolean isLayoutComplete = false;
   private SearchPresenter<?, ?> searchPresenter;
   private SearchNavigator navigator;

   @Override
   public void attach() {
      if (!isLayoutComplete) {
         try {
            OseeUiApplication<?, ?> app = (OseeUiApplication<?, ?>) this.getApplication();
            searchPresenter = app.getSearchPresenter();
            navigator = app.getNavigator();
         } catch (Exception e) {
            System.out.println("OseeSearchResultsListComponent.attach - CRITICAL ERROR: (AtsUiApplication) this.getApplication() threw an exception.");
         }
         createLayout();
         isLayoutComplete = true;
      }
   }

   @Override
   public void clearAll() {
      resultList.clear();
      pagingComponent.gotoFirstPage();
      updateManySearchResultsLabel();
      updateSearchResultsLayout();
   }

   private void createLayout() {
      setSizeFull();
      pagingComponent.addListener(this);
      pagingComponent.setManyItemsPerPage(INIT_MANY_RES_PER_PAGE);

      manySearchResultsHorizLayout.setSizeUndefined();

      mainLayout.setMargin(false, false, false, true);
      Panel mainLayoutPanel = new Panel();
      mainLayoutPanel.setScrollable(true);
      mainLayoutPanel.getContent().setSizeUndefined();
      mainLayoutPanel.setSizeFull();

      bottomSpacer.setSizeFull();

      showVerboseCheckBox.setImmediate(true);
      showVerboseCheckBox.addListener(new Property.ValueChangeListener() {
         @Override
         public void valueChange(ValueChangeEvent event) {
            boolean showVerbose = showVerboseCheckBox.toString().equalsIgnoreCase("true");
            OseeUiApplication<?, ?> app = (OseeUiApplication<?, ?>) getApplication();
            String url = "";
            if (app != null) {
               url = app.getRequestedDataId();
            }
            ViewSearchParameters params = new ViewSearchParameters(null, null, showVerbose);
            searchPresenter.selectSearch(url, params, navigator);
         }
      });

      manyResultsComboBox.setImmediate(true);
      manyResultsComboBox.setTextInputAllowed(false);
      manyResultsComboBox.setNullSelectionAllowed(false);
      manyResultsComboBox.addItem("5");
      manyResultsComboBox.addItem("15");
      manyResultsComboBox.addItem("50");
      manyResultsComboBox.addItem("100");
      manyResultsComboBox.addItem("All");
      manyResultsComboBox.setValue((new Integer(INIT_MANY_RES_PER_PAGE)).toString());
      manyResultsComboBox.setWidth(50, UNITS_PIXELS);
      manyResultsComboBox.addListener(new Property.ValueChangeListener() {
         @Override
         public void valueChange(ValueChangeEvent event) {
            if (pagingComponent != null) {
               String manyItemsPerPage_str = (String) manyResultsComboBox.getValue();
               if (manyItemsPerPage_str.equalsIgnoreCase("All")) {
                  pagingComponent.setAllItemsPerPage();
               } else {
                  int manyItemsPerPage = Integer.parseInt(manyItemsPerPage_str);
                  pagingComponent.setManyItemsPerPage(manyItemsPerPage);
               }
               updateSearchResultsLayout();
            }
         }
      });

      Label hSpacer_ManyRes = new Label();
      hSpacer_ManyRes.setWidth(5, UNITS_PIXELS);
      Label manySearchResults_suffix = new Label("Results Found");
      manySearchResults.setSizeUndefined();
      manySearchResults_suffix.setSizeUndefined();
      manySearchResults.setStyleName(CssConstants.OSEE_SEARCHRESULT_MATCH_MANY);

      Label hSpacer_ManyResVerbose = new Label();
      hSpacer_ManyResVerbose.setWidth(30, UNITS_PIXELS);

      Label hSpacer_VerbosePerPage = new Label();
      hSpacer_VerbosePerPage.setWidth(30, UNITS_PIXELS);
      Label hSpacer_PerPage = new Label();
      hSpacer_PerPage.setWidth(5, UNITS_PIXELS);
      Label manyResultsLabel = new Label("Results Per Page");

      manySearchResultsHorizLayout.addComponent(manySearchResults);
      manySearchResultsHorizLayout.addComponent(hSpacer_ManyRes);
      manySearchResultsHorizLayout.addComponent(manySearchResults_suffix);
      manySearchResultsHorizLayout.addComponent(hSpacer_ManyResVerbose);
      manySearchResultsHorizLayout.addComponent(showVerboseCheckBox);
      manySearchResultsHorizLayout.addComponent(hSpacer_VerbosePerPage);
      manySearchResultsHorizLayout.addComponent(manyResultsComboBox);
      manySearchResultsHorizLayout.addComponent(hSpacer_PerPage);
      manySearchResultsHorizLayout.addComponent(manyResultsLabel);

      mainLayoutPanel.setContent(mainLayout);
      mainLayout.addComponent(bottomSpacer);

      addComponent(manySearchResultsHorizLayout);
      addComponent(mainLayoutPanel);
      addComponent(pagingComponent);

      manySearchResultsHorizLayout.setComponentAlignment(manyResultsComboBox, Alignment.TOP_CENTER);
      mainLayout.setExpandRatio(bottomSpacer, 1.0f);
      this.setExpandRatio(mainLayoutPanel, 1.0f);
   }

   private void updateManySearchResultsLabel() {
      String manyResults = String.format("[%d]", resultList.size());
      manySearchResults.setCaption(manyResults);
      pagingComponent.setManyItemsTotal(resultList.size());
   }

   private void updateSearchResultsLayout() {
      //if the list of currently visible items has not changed, then don't bother updating the layout
      Collection<Integer> resultListIndices = pagingComponent.getCurrentVisibleItemIndices();
      boolean showVerbose = showVerboseCheckBox.toString().equalsIgnoreCase("true");

      //First, get a list of all the search results components currently in the layout
      Collection<Component> removeTheseComponents = new ArrayList<Component>();
      for (Iterator<Component> iter = mainLayout.getComponentIterator(); iter.hasNext();) {
         Component component = iter.next();
         if (component.getClass() == OseeSearchResultComponent.class) {
            removeTheseComponents.add(component);
         }
      }

      //Second, remove the search result components
      for (Component component : removeTheseComponents) {
         mainLayout.removeComponent(component);
      }

      //Next, add the result components to the layout that are on the current 'page'
      for (Integer i : resultListIndices) {
         try {
            OseeSearchResultComponent searchResultComp = resultList.get(i);
            searchResultComp.setShowVerboseSearchResults(showVerbose);
            mainLayout.addComponent(searchResultComp, 0);
         } catch (IndexOutOfBoundsException e) {
            System.out.println("OseeSearchResultsListComponent.updateSearchResultsLayout - CRITICAL ERROR: IndexOutOfBoundsException e");
         }
      }
   }

   @Override
   public SearchResultComponent createSearchResult() {
      OseeSearchResultComponent searchResultComp = new OseeSearchResultComponent();
      resultList.add(searchResultComp);
      updateManySearchResultsLabel();

      int lastCompIndex = mainLayout.getComponentIndex(bottomSpacer);
      mainLayout.addComponent(searchResultComp, lastCompIndex);

      return searchResultComp;
   }

   @Override
   public void setErrorMessage(String message) {
      Application app = this.getApplication();
      if (app != null) {
         Window mainWindow = app.getMainWindow();
         if (mainWindow != null) {
            mainWindow.showNotification(message, Notification.TYPE_ERROR_MESSAGE);
         } else {
            System.out.println("OseeSearchResultsListComponent.setErrorMessage - ERROR: Application.getMainWindow() returns null value.");
         }
      } else {
         System.out.println("OseeSearchResultsListComponent.setErrorMessage - ERROR: getApplication() returns null value.");
      }
   }

   @Override
   public void pageSelected(PageSelectedEvent source) {
      updateSearchResultsLayout();
   }
}
