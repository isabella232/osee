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

package org.eclipse.osee.framework.ui.skynet.render.word.template;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.word.WordUtil;
import org.eclipse.osee.framework.ui.skynet.render.word.WordMLProducer;

/**
 * @author Andrew M. Finkbeiner
 */
public class WordTemplateManager {

   //	private static final Matcher setNameMatcher =
   //         Pattern.compile("<(\\w+:)?Set_Name>(.*?)</(\\w+:)?Set_Name>", Pattern.DOTALL | Pattern.MULTILINE).matcher("");
   //    private static final Matcher headElementsMatcher =
   //         Pattern.compile("<((\\w+:)(Artifact|Extension_Processor))>(.*?)</\\1>",
   //               Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE).matcher("");
   //	
   //    private static final Matcher artifactMatcher = Pattern.compile(
   //          "<\\w+?:Artifact>(.*?)</\\w+?:Artifact>",
   //          Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE)
   //          .matcher("");
   //	
   //	private static final Matcher internalMatcher = Pattern.compile(
   //			"<\\w*?(Label|Outline|Name|Format|Editable)>(.*?)</\\1>",
   //			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE)
   //			.matcher("");
   //	private static final Matcher attributeMatcher = Pattern.compile(
   //			"<((\\w+:)?(Attribute))>(.*?)</\\3>",
   //			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE)
   //			.matcher("");

   private static final Matcher setNameMatcher = Pattern.compile("<(\\w+:)?Set_Name>(.*?)</(\\w+:)?Set_Name>",
      Pattern.DOTALL | Pattern.MULTILINE).matcher("");
   private static final Matcher headElementsMatcher = Pattern.compile(
      "<((\\w+:)?(Artifact|Extension_Processor))>(.*?)</\\1>",
      Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE).matcher("");
   private static final Matcher attributeElementsMatcher = Pattern.compile("<((\\w+:)?(Attribute))>(.*?)</\\3>",
      Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE).matcher("");
   private static final Matcher internalAttributeElementsMatcher = Pattern.compile(
      "<((\\w+:)?(Label|Outline|Name|Format|Editable|ParagraphWrap))>(.*?)</\\1>",
      Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE).matcher("");

   enum XmlAttributeType {
      Label,
      Outline,
      Name,
      Format,
      Editable,
      ParagraphWrap
   }

   private String template;
   private final String artifactSetName;
   private final List<ITemplateTask> tasks = new ArrayList<ITemplateTask>();
   private final List<ITemplateAttributeHandler> attributeHandlers;

   public WordTemplateManager(String template, List<ITemplateAttributeHandler> attributeHandlers) {
      //		this.template = sanatize(template);
      this.template = template;
      this.attributeHandlers = attributeHandlers;
      this.artifactSetName = getArtifactSetName();
      //		preProcessTemplatePositions();

   }

   private String getArtifactSetName() {
      String artifactSetName = "";
      headElementsMatcher.reset(template);

      if (headElementsMatcher.find()) {
         String elementType = headElementsMatcher.group(3);
         String elementValue = headElementsMatcher.group(4);

         if (elementType.equals("Artifact")) {
            setNameMatcher.reset(elementValue);
            setNameMatcher.find();
            artifactSetName = WordUtil.textOnly(setNameMatcher.group(2));
         }
      }
      return artifactSetName;
   }

   private void preProcessTemplateInsideArtifactTag(String text, List<ITemplateTask> innerTasks) {
      String newtext = text;//trimUnwantedText(text);
      attributeElementsMatcher.reset(newtext);
      int last = 0;
      while (attributeElementsMatcher.find()) {
         int start = attributeElementsMatcher.start();
         innerTasks.add(new AddTemplateText(last, start, newtext));
         int end = attributeElementsMatcher.end();
         last = end;
         TemplateAttribute processAttribute = new TemplateAttribute();
         innerTasks.add(processAttribute);
         String internal = attributeElementsMatcher.group(4);
         internalAttributeElementsMatcher.reset(internal);
         while (internalAttributeElementsMatcher.find()) {
            String type = internalAttributeElementsMatcher.group(3);
            switch (XmlAttributeType.valueOf(type)) {
               case Label:
                  processAttribute.addLabel(internalAttributeElementsMatcher.group(4));
                  break;
               case Editable:
                  processAttribute.addEditable(internalAttributeElementsMatcher.group(4));
                  break;
               case Format:
                  processAttribute.addFormat(internalAttributeElementsMatcher.group(4));
                  break;
               case Name:
                  processAttribute.addName(internalAttributeElementsMatcher.group(4));
                  break;
               case Outline:
                  processAttribute.addNOutline(internalAttributeElementsMatcher.group(4));
                  break;
               case ParagraphWrap:
                  processAttribute.addParagraphWrap(internalAttributeElementsMatcher.group(4));
                  break;
               default:
                  break;
            }
         }
      }
      innerTasks.add(new AddTemplateText(last, newtext.length(), newtext));
   }

   private void preProcessTemplatePositions() {
      headElementsMatcher.reset(template);
      int last = 0;
      while (headElementsMatcher.find()) {
         int start = headElementsMatcher.start();
         tasks.add(new AddTemplateText(last, start, template));
         int end = headElementsMatcher.end();
         last = end;
         List<ITemplateTask> innerTasks = new ArrayList<ITemplateTask>();
         String artifactSection = headElementsMatcher.group(4);
         String elementType = headElementsMatcher.group(3);

         ArtifactProcessing artifactProcessing = new ArtifactProcessing(innerTasks, artifactSection, elementType);
         tasks.add(artifactProcessing);
         preProcessTemplateInsideArtifactTag(artifactProcessing.getText(), innerTasks);

      }
      tasks.add(new AddTemplateText(last, template.length(), template));
   }

   public String getArtifactSet() {
      return this.artifactSetName;
   }

   public void processArtifacts(WordMLProducer wordMl, List<Artifact> artifacts) throws OseeCoreException {
      String outlineNumber = peekAtFirstArtifactToGetParagraphNumber(template, artifacts);
      template = wordMl.setHeadingNumbers(outlineNumber, template, null);
      preProcessTemplatePositions();

      for (ITemplateTask task : tasks) {
         if (task instanceof ArtifactProcessing) {
            ArtifactProcessing artifactProcessingTask = (ArtifactProcessing) task;

            if (artifactProcessingTask.isRecurseChildren()) {
               artifacts = recurseArtifactChildren(artifacts);
            }
            List<ITemplateTask> artifactAttributeTasks = ((ArtifactProcessing) task).getTasks();
            for (Artifact artifact : artifacts) {

               boolean performedOutLining = false;

               if (artifactProcessingTask.isOutlining()) {
                  performedOutLining = true;

                  String headingText =
                     artifact.getSoleAttributeValue(
                        AttributeTypeManager.getType(artifactProcessingTask.getHeadingAttributeName()), "");
                  wordMl.startOutlineSubSection("Times New Roman", headingText, null);
               }

               List<ITemplateTask> actualTasks = preProcessTemplateTasks(artifactAttributeTasks, artifact);
               for (ITemplateTask inner : actualTasks) {
                  inner.process(wordMl, artifact, attributeHandlers);
               }

               if (performedOutLining) {
                  wordMl.endOutlineSubSection();
               }

               wordMl.setPageLayout(artifact);
            }
         } else {
            task.process(wordMl, null, attributeHandlers);
         }
      }
   }

   private List<Artifact> recurseArtifactChildren(List<Artifact> artifacts) throws OseeCoreException {
      List<Artifact> arts = new ArrayList<Artifact>();
      for (Artifact art : artifacts) {
         recursiveChildResolver(art, arts);
      }
      return arts;
   }

   private void recursiveChildResolver(Artifact artifact, List<Artifact> arts) throws OseeCoreException {
      arts.add(artifact);
      for (Artifact child : artifact.getChildren()) {
         recursiveChildResolver(child, arts);
      }
   }

   /**
    * This method expands wildcard(*) attribute names into all of the attribute types of a particular artifact.
    */
   private List<ITemplateTask> preProcessTemplateTasks(List<ITemplateTask> tasks, Artifact artifact) throws OseeCoreException {
      List<ITemplateTask> newTasks = new ArrayList<ITemplateTask>();
      for (ITemplateTask task : tasks) {
         if (task instanceof TemplateAttribute && ((TemplateAttribute) task).isTypeNameWildcard()) {
            TemplateAttribute attributeTask = (TemplateAttribute) task;
            for (IAttributeType attributeType : artifact.getAttributeTypes()) {
               newTasks.add(new TemplateAttribute(attributeTask, attributeType.getName()));
            }
         } else {
            newTasks.add(task);
         }
      }
      return newTasks;
   }

   private String peekAtFirstArtifactToGetParagraphNumber(String template, List<Artifact> artifacts) throws OseeCoreException {
      Pattern headElementsPattern =
         Pattern.compile("<((\\w+:)?(Artifact|Extension_Processor))>(.*?)</\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
      String startParagraphNumber = "1";
      Matcher matcher = headElementsPattern.matcher(template);

      if (matcher.find()) {
         String elementType = matcher.group(3);
         String elementValue = matcher.group(4);

         if (elementType.equals("Artifact")) {
            //            Matcher setNameMatcher = setNamePattern.matcher(elementValue);
            setNameMatcher.reset(elementValue);
            setNameMatcher.find();

            if (!artifacts.isEmpty()) {
               Artifact artifact = artifacts.iterator().next();
               if (artifact.isAttributeTypeValid(CoreAttributeTypes.ParagraphNumber)) {
                  if (!artifact.getSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "").equals("")) {
                     startParagraphNumber = artifact.getSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "");
                  }
               }
            }
         }
      }
      return startParagraphNumber;
   }

}
