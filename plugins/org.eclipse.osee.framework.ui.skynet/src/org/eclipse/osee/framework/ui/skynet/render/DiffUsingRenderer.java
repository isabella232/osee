/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.render;

/**
 * @author Ryan D. Brooks
 */
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.DIFF;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.change.ArtifactDelta;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.render.compare.CompareDataCollector;
import org.eclipse.osee.framework.ui.skynet.render.compare.IComparator;

public final class DiffUsingRenderer extends AbstractOperation {

   private final Object[] options;
   private final Collection<ArtifactDelta> artifactDeltas;
   private final String pathPrefix;
   private final CompareDataCollector collector;
   private final IRenderer preferedRenderer;

   public DiffUsingRenderer(CompareDataCollector collector, Collection<ArtifactDelta> artifactDeltas, String pathPrefix, IRenderer preferedRenderer, Object... options) {
      super(generateOperationName(artifactDeltas), Activator.PLUGIN_ID);
      this.artifactDeltas = artifactDeltas;
      this.pathPrefix = pathPrefix;
      this.options = options;
      this.collector = collector;
      this.preferedRenderer = preferedRenderer;
   }

   public DiffUsingRenderer(CompareDataCollector collector, Collection<ArtifactDelta> artifactDeltas, String pathPrefix, Object... options) {
      this(collector, artifactDeltas, pathPrefix, null, options);
   }

   public DiffUsingRenderer(CompareDataCollector collector, ArtifactDelta artifactDelta, String diffPrefix, Object... options) {
      this(collector, Collections.singletonList(artifactDelta), diffPrefix, options);
   }

   private static String generateOperationName(Collection<ArtifactDelta> artifactDeltas) {
      ArtifactDelta firstDelta = artifactDeltas.iterator().next();
      if (artifactDeltas.size() == 1) {
         Artifact startVersion = firstDelta.getStartArtifact();
         Artifact endVersion = firstDelta.getEndArtifact();
         return String.format("Compare %s to %s", startVersion == null ? " new " : startVersion.getName(),
            endVersion == null ? " delete " : endVersion.getName());
      } else {
         return "Combined Diff";
      }
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {

      ArtifactDelta firstDelta = artifactDeltas.iterator().next();
      Artifact sampleArtifact =
         firstDelta.getStartArtifact() != null ? firstDelta.getStartArtifact() : firstDelta.getEndArtifact();

      IRenderer renderer = preferedRenderer;
      if (preferedRenderer == null) {
         renderer = RendererManager.getBestRenderer(DIFF, sampleArtifact, options);
      }

      IComparator comparator = renderer.getComparator();
      if (artifactDeltas.size() == 1) {
         comparator.compare(monitor, collector, DIFF, firstDelta, pathPrefix);
      } else {
         comparator.compareArtifacts(monitor, collector, DIFF, artifactDeltas, pathPrefix);
      }
   }
}
