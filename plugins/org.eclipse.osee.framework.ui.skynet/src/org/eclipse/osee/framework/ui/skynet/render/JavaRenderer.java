/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.render;

import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.SPECIALIZED_EDIT;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FindInWorkspaceOperation;
import org.eclipse.osee.framework.ui.skynet.FindInWorkspaceOperation.FindInWorkspaceCollector;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.MenuCmdDef;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * @author John R. Misinco
 */
public class JavaRenderer extends FileSystemRenderer {

   @Override
   public String getName() {
      return "Java Editor";
   }

   @Override
   public DefaultArtifactRenderer newInstance() {
      return new JavaRenderer();
   }

   @Override
   public int getApplicabilityRating(PresentationType presentationType, IArtifact artifact) throws OseeCoreException {
      int toReturn = NO_MATCH;
      Artifact aArtifact = artifact.getFullArtifact();
      if (aArtifact.isOfType(CoreArtifactTypes.TestCase)) {
         if (presentationType.matches(PresentationType.SPECIALIZED_EDIT, PresentationType.DEFAULT_OPEN)) {
            toReturn = PRESENTATION_SUBTYPE_MATCH;
         }
      }
      return toReturn;
   }

   @Override
   public boolean supportsCompare() {
      return false;
   }

   @Override
   public void open(final List<Artifact> artifacts, PresentationType presentationType) {
      final List<Artifact> notMatched = new LinkedList<Artifact>();
      final StringBuffer findErrorMessage = new StringBuffer();

      FindInWorkspaceCollector collector = new FindInWorkspaceCollector() {

         @Override
         public void onResource(final IResource resource) {
            Displays.ensureInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  IFileSystem fs = EFS.getLocalFileSystem();

                  IPath fullPath = resource.getLocation();
                  final File fileToOpen = fullPath.toFile();
                  if (fileToOpen != null && fileToOpen.exists() && fileToOpen.isFile()) {
                     try {
                        IFileStore fileStore = fs.getStore(fileToOpen.toURI());
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        IDE.openEditorOnFileStore(page, fileStore);
                     } catch (PartInitException e) {
                        findErrorMessage.append(e.toString());
                     }
                  }
               }
            });
         }

         @Override
         public void onNotFound(Artifact artifact) {
            notMatched.add(artifact);
         }
      };
      IOperation op = new FindInWorkspaceOperation(artifacts, collector);
      Operations.executeAsJob(op, true, Job.LONG, new JobChangeAdapter() {
         @Override
         public void done(IJobChangeEvent event) {
            if (event.getResult().isOK()) {
               Displays.ensureInDisplayThread(new Runnable() {
                  @Override
                  public void run() {
                     StringBuilder builder = new StringBuilder();
                     builder.append(findErrorMessage);
                     if (!notMatched.isEmpty()) {
                        builder.append(String.format("Item(s) not found in the workspace: [%s]\n",
                           Collections.toString(",", notMatched)));
                     }

                     if (builder.length() > 0) {
                        Shell shell = AWorkbench.getActiveShell();
                        MessageDialog.openError(shell, getName(), builder.toString());
                     }
                  }
               });
            }
         }
      });
   }

   @Override
   public String getAssociatedExtension(Artifact artifact) {
      return "java";
   }

   @Override
   public Program getAssociatedProgram(Artifact artifact) throws OseeCoreException {
      Program program = Program.findProgram("java");
      if (program == null) {
         throw new OseeArgumentException("No program associated with the extension [%s] found on your local machine.",
            "java");
      }
      return program;
   }

   @Override
   public InputStream getRenderInputStream(PresentationType presentationType, List<Artifact> artifacts) throws OseeCoreException {
      final List<IResource> matches = new LinkedList<IResource>();
      final List<Artifact> notMatched = new LinkedList<Artifact>();

      FindInWorkspaceCollector collector = new FindInWorkspaceCollector() {

         @Override
         public void onResource(IResource resource) {
            matches.add(resource);
         }

         @Override
         public void onNotFound(Artifact artifact) {
            notMatched.add(artifact);
         }
      };
      IOperation op = new FindInWorkspaceOperation(artifacts.subList(0, 1), collector);
      Operations.executeWorkAndCheckStatus(op);
      for (IResource resource : matches) {
         IPath fullPath = resource.getLocation();
         File fileToOpen = fullPath.toFile();
         try {
            return new FileInputStream(fileToOpen);
         } catch (FileNotFoundException ex) {
            OseeExceptions.wrapAndThrow(ex);
         }
      }
      return null;
   }

   @Override
   protected IOperation getUpdateOperation(File file, List<Artifact> artifacts, IOseeBranch branch, PresentationType presentationType) throws OseeCoreException {
      throw new UnsupportedOperationException();
   }

   @Override
   public void addMenuCommandDefinitions(ArrayList<MenuCmdDef> commands, Artifact artifact) {
      commands.add(new MenuCmdDef(CommandGroup.EDIT, SPECIALIZED_EDIT, "Java Editor",
         FrameworkImage.JAVA_COMPILATION_UNIT));
   }
}
