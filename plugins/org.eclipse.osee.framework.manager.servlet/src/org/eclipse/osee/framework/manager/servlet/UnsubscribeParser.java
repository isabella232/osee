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
package org.eclipse.osee.framework.manager.servlet;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.xml.Jaxp;
import org.eclipse.osee.framework.manager.servlet.ats.XmlUtil;
import org.w3c.dom.Element;

public final class UnsubscribeParser {
   private static final Matcher URI_PATTERN_MATCHER = Pattern.compile("group/(\\d+)?/user/(\\d+)").matcher("");

   private UnsubscribeParser() {
      // Utility class
   }

   public static UnsubscribeData createFromXML(HttpServletRequest request) throws IOException, Exception {
      Element rootElement = XmlUtil.readXML(request.getInputStream());
      String groupId = Jaxp.getChildText(rootElement, "groupId");
      String userId = Jaxp.getChildText(rootElement, "userId");
      Conditions.checkNotNullOrEmpty(groupId, "groupId");
      Conditions.checkNotNullOrEmpty(userId, "userId");
      return new UnsubscribeData(groupId, userId);
   }

   public static UnsubscribeData createFromURI(HttpServletRequest request) throws OseeCoreException {
      String uri = request.getRequestURI();
      String groupId = null;
      String userId = null;
      URI_PATTERN_MATCHER.reset(uri);
      if (URI_PATTERN_MATCHER.find()) {
         groupId = URI_PATTERN_MATCHER.group(1);
         userId = URI_PATTERN_MATCHER.group(2);
      }
      Conditions.checkNotNullOrEmpty(groupId, "groupId");
      Conditions.checkNotNullOrEmpty(userId, "userId");
      return new UnsubscribeData(groupId, userId);
   }
}