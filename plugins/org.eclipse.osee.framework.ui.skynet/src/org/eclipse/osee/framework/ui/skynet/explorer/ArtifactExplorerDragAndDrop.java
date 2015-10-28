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
package org.eclipse.osee.framework.ui.skynet.explorer;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.AccessPolicy;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactData;
import org.eclipse.osee.framework.skynet.core.importing.parsers.IArtifactExtractor;
import org.eclipse.osee.framework.skynet.core.importing.parsers.NativeDocumentExtractor;
import org.eclipse.osee.framework.skynet.core.importing.parsers.WholeWordDocumentExtractor;
import org.eclipse.osee.framework.skynet.core.importing.resolvers.IArtifactImportResolver;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.Wizards;
import org.eclipse.osee.framework.ui.skynet.ArtifactStructuredSelection;
import org.eclipse.osee.framework.ui.skynet.Import.ArtifactImportOperationFactory;
import org.eclipse.osee.framework.ui.skynet.Import.ArtifactImportOperationParameter;
import org.eclipse.osee.framework.ui.skynet.Import.ArtifactImportWizard;
import org.eclipse.osee.framework.ui.skynet.Import.ArtifactResolverFactory;
import org.eclipse.osee.framework.ui.skynet.Import.ArtifactResolverFactory.ArtifactCreationStrategy;
import org.eclipse.osee.framework.ui.skynet.artifact.ArtifactTransfer;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.internal.ServiceUtil;
import org.eclipse.osee.framework.ui.skynet.update.InterArtifactExplorerDropHandlerOperation;
import org.eclipse.osee.framework.ui.skynet.util.SkynetDragAndDrop;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.ui.IViewPart;

/**
 * @author Jeff C. Phillips
 */
public class ArtifactExplorerDragAndDrop extends SkynetDragAndDrop {
   private final TreeViewer treeViewer;
   private final String viewId;
   private final IViewPart viewPart;

   private IOseeBranch selectedBranch;

   public ArtifactExplorerDragAndDrop(TreeViewer treeViewer, String viewId, IViewPart viewPart, IOseeBranch selectedBranch) {
      this(treeViewer, viewId, viewPart);
      this.selectedBranch = selectedBranch;
   }

   public ArtifactExplorerDragAndDrop(TreeViewer treeViewer, String viewId, IViewPart viewPart) {
      super(treeViewer.getTree(), treeViewer.getTree(), viewId);

      this.treeViewer = treeViewer;
      this.viewId = viewId;
      this.viewPart = viewPart;
   }

   public void updateBranch(IOseeBranch branch) {
      selectedBranch = branch;
   }

   @Override
   public Artifact[] getArtifacts() {
      IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
      Object[] objects = selection.toArray();
      Artifact[] artifacts = new Artifact[objects.length];

      for (int index = 0; index < objects.length; index++) {
         artifacts[index] = (Artifact) objects[index];
      }

      return artifacts;
   }

   @Override
   public void performDragOver(DropTargetEvent event) {
      event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;

      if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
         event.detail = DND.DROP_COPY;
      } else if (isValidForArtifactDrop(event)) {
         event.detail = DND.DROP_MOVE;
      } else {
         event.detail = DND.DROP_NONE;
      }
   }

   private boolean isValidForArtifactDrop(DropTargetEvent event) {
      boolean valid = false;
      if (ArtifactTransfer.getInstance().isSupportedType(event.currentDataType)) {

         Artifact dropTarget = getSelectedArtifact(event);
         ArtifactData toBeDropped = ArtifactTransfer.getInstance().nativeToJava(event.currentDataType);
         if (dropTarget != null) {
            try {
               AccessPolicy policy = ServiceUtil.getAccessPolicy();
               Artifact[] artifactsBeingDropped = toBeDropped.getArtifacts();
               List<Artifact> artsOnSameBranchAsDestination = new LinkedList<>();
               IOseeBranch destinationBranch = dropTarget.getBranch();
               for (Artifact art : artifactsBeingDropped) {
                  if (art.isOnBranch(destinationBranch)) {
                     artsOnSameBranchAsDestination.add(art);
                  }
               }
               valid = policy.canRelationBeModified(dropTarget, artsOnSameBranchAsDestination,
                  CoreRelationTypes.Default_Hierarchical__Child, Level.FINE).matched();

               // if we are deparenting ourself, make sure our parent's child side can be modified
               if (valid) {
                  for (Artifact art : artsOnSameBranchAsDestination) {
                     if (art.hasParent()) {
                        valid = policy.canRelationBeModified(art.getParent(), null,
                           CoreRelationTypes.Default_Hierarchical__Child, Level.FINE).matched();
                     }
                     if (!valid) {
                        break;
                     }
                  }
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               valid = false;
            }
         }
      }
      return valid;
   }

   private Artifact getSelectedArtifact(DropTargetEvent event) {
      if (event.item != null && event.item.getData() instanceof Artifact) {
         return (Artifact) event.item.getData();
      }
      return null;
   }

   @Override
   public void performDrop(final DropTargetEvent event) {
      Artifact parentArtifact = getSelectedArtifact(event);

      if (parentArtifact == null && selectedBranch != null) {
         //try Default Root Hierarchy
         try {
            parentArtifact = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(selectedBranch);
         } catch (Exception ex) {
            OseeLog.log(getClass(), OseeLevel.SEVERE_POPUP, ex);
         }
      }

      if (parentArtifact != null) {
         if (ArtifactTransfer.getInstance().isSupportedType(event.currentDataType)) {
            ArtifactData artData = ArtifactTransfer.getInstance().nativeToJava(event.currentDataType);
            final Artifact[] artifactsToBeRelated = artData.getArtifacts();
            if (artifactsToBeRelated != null && artifactsToBeRelated.length > 0 && !artifactsToBeRelated[0].isOnSameBranch(parentArtifact)) {
               InterArtifactExplorerDropHandlerOperation interDropHandler =
                  new InterArtifactExplorerDropHandlerOperation(parentArtifact, artifactsToBeRelated, true);
               Operations.executeAsJob(interDropHandler, true);
            } else if (isValidForArtifactDrop(event) && MessageDialog.openQuestion(viewPart.getViewSite().getShell(),
               "Confirm Move",
               "Are you sure you want to make each of the selected artifacts a child of " + parentArtifact.getName() + "?")) {
               try {
                  SkynetTransaction transaction =
                     TransactionManager.createTransaction(parentArtifact.getBranch(), "Artifact explorer drag & drop");
                  // Replace all of the parent relations
                  for (Artifact artifact : artifactsToBeRelated) {
                     Artifact currentParent = artifact.getParent();
                     if (currentParent != null) {
                        currentParent.deleteRelation(CoreRelationTypes.Default_Hierarchical__Child, artifact);
                        currentParent.persist(transaction);
                     }
                     parentArtifact.addChild(RelationOrderBaseTypes.USER_DEFINED, artifact);
                     parentArtifact.persist(transaction);
                  }
                  transaction.execute();
               } catch (OseeCoreException ex) {
                  OseeLog.log(getClass(), OseeLevel.SEVERE_POPUP, ex);
               }
            }
         } else if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {

            Object object = FileTransfer.getInstance().nativeToJava(event.currentDataType);

            if (object instanceof String[]) {
               String[] items = (String[]) object;
               File importFile = new File(items[0]);

               ArtifactImportWizard wizard = new ArtifactImportWizard();
               wizard.setImportFile(importFile);
               wizard.setDestinationArtifact(parentArtifact);

               String fileName = importFile.getName();
               if (isSameName(parentArtifact, fileName)) {
                  String promptMsg = String.format(
                     "Artifact [%s] has same base file name as [%s]. \n\nDo you want to update the exisiting file? \nIf 'NO' selected, you'll be taken to the Artifact Import Wizard",
                     parentArtifact.getName(), FilenameUtils.getName(fileName));

                  if (MessageDialog.openQuestion(viewPart.getViewSite().getShell(), "Confirm Import", promptMsg)) {

                     IArtifactImportResolver resolver =
                        ArtifactResolverFactory.createResolver(ArtifactCreationStrategy.CREATE_ON_DIFFERENT_ATTRIBUTES,
                           parentArtifact.getArtifactType(), Arrays.asList(CoreAttributeTypes.Name), true, false);
                     try {
                        ArtifactImportOperationParameter parameter = new ArtifactImportOperationParameter();
                        parameter.setSourceFile(importFile);
                        parameter.setDestinationArtifact(parentArtifact.getParent());
                        parameter.setExtractor(getArtifactExtractor(parentArtifact.getArtifactType()));
                        parameter.setResolver(resolver);
                        parameter.setStopOnError(true);

                        IOperation operation = ArtifactImportOperationFactory.completeOperation(parameter);
                        Operations.executeWorkAndCheckStatus(operation);
                     } catch (OseeCoreException ex) {
                        OseeLog.log(getClass(), OseeLevel.SEVERE_POPUP, ex);
                     }

                  } else {
                     Wizards.initAndOpen(wizard, viewPart, new ArtifactStructuredSelection(parentArtifact));
                  }
               } else {
                  Wizards.initAndOpen(wizard, viewPart, new ArtifactStructuredSelection(parentArtifact));
               }
            }
         }
      }
   }

   private boolean isSameName(Artifact art, String fileName) {
      boolean isSame = false;
      if (!art.getArtifactType().equals(CoreArtifactTypes.Folder)) {
         if (art.getName().equals(FilenameUtils.getBaseName(fileName))) {
            isSame = true;
         }
      }
      return isSame;
   }

   private IArtifactExtractor getArtifactExtractor(ArtifactType type) {
      IArtifactExtractor extractor = null;
      if (type.inheritsFrom(CoreArtifactTypes.GeneralDocument)) {
         extractor = new NativeDocumentExtractor();
      } else {
         extractor = new WholeWordDocumentExtractor();
      }
      return extractor;
   }
}
