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
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.PREVIEW;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.PRODUCE_ATTRIBUTE;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.SPECIALIZED_EDIT;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.define.report.api.DataRightInput;
import org.eclipse.osee.define.report.api.DataRightResult;
import org.eclipse.osee.define.report.api.PageOrientation;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.jdk.core.text.change.ChangeSet;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.io.Streams;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.WordWholeDocumentAttribute;
import org.eclipse.osee.framework.skynet.core.linking.LinkType;
import org.eclipse.osee.framework.skynet.core.linking.WordMlLinkHandler;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.skynet.core.word.WordUtil;
import org.eclipse.osee.framework.ui.skynet.MenuCmdDef;
import org.eclipse.osee.framework.ui.skynet.render.compare.IComparator;
import org.eclipse.osee.framework.ui.skynet.render.compare.WholeWordCompare;
import org.eclipse.osee.framework.ui.skynet.render.word.DataRightProviderImpl;
import org.eclipse.osee.framework.ui.skynet.render.word.WordRendererUtil;
import org.eclipse.osee.framework.ui.skynet.util.WordUiUtil;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Jeff C. Phillips
 */
public class WholeWordRenderer extends WordRenderer {

   private static final String FTR_END_TAG = "</w:ftr>";
   private static final String FTR_START_TAG = "<w:ftr[^>]*>";
   private static final Pattern START_PATTERN = Pattern.compile(FTR_START_TAG);
   private static final Pattern END_PATTERN = Pattern.compile(FTR_END_TAG);
   private final IComparator comparator;

   public WholeWordRenderer() {
      this.comparator = new WholeWordCompare(this);
   }

   @Override
   public WholeWordRenderer newInstance() {
      return new WholeWordRenderer();
   }

   @Override
   public void addMenuCommandDefinitions(ArrayList<MenuCmdDef> commands, Artifact artifact) {
      ImageDescriptor imageDescriptor = ImageManager.getProgramImageDescriptor("doc");
      commands.add(new MenuCmdDef(CommandGroup.EDIT, SPECIALIZED_EDIT, "MS Word Edit", imageDescriptor));
      commands.add(new MenuCmdDef(CommandGroup.PREVIEW, PREVIEW, "MS Word Preview", imageDescriptor));
   }

   @Override
   public int getApplicabilityRating(PresentationType presentationType, IArtifact artifact) throws OseeCoreException {
      Artifact aArtifact = artifact.getFullArtifact();
      if (!presentationType.matches(GENERALIZED_EDIT, GENERAL_REQUESTED,
         PRODUCE_ATTRIBUTE) && aArtifact.isAttributeTypeValid(WholeWordContent)) {
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

            Set<String> unknownGuids = new HashSet<>();
            LinkType linkType = LinkType.OSEE_SERVER_LINK;
            content = WordMlLinkHandler.link(linkType, artifact, content, unknownGuids);
            WordUiUtil.displayUnknownGuids(artifact, unknownGuids);

            String classification = WordRendererUtil.getDataRightsClassification(artifact);
            if (Strings.isValid(classification)) {
               content = addDataRights(content, classification, artifact);
            }

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

   private String addDataRights(String content, String classification, Artifact artifact) {
      String toReturn = content;
      PageOrientation orientation = WordRendererUtil.getPageOrientation(artifact);
      DataRightInput request = new DataRightInput();
      request.addData(artifact.getGuid(), classification, orientation, 0);

      DataRightProvider provider = new DataRightProviderImpl();
      DataRightResult dataRights = provider.getDataRights(request);
      String footer = dataRights.getContent(artifact.getGuid(), orientation);

      Matcher startFtr = START_PATTERN.matcher(footer);
      Matcher endFtr = END_PATTERN.matcher(footer);
      if (startFtr.find() && endFtr.find()) {
         ChangeSet ftrCs = new ChangeSet(footer);
         ftrCs.delete(0, startFtr.end());
         ftrCs.delete(endFtr.start(), footer.length());
         footer = ftrCs.applyChangesToSelf().toString();
      }

      startFtr.reset(content);
      endFtr.reset(content);
      ChangeSet cs = new ChangeSet(content);
      while (startFtr.find()) {
         if (endFtr.find()) {
            cs.replace(startFtr.end(), endFtr.start(), footer);
         }
      }
      toReturn = cs.applyChangesToSelf().toString();
      return toReturn;
   }
}