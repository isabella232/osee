/*********************************************************************
 * Copyright (c) 2018 Boeing
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

package org.eclipse.osee.disposition.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.eclipse.osee.framework.core.util.JsonUtil;

/**
 * @author Dominic A. Guss
 */
public class DispoSummarySeverityDeserializer extends StdDeserializer<DispoSummarySeverity> {

   public DispoSummarySeverityDeserializer() {
      this(DispoSummarySeverity.class);
   }

   public DispoSummarySeverityDeserializer(Class<?> object) {
      super(object);
   }

   @Override
   public DispoSummarySeverity deserialize(JsonParser jp, DeserializationContext ctxt) {
      JsonNode readTree = JsonUtil.getJsonParserTree(jp);
      return DispoSummarySeverity.forVal(readTree.asText());
   }
}
