/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.ui.plugin.util;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.IContentProviderRunnable;
import org.eclipse.osee.framework.ui.swt.ITreeNode;
import org.eclipse.osee.framework.ui.swt.TreeNode;

/**
 * @author Robert A. Fisher
 */
public class JobbedNode extends TreeNode {
   private static final long serialVersionUID = 2285210369260889786L;
   private static final JobbedNode LOADING_NODE = new JobbedNode("Loading...");
   private static final JobbedNode CANCELLED_NODE = new JobbedNode("Cancelled");
   private static final Object[] LOADING = new Object[] {LOADING_NODE};
   private static final Object[] CANCELLED = new Object[] {CANCELLED_NODE};
   private final Viewer viewer;
   private final IContentProviderRunnable providerRunnable;
   private boolean expand = false;

   private JobbedNode(String title) {
      super(null, null, title);
      this.viewer = null;
      this.providerRunnable = null;
   }

   public JobbedNode(Object backingData, Viewer viewer, IContentProviderRunnable providerRunnable) {
      this(backingData, viewer, providerRunnable, null);
   }

   public JobbedNode(Object backingData, Viewer viewer, IContentProviderRunnable providerRunnable, JobbedNode parent) {
      super(parent, null, backingData);
      this.viewer = viewer;
      this.providerRunnable = providerRunnable;
   }

   @Override
   public Object[] getChildren() {
      if (children == null && providerRunnable != null) {
         children = LOADING;
         Jobs.startJob(new GetChildrenJob(null, viewer, this, providerRunnable));
      }
      return children;
   }

   public void cancelled() {
      cancelled(CANCELLED);
   }

   public void cancelled(Exception ex) {
      cancelled(new Object[] {ex});
   }

   private void cancelled(Object[] children) {
      this.children = children;
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            viewer.refresh();
         }
      });
   }

   @Override
   public void setChildren(Object[] objChildren) {
      super.setChildren(objChildren);

      if (expand) {
         if (viewer instanceof TreeViewer) {
            final TreeViewer treeViewer = (TreeViewer) viewer;

            Displays.ensureInDisplayThread(new Runnable() {

               @Override
               public void run() {
                  treeViewer.refresh();
                  treeViewer.expandToLevel(2);
               }
            });
         }
      }
   }

   public void refresh() {
      refresh(false);
   }

   public void refresh(boolean expand) {
      this.expand = expand;

      if (children != null) {
         for (Object obj : children) {
            if (obj instanceof JobbedNode) {
               ((JobbedNode) obj).refresh(true);
            }
         }
      }

      children = null;
   }

   @Override
   protected ITreeNode getChild(Object backingData) {
      return new JobbedNode(backingData, viewer, providerRunnable, this);
   }

}
