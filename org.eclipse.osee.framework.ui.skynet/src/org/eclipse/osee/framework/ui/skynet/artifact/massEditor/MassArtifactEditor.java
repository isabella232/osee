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
package org.eclipse.osee.framework.ui.skynet.artifact.massEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.access.AccessControlManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.ImageManager;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.AbstractArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.ats.IActionable;
import org.eclipse.osee.framework.ui.skynet.ats.OseeAts;
import org.eclipse.osee.framework.ui.skynet.widgets.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * @author Donald G. Dunne
 */
public class MassArtifactEditor extends AbstractArtifactEditor implements IDirtiableEditor, IActionable {
   public static final String EDITOR_ID = "org.eclipse.osee.framework.ui.skynet.massEditor.MassArtifactEditor";
   private int artifactsPageIndex;
   private MassXViewer xViewer;
   private Label branchLabel;
   private ToolBar toolBar;

   /**
    * @return the xViewer
    */
   public MassXViewer getXViewer() {
      return xViewer;
   }

   @Override
   public void doSave(IProgressMonitor monitor) {
      try {
         Artifacts.persistInTransaction(xViewer.getArtifacts());
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
      onDirtied();
   }

   public static void editArtifacts(final String name, final Collection<? extends Artifact> artifacts, TableLoadOption... tableLoadOptions) {
      Set<TableLoadOption> options = new HashSet<TableLoadOption>();
      options.addAll(Arrays.asList(tableLoadOptions));
      Displays.ensureInDisplayThread(new Runnable() {
         public void run() {
            boolean accessControlFilteredResults = false;
            try {
               Set<Artifact> accessibleArts = new HashSet<Artifact>();
               for (Artifact artifact : artifacts) {
                  if (!AccessControlManager.hasPermission(artifact, PermissionEnum.READ)) {
                     OseeLog.log(SkynetGuiPlugin.class, Level.INFO,
                           "The user " + UserManager.getUser() + " does not have read access to " + artifact);
                     accessControlFilteredResults = true;
                  } else
                     accessibleArts.add(artifact);
               }
               if (accessibleArts.size() == 0)
                  AWorkbench.popup("ERROR", "No Artifacts to edit");
               else

                  AWorkbench.getActivePage().openEditor(
                        new MassArtifactEditorInput(name, accessibleArts, new MassXViewerFactory(accessibleArts)),
                        EDITOR_ID);
               if (accessControlFilteredResults) AWorkbench.popup("ERROR",
                     "Some Artifacts not loaded due to access control limitations.");
            } catch (Exception ex) {
               OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      }, options.contains(TableLoadOption.ForcePend));
   }

   public static void editArtifact(final Artifact artifact, TableLoadOption... tableLoadOptions) {
      editArtifacts("", Arrays.asList(artifact));
   }

   public void createTaskActionBar(Composite parent) {

      // Button composite for state transitions, etc
      Composite bComp = new Composite(parent, SWT.NONE);
      // bComp.setBackground(mainSComp.getDisplay().getSystemColor(SWT.COLOR_CYAN));
      bComp.setLayout(ALayout.getZeroMarginLayout(2, false));
      bComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      Composite leftComp = new Composite(bComp, SWT.NONE);
      leftComp.setLayout(new GridLayout());
      leftComp.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL));

      branchLabel = new Label(leftComp, SWT.NONE);
      branchLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      Composite rightComp = new Composite(bComp, SWT.NONE);
      rightComp.setLayout(new GridLayout());
      rightComp.setLayoutData(new GridData(GridData.END));

      toolBar = new ToolBar(rightComp, SWT.FLAT | SWT.RIGHT);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      toolBar.setLayoutData(gd);
      ToolItem item = null;

      item = new ToolItem(toolBar, SWT.PUSH);
      item.setImage(ImageManager.getImage(FrameworkImage.REFRESH));
      item.setToolTipText("Refresh");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            xViewer.refresh();
         }
      });

      OseeAts.addButtonToEditorToolBar(this, SkynetGuiPlugin.getInstance(), toolBar, EDITOR_ID, "Mass Artifact Editor");
   }

   public static void editArtifacts(final MassArtifactEditorInput input) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            IWorkbenchPage page = AWorkbench.getActivePage();
            try {
               page.openEditor(input, EDITOR_ID);
            } catch (PartInitException ex) {
               OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });

   }

   @Override
   public boolean isSaveOnCloseNeeded() {
      return isDirty();
   }

   @Override
   public void dispose() {
      super.dispose();

      for (Artifact taskArt : xViewer.getArtifacts())
         try {
            if (taskArt != null && !taskArt.isDeleted() && taskArt.hasDirtyAttributes()) taskArt.reloadAttributesAndRelations();
         } catch (Exception ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
   }

   public ArrayList<Artifact> getLoadedArtifacts() {
      return xViewer.getLoadedArtifacts();
   }

   @Override
   public boolean isDirty() {
      for (Artifact taskArt : xViewer.getArtifacts()) {
         if (!taskArt.isDeleted() && taskArt.hasDirtyAttributes()) {
            return true;
         }
      }
      return false;
   }

   @Override
   public String toString() {
      return "MassArtifactEditor";
   }

   @Override
   protected void addPages() {
      IEditorInput editorInput = getEditorInput();
      if (!(editorInput instanceof MassArtifactEditorInput)) {
         throw new IllegalArgumentException("Editor Input not TaskEditorInput");
      }

      if (((MassArtifactEditorInput) editorInput).getName().equals(""))
         setPartName("Mass Artifact Editor");
      else
         setPartName(((MassArtifactEditorInput) editorInput).getName());

      SkynetGuiPlugin.getInstance().setHelp(getContainer(), "mass_artifact_editor",
            "org.eclipse.osee.framework.help.ui");

      Composite comp = new Composite(getContainer(), SWT.NONE);
      comp.setLayout(new GridLayout(1, true));
      comp.setLayoutData(new GridData(GridData.FILL_BOTH));

      createTaskActionBar(comp);

      xViewer = new MassXViewer(comp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION, this);
      xViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
      xViewer.setContentProvider(new org.eclipse.osee.framework.ui.skynet.artifact.massEditor.MassContentProvider(
            xViewer));
      xViewer.setLabelProvider(new org.eclipse.osee.framework.ui.skynet.artifact.massEditor.MassLabelProvider(xViewer));
      branchLabel.setText("Branch: " + (getBranch() == null ? "No Artifacts Returned" : getBranch().getShortName()));
      artifactsPageIndex = addPage(comp);
      setPageText(artifactsPageIndex, "Artifacts");

      new ActionContributionItem(xViewer.getCustomizeAction()).fill(toolBar, -1);

      Tree tree = xViewer.getTree();
      GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
      tree.setLayoutData(gridData);
      tree.setHeaderVisible(true);
      tree.setLinesVisible(true);

      setActivePage(artifactsPageIndex);
      try {
         xViewer.set(((MassArtifactEditorInput) editorInput).getArtifacts());
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public Branch getBranch() {
      if (((MassArtifactEditorInput) getEditorInput()).getArtifacts().size() == 0) return null;
      return ((MassArtifactEditorInput) getEditorInput()).getArtifacts().iterator().next().getBranch();
   }

   @Override
   public void onDirtied() {
      Displays.ensureInDisplayThread(new Runnable() {

         public void run() {
            firePropertyChange(PROP_DIRTY);
         }
      });
   }

   public String getCurrentStateName() {
      return "";
   }

   public IDirtiableEditor getEditor() {
      return this;
   }

   public boolean isArtifactsEditable() {
      return true;
   }

   /**
    * @return the artifacts
    */
   public Collection<? extends Artifact> getArtifacts() {
      return xViewer.getArtifacts();
   }

   public String getActionDescription() {
      return "";
   }
}
