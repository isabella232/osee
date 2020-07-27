/*********************************************************************
 * Copyright (c) 2011 Boeing
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

package org.eclipse.osee.ats.ide.workflow.review;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.workflow.WorkItemType;
import org.eclipse.osee.ats.ide.AtsImage;
import org.eclipse.osee.ats.ide.actions.OpenWorkflowByIdAction;
import org.eclipse.osee.ats.ide.actions.ValidatePeerDefectsAction;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.ats.ide.navigate.SearchNavigateItem;
import org.eclipse.osee.ats.ide.world.search.AtsSearchReviewSearchItem;
import org.eclipse.osee.ats.ide.world.search.MyReviewSearchItem;
import org.eclipse.osee.ats.ide.world.search.ShowOpenWorkflowsByReviewType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.OpenPerspectiveNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.IXNavigateCommonItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateCommonItems;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateViewItems;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionUtility;

/**
 * @author Donald G. Dunne
 */
public class ReviewNavigateViewItems implements XNavigateViewItems, IXNavigateCommonItem {

   private final static ReviewNavigateViewItems instance = new ReviewNavigateViewItems();
   private final List<XNavigateItem> items = new CopyOnWriteArrayList<>();
   private boolean ensurePopulatedRanOnce = false;

   public static ReviewNavigateViewItems getInstance() {
      return instance;
   }

   @Override
   public List<XNavigateItem> getSearchNavigateItems() {
      ensurePopulated();
      return items;
   }

   private synchronized void ensurePopulated() {
      if (!ensurePopulatedRanOnce) {
         if (DbConnectionUtility.areOSEEServicesAvailable().isFalse()) {
            return;
         }
         this.ensurePopulatedRanOnce = true;

         try {
            addOseePeerSectionChildren(null);

            XNavigateCommonItems.addCommonNavigateItems(items, Arrays.asList(getSectionId()));
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   public void addOseePeerSectionChildren(XNavigateItem item) {
      items.add(new SearchNavigateItem(item, new MyReviewSearchItem()));
      items.add(new SearchNavigateItem(item, new AtsSearchReviewSearchItem()));
      items.add(new XNavigateItemAction(item, new OpenWorkflowByIdAction("Open Review by ID"), AtsImage.REVIEW));
      items.add(new SearchNavigateItem(item,
         new ShowOpenWorkflowsByReviewType("Show Open " + WorkItemType.DecisionReview.name() + "s",
            WorkItemType.DecisionReview, false, false, AtsImage.DECISION_REVIEW)));
      items.add(new SearchNavigateItem(item,
         new ShowOpenWorkflowsByReviewType("Show Workflows Waiting " + WorkItemType.DecisionReview.name() + "s",
            WorkItemType.DecisionReview, false, true, AtsImage.DECISION_REVIEW)));
      items.add(new SearchNavigateItem(item,
         new ShowOpenWorkflowsByReviewType("Show Open " + WorkItemType.PeerReview.name() + "s", WorkItemType.PeerReview,
            false, false, AtsImage.PEER_REVIEW)));
      items.add(new SearchNavigateItem(item,
         new ShowOpenWorkflowsByReviewType("Show Workflows Waiting " + WorkItemType.PeerReview.name() + "s",
            WorkItemType.PeerReview, false, true, AtsImage.PEER_REVIEW)));
      items.add(new NewPeerToPeerReviewItem(item));
      items.add(new GenerateReviewParticipationReport(item));
      if (AtsClientService.get().getUserService().isAtsAdmin()) {
         items.add(new XNavigateItemAction(item, new ValidatePeerDefectsAction(), AtsImage.PEER_REVIEW));
      }

   }

   @Override
   public void createCommonSection(List<XNavigateItem> items, List<String> excludeSectionIds) {
      try {
         XNavigateItem reviewItem = new XNavigateItem(null, "OSEE Review", AtsImage.REVIEW);
         new OpenPerspectiveNavigateItem(reviewItem, "Review", ReviewPerspective.ID, AtsImage.REVIEW);
         addOseePeerSectionChildren(reviewItem);
         items.add(reviewItem);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, "Can't create OSEE Review section");
      }
   }

   @Override
   public String getSectionId() {
      return "Peer";
   }
}
