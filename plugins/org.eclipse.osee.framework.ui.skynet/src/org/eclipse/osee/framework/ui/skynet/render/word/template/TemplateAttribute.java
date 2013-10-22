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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.render.word.WordMLProducer;

/**
 * @author Andrew M. Finkbeiner
 */
public class TemplateAttribute implements ITemplateTask {

   private static Matcher trimTags = Pattern.compile("(<.*?>)").matcher("");

   private String name;
   private String label;
   private String editable;
   private String format;
   private String outline;
   private boolean paragraphWrap = true;

   public TemplateAttribute() {
      // do nothing
   }

   public TemplateAttribute(TemplateAttribute copyMe, String name) {
      this.editable = copyMe.editable;
      this.format = copyMe.format;
      this.label = copyMe.label;
      this.outline = copyMe.outline;
      this.name = name;
      this.paragraphWrap = copyMe.paragraphWrap;
   }

   public void addName(String name) {
      trimTags.reset(name);
      this.name = trimTags.replaceAll("");
   }

   public void addLabel(String label) {
      this.label = label;
   }

   public void addEditable(String editable) {
      this.editable = editable;
   }

   public void addFormat(String format) {
      this.format = format;
   }

   public void addNOutline(String outline) {
      this.outline = outline;
   }

   public String getName() {
      return this.name;
   }

   @Override
   public void process(WordMLProducer wordMl, Artifact artifact, List<ITemplateAttributeHandler> handlers) throws OseeCoreException {
      for (ITemplateAttributeHandler handler : handlers) {
         if (handler.canHandle(artifact, this)) {
            handler.process(wordMl, artifact, this);
            return;
         }
      }
      //      throw new Exception(String.format("There was not a valid handler for Artifact[%s] and TemplateAttribute[%s].",
      //            artifact.toString(), this.toString()));
      System.out.println(String.format("There was not a valid handler for Artifact[%s] and TemplateAttribute[%s].",
         artifact.toString(), this.toString()));
   }

   public String getLabel() {
      return this.label;
   }

   public String getFormat() {
      return this.format;
   }

   public boolean hasFormatting() {
      return this.format != null && this.format.length() > 0;
   }

   public boolean hasLabel() {
      return this.label != null && this.label.length() > 0;
   }

   @Override
   public boolean isTypeNameWildcard() {
      return getName().equals("*");
   }

   public void addParagraphWrap(String group) {
      trimTags.reset(group);
      String value = trimTags.replaceAll("");
      this.paragraphWrap = Boolean.parseBoolean(value);
   }

   public boolean isParagrapthWrap() {
      return this.paragraphWrap;
   }

}
