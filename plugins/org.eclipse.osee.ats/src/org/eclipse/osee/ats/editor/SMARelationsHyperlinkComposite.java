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

package org.eclipse.osee.ats.editor;

import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.osee.ats.AtsOpenOption;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.review.AbstractReviewArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilClient;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.config.ActionableItems;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.AICheckTreeDialog;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * @author Donald G. Dunne
 */
public class SMARelationsHyperlinkComposite extends Composite {

   private static IRelationTypeSide[] sides = new IRelationTypeSide[] {
      AtsRelationTypes.TeamWorkflowToReview_Review,
      AtsRelationTypes.TeamWorkflowToReview_Team,
      CoreRelationTypes.Supercedes_Superceded,
      CoreRelationTypes.Supercedes_Supercedes,
      CoreRelationTypes.SupportingInfo_SupportedBy,
      CoreRelationTypes.SupportingInfo_SupportingInfo,
      CoreRelationTypes.Dependency__Artifact,
      CoreRelationTypes.Dependency__Dependency};
   private AbstractWorkflowArtifact awa;
   private Label actionableItemsLabel;
   private final SMAEditor editor;

   public SMARelationsHyperlinkComposite(Composite parent, int style, SMAEditor editor) {
      super(parent, style);
      this.editor = editor;
   }

   public void create(AbstractWorkflowArtifact sma) throws OseeCoreException {
      this.awa = sma;
      setLayout(ALayout.getZeroMarginLayout(2, false));
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.widthHint = 500;
      setLayoutData(gd);
      editor.getToolkit().adapt(this);

      // Create all hyperlinks from this artifact to others of interest
      createArtifactRelationHyperlinks("This", sma, "is reviewed by", AtsRelationTypes.TeamWorkflowToReview_Review);
      createArtifactRelationHyperlinks("This", sma, "reviews", AtsRelationTypes.TeamWorkflowToReview_Team);
      createArtifactRelationHyperlinks("This", sma, "is superceded by", CoreRelationTypes.Supercedes_Superceded);
      createArtifactRelationHyperlinks("This", sma, "supercedes", CoreRelationTypes.Supercedes_Supercedes);
      createArtifactRelationHyperlinks("This", sma, "depends on", CoreRelationTypes.Dependency__Dependency);
      createArtifactRelationHyperlinks("This", sma, "is dependency of", CoreRelationTypes.Dependency__Artifact);
      createArtifactRelationHyperlinks("This", sma, "is supported info for",
         CoreRelationTypes.SupportingInfo_SupportedBy);
      createArtifactRelationHyperlinks("This", sma, "has supporting info",
         CoreRelationTypes.SupportingInfo_SupportingInfo);

      // Create label for review's related actionable items (if any)
      if (sma instanceof AbstractReviewArtifact) {
         processReviewArtifact((AbstractReviewArtifact) sma);
      }

   }

   public static boolean relationExists(AbstractWorkflowArtifact smaArt) throws OseeCoreException {
      for (IRelationTypeSide side : sides) {
         if (smaArt.getRelatedArtifacts(side).size() > 0) {
            return true;
         }
      }
      if (smaArt instanceof AbstractReviewArtifact && ((AbstractReviewArtifact) smaArt).getActionableItemsDam().getActionableItemUuids().size() > 0) {
         return true;
      }
      return false;
   }

   private String getCompletedCancelledString(Artifact art) throws OseeCoreException {
      if (art instanceof AbstractWorkflowArtifact && ((AbstractWorkflowArtifact) art).isCompletedOrCancelled()) {
         return " " + ((AbstractWorkflowArtifact) art).getStateMgr().getCurrentStateName() + " ";
      }
      return "";
   }

   private void createArtifactRelationHyperlinks(String prefix, Artifact thisArt, String action, IRelationTypeSide relationEnum) throws OseeCoreException {
      for (final Artifact art : thisArt.getRelatedArtifacts(relationEnum)) {
         createLink(art, prefix, action, thisArt);
      }
   }

   private void createLink(final Artifact art, String prefix, String action, Artifact thisArt) {
      try {
         editor.getToolkit().createLabel(
            this,
            prefix + " \"" + thisArt.getArtifactTypeName() + "\" " + action + getCompletedCancelledString(art) + " \"" + art.getArtifactTypeName() + "\" ");
         Hyperlink link =
            editor.getToolkit().createHyperlink(
               this,
               String.format("\"%s\" - %s",
                  art.getName().length() < 60 ? art.getName() : art.getName().substring(0, 60),
                  AtsUtilClient.getAtsId(art)), SWT.NONE);
         link.addHyperlinkListener(new IHyperlinkListener() {

            @Override
            public void linkEntered(HyperlinkEvent e) {
               // do nothing
            }

            @Override
            public void linkExited(HyperlinkEvent e) {
               // do nothing
            }

            @Override
            public void linkActivated(HyperlinkEvent e) {
               if (AtsUtil.isAtsArtifact(art)) {
                  AtsUtil.openATSAction(art, AtsOpenOption.OpenOneOrPopupSelect);
               } else {
                  try {
                     RendererManager.open(art, PresentationType.DEFAULT_OPEN);
                  } catch (OseeCoreException ex) {
                     OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
                  }
               }
            }
         });
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void processReviewArtifact(final AbstractReviewArtifact reviewArt) throws OseeCoreException {
      if (reviewArt.getActionableItemsDam().getActionableItemUuids().isEmpty()) {
         return;
      }
      actionableItemsLabel = editor.getToolkit().createLabel(this, "");
      Hyperlink link = editor.getToolkit().createHyperlink(this, "(Edit)", SWT.NONE);
      link.addHyperlinkListener(new IHyperlinkListener() {

         @Override
         public void linkEntered(HyperlinkEvent e) {
            // do nothing
         }

         @Override
         public void linkExited(HyperlinkEvent e) {
            // do nothing
         }

         @Override
         public void linkActivated(HyperlinkEvent e) {
            editRelatedActionableItems(reviewArt);
         }
      });
      refreshActionableItemsLabel();
   }

   private void refreshActionableItemsLabel() throws OseeCoreException {
      if (actionableItemsLabel != null && awa instanceof AbstractReviewArtifact) {
         actionableItemsLabel.setText("This \"" + ((AbstractReviewArtifact) awa).getArtifactTypeName() +
         //
         "\" is review of Actionable Items  \"" +
         //
         ((AbstractReviewArtifact) awa).getActionableItemsDam().getActionableItemsStr() + "\" ");
      }
   }

   public void refresh() throws OseeCoreException {
      refreshActionableItemsLabel();
   }

   private void editRelatedActionableItems(final AbstractReviewArtifact reviewArt) {
      final AICheckTreeDialog diag =
         new AICheckTreeDialog("Edit Actionable Items", "Select Actionable Items for this review", Active.Active);
      try {
         Collection<IAtsActionableItem> actionableItems =
            ActionableItems.getUserEditableActionableItems(reviewArt.getActionableItemsDam().getActionableItems());
         reviewArt.getActionableItemsDam().getActionableItems();

         diag.setInitialSelections(actionableItems);
         if (diag.open() != 0) {
            return;
         }
         reviewArt.getActionableItemsDam().setActionableItems(diag.getChecked());
         editor.onDirtied();
         refreshActionableItemsLabel();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

   }

}
