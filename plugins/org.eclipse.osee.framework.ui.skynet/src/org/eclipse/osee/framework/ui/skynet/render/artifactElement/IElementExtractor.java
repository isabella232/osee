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

package org.eclipse.osee.framework.ui.skynet.render.artifactElement;

import java.io.IOException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author Jeff C. Phillips
 */
public interface IElementExtractor {
   public abstract Element getOleDataElement();

   public abstract Collection<WordExtractorData> extractElements() throws DOMException, ParserConfigurationException, SAXException, IOException, OseeCoreException;
}