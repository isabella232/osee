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
package org.eclipse.osee.orcs.db.internal.exchange.transform;

import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.eclipse.osee.framework.jdk.core.util.io.xml.SaxTransformer;
import org.xml.sax.Attributes;

/**
 * @author Roberto E. Escobar
 * @author Ryan D. Brooks
 */
public class V0_9_2ConflictTransformer extends SaxTransformer {
   private final Map<Long, Long> artifactGammaToNetGammaId;

   public V0_9_2ConflictTransformer(Map<Long, Long> artifactGammaToNetGammaId) {
      this.artifactGammaToNetGammaId = artifactGammaToNetGammaId;
   }

   @Override
   public void startElementFound(String uri, String localName, String qName, Attributes attributes) throws XMLStreamException {
      writer.writeStartElement(localName);
      for (int i = 0; i < attributes.getLength(); i++) {
         String value = null;
         if (attributes.getLocalName(i).equals("source_gamma_id") || attributes.getLocalName(i).equals(
            "dest_gamma_id")) {
            Long netGammaId = artifactGammaToNetGammaId.get(Long.valueOf(attributes.getValue(i)));
            if (netGammaId != null) {
               value = String.valueOf(netGammaId);
            }
         }
         if (value == null) {
            value = attributes.getValue(i);
         }
         writer.writeAttribute(attributes.getLocalName(i), value);
      }
   }
}