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
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.render.word.WordMLProducer;

/**
 * @author Andrew M. Finkbeiner
 */
public class AddTemplateText implements ITemplateTask {

   private final String template;
   private final int begin;
   private final int end;

   public AddTemplateText(int begin, int end, String template) {
      this.template = template;
      this.end = end;
      this.begin = begin;
   }

   @Override
   public void process(WordMLProducer wordMl, Artifact artifact, List<ITemplateAttributeHandler> handlers) throws OseeCoreException {
      wordMl.addWordMl(template.subSequence(begin, end));
   }

   @Override
   public boolean isTypeNameWildcard() {
      return false;
   }

}
