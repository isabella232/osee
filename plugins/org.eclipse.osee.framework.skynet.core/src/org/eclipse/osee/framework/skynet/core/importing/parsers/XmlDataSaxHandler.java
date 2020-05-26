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

package org.eclipse.osee.framework.skynet.core.importing.parsers;

import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifact;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifactKind;
import org.eclipse.osee.framework.skynet.core.importing.operations.RoughArtifactCollector;
import org.xml.sax.Attributes;

/**
 * @author Ryan D. Brooks
 */
public class XmlDataSaxHandler extends AbstractSaxHandler {
   private int level = 0;
   private RoughArtifact roughArtifact;
   private final RoughArtifactCollector collector;
   private final ArtifactTypeToken primaryArtifactType;

   public XmlDataSaxHandler(RoughArtifactCollector collector, ArtifactTypeToken primaryArtifactType) {
      super();
      this.collector = collector;
      this.primaryArtifactType = primaryArtifactType;
   }

   @Override
   public void endElementFound(String uri, String localName, String name) {
      if (level == 3) {
         roughArtifact.addAttribute(localName, getContents());
      }
      level--;
   }

   @Override
   public void startElementFound(String uri, String localName, String name, Attributes attributes) {
      level++;

      if (level == 2) {
         roughArtifact = new RoughArtifact(RoughArtifactKind.PRIMARY);
         roughArtifact.setPrimaryArtifactType(primaryArtifactType);
         collector.addRoughArtifact(roughArtifact);
      }
   }
}