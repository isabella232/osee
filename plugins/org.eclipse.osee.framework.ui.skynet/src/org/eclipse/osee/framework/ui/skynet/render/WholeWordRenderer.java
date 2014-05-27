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

package org.eclipse.osee.framework.ui.skynet.render;

import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.WholeWordContent;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERALIZED_EDIT;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERAL_REQUESTED;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.PRODUCE_ATTRIBUTE;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.io.Streams;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.WordWholeDocumentAttribute;
import org.eclipse.osee.framework.skynet.core.linking.LinkType;
import org.eclipse.osee.framework.skynet.core.linking.WordMlLinkHandler;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.skynet.core.word.WordUtil;
import org.eclipse.osee.framework.ui.skynet.render.compare.IComparator;
import org.eclipse.osee.framework.ui.skynet.render.compare.WholeWordCompare;

/**
 * @author Jeff C. Phillips
 */
public class WholeWordRenderer extends WordRenderer {

   private final IComparator comparator;

   public WholeWordRenderer() {
      this.comparator = new WholeWordCompare(this);
   }

   @Override
   public WholeWordRenderer newInstance() {
      return new WholeWordRenderer();
   }

   @Override
   public List<String> getCommandIds(CommandGroup commandGroup) {
      ArrayList<String> commandIds = new ArrayList<String>(1);

      if (commandGroup.isPreview()) {
         commandIds.add("org.eclipse.osee.framework.ui.skynet.wholewordpreview.command");
      }

      if (commandGroup.isEdit()) {
         commandIds.add("org.eclipse.osee.framework.ui.skynet.wholedocumenteditor.command");
      }

      return commandIds;
   }

   @Override
   public int getApplicabilityRating(PresentationType presentationType, IArtifact artifact) throws OseeCoreException {
      Artifact aArtifact = artifact.getFullArtifact();
      if (!presentationType.matches(GENERALIZED_EDIT, GENERAL_REQUESTED, PRODUCE_ATTRIBUTE) && aArtifact.isAttributeTypeValid(WholeWordContent)) {
         return PRESENTATION_SUBTYPE_MATCH;
      }
      return NO_MATCH;
   }

   @Override
   public InputStream getRenderInputStream(PresentationType presentationType, List<Artifact> artifacts) throws OseeCoreException {
      InputStream stream = null;
      try {
         if (artifacts.isEmpty()) {
            stream = Streams.convertStringToInputStream(WordWholeDocumentAttribute.getEmptyDocumentContent(), "UTF-8");
         } else {
            Artifact artifact = artifacts.iterator().next();
            String content = artifact.getOrInitializeSoleAttributeValue(CoreAttributeTypes.WholeWordContent);
            if (presentationType == PresentationType.DIFF && WordUtil.containsWordAnnotations(content)) {
               throw new OseeStateException(
                  "Trying to diff the [%s] artifact on the [%s] branch, which has tracked changes turned on.  All tracked changes must be removed before the artifacts can be compared.",
                  artifact.getName(), artifact.getBranch().getName());
            }

            LinkType linkType = LinkType.OSEE_SERVER_LINK;
            content = WordMlLinkHandler.link(linkType, artifact, content);
            stream = Streams.convertStringToInputStream(content, "UTF-8");
         }
      } catch (IOException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return stream;
   }

   @Override
   public IComparator getComparator() {
      return comparator;
   }

   @Override
   protected IOperation getUpdateOperation(File file, List<Artifact> artifacts, IOseeBranch branch, PresentationType presentationType) {
      return new FileToAttributeUpdateOperation(file, artifacts.get(0), CoreAttributeTypes.WholeWordContent);
   }

   @Override
   public void open(List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException {
      for (Artifact artifact : artifacts) {
         super.open(Arrays.asList(artifact), presentationType);
      }
   }
}