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
package org.eclipse.osee.framework.ui.skynet.search;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import static org.eclipse.osee.framework.core.enums.DeletionFlag.INCLUDE_DELETED;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.message.SearchOptions;
import org.eclipse.osee.framework.core.message.SearchRequest;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.help.ui.OseeHelpContext;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.panels.SearchComposite;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionExceptionComposite;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericViewPart;
import org.eclipse.osee.framework.ui.skynet.widgets.XBranchSelectWidget;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

/**
 * @author Robert A. Fisher
 * @author Ryan D. Brooks
 */
public class QuickSearchView extends GenericViewPart implements Listener {
   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.skynet.QuickSearchView";

   private static final String ENTRY_SEPARATOR = "##";
   private static final String LAST_QUERY_KEY_ID = "lastQuery";
   private static final String LAST_BRANCH_GUID = "lastBranch";
   private static final String QUERY_HISTORY_KEY_ID = "queryHistory";

   private Label branchLabel;
   private XBranchSelectWidget branchSelect;
   private SearchComposite searchComposite;
   private QuickSearchOptionComposite optionsComposite;
   private IMemento memento;

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);
      if (memento != null) {
         this.memento = memento;
      }
   }

   @Override
   public void saveState(IMemento memento) {
      if (memento != null) {
         if (Widgets.isAccessible(searchComposite)) {
            memento.putString(LAST_QUERY_KEY_ID, searchComposite.getQuery());
            IOseeBranch branch = branchSelect.getData();
            if (branch != null) {
               memento.putString(LAST_BRANCH_GUID, branch.getGuid());
            }
            StringBuilder builder = new StringBuilder();
            String[] queries = searchComposite.getQueryHistory();
            for (int index = 0; index < queries.length; index++) {
               try {
                  builder.append(URLEncoder.encode(queries[index], "UTF-8"));
                  if (index + 1 < queries.length) {
                     builder.append(ENTRY_SEPARATOR);
                  }
               } catch (UnsupportedEncodingException ex) {
                  // DO NOTHING
               }
            }
            memento.putString(QUERY_HISTORY_KEY_ID, builder.toString());
         }
         if (Widgets.isAccessible(optionsComposite)) {
            optionsComposite.saveState(memento);
         }
      }
   }

   private void loadState() {
      if (memento != null) {
         if (Widgets.isAccessible(searchComposite)) {
            String lastQuery = memento.getString(LAST_QUERY_KEY_ID);
            List<String> queries = new ArrayList<String>();
            String rawHistory = memento.getString(QUERY_HISTORY_KEY_ID);
            if (rawHistory != null) {
               String[] values = rawHistory.split(ENTRY_SEPARATOR);
               for (String value : values) {
                  try {
                     queries.add(URLDecoder.decode(value, "UTF-8"));
                  } catch (UnsupportedEncodingException ex) {
                     // DO NOTHING
                  }
               }
            }
            searchComposite.restoreWidgetValues(queries, lastQuery, null, null);
         }
         if (Widgets.isAccessible(optionsComposite)) {
            optionsComposite.loadState(memento);
         }
         if (branchSelect != null) {
            String guid = memento.getString(LAST_BRANCH_GUID);
            if (Strings.isValid(guid)) {
               try {
                  Branch branch = BranchManager.getBranchByGuid(guid);
                  branchSelect.setSelection(branch);
               } catch (OseeCoreException ex) {
                  // do nothing
               }
            }
         }
      }
   }

   @Override
   public void createPartControl(Composite parent) {
      if (!DbConnectionExceptionComposite.dbConnectionIsOk(parent)) {
         return;
      }

      parent.setLayout(new GridLayout());
      parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      branchSelect = new XBranchSelectWidget("");
      branchSelect.setDisplayLabel(false);
      branchSelect.createWidgets(parent, 2);
      branchSelect.addListener(this);
      // allow user to double click the branch text area to select the branch
      if (Widgets.isAccessible(branchSelect.getSelectComposite())) {
         if (Widgets.isAccessible(branchSelect.getSelectComposite().getBranchSelectText())) {
            branchSelect.getSelectComposite().getBranchSelectText().setDoubleClickEnabled(true);
         }
      }
      OseeStatusContributionItemFactory.addTo(this, true);

      Composite panel = new Composite(parent, SWT.NONE);
      GridLayout gL = new GridLayout();
      gL.marginHeight = 0;
      gL.marginWidth = 0;
      panel.setLayout(gL);
      panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      searchComposite = new SearchComposite(panel, SWT.NONE);
      searchComposite.addListener(this);

      optionsComposite = new QuickSearchOptionComposite(panel, SWT.NONE);
      optionsComposite.setLayout(ALayout.getZeroMarginLayout());
      optionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      loadState();
      compositeEnablement(searchComposite, false);
      searchComposite.setHelpContext(OseeHelpContext.QUICK_SEARCH);

      branchLabel = new Label(parent, SWT.NONE);
      branchLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      branchLabel.setText("");

      createClearHistoryAction();

      setFocusWidget(searchComposite);
   }

   private void createClearHistoryAction() {
      Action action = new Action("Clear Search History") {
         @Override
         public void run() {
            if (searchComposite != null) {
               searchComposite.clearHistory();
            }
         }
      };
      action.setToolTipText("Clears search history");
      action.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.REMOVE));
      getViewSite().getActionBars().getMenuManager().add(action);
   }

   private void compositeEnablement(SearchComposite composite, boolean enable) {
      if (Widgets.isAccessible(composite)) {
         for (Control cntrl : composite.getSearchChildren()) {
            cntrl.setEnabled(enable);
         }
      }
   }

   public void setBranch(IOseeBranch branch) {
      if (branchSelect != null) {
         branchSelect.setSelection(branch);
         // branch has been selected; allow user to set up search string
         compositeEnablement(searchComposite, true);
      }
   }

   @Override
   public void handleEvent(Event event) {
      if (Widgets.isAccessible(branchLabel) && branchSelect != null) {
         branchLabel.setText("");
         final IOseeBranch branch = branchSelect.getData();
         if (branch == null) {
            branchLabel.setText("Error: Must Select a Branch");
         } else if (Widgets.isAccessible(searchComposite) && searchComposite.isExecuteSearchEvent(event) && Widgets.isAccessible(optionsComposite)) {
            DeletionFlag allowDeleted = optionsComposite.isIncludeDeletedEnabled() ? INCLUDE_DELETED : EXCLUDE_DELETED;
            NewSearchUI.activateSearchResultView();

            ISearchQuery query;
            if (optionsComposite.isSearchByIdEnabled()) {
               query = new IdArtifactSearch(searchComposite.getQuery(), branch, allowDeleted);
            } else {
               SearchOptions options = new SearchOptions();
               options.setDeletedIncluded(allowDeleted);
               options.setAttributeTypeFilter(optionsComposite.getAttributeTypeFilter());
               options.setCaseSensive(optionsComposite.isCaseSensitiveEnabled());
               options.setFindAllLocationsEnabled(optionsComposite.isMatchAllLocationsEnabled());
               options.setMatchWordOrder(optionsComposite.isMatchWordOrderEnabled());

               SearchRequest searchRequest = new SearchRequest(branch, searchComposite.getQuery(), options);
               query = new RemoteArtifactSearch(searchRequest);
            }
            NewSearchUI.runQueryInBackground(query);
         } else {
            // branch has been selected; allow user to set up search string
            compositeEnablement(searchComposite, true);
         }
      }
   }
}
