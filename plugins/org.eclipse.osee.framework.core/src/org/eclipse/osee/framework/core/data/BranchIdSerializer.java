/*********************************************************************
 * Copyright (c) 2016 Boeing
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

package org.eclipse.osee.framework.core.data;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Ryan D. Brooks
 */
public class BranchIdSerializer extends StdSerializer<BranchId> {

   public BranchIdSerializer() {
      super(BranchId.class);
   }

   @Override
   public void serialize(BranchId branch, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      jgen.writeStartObject();
      jgen.writeStringField("id", branch.getIdString());
      jgen.writeStringField("viewId", branch.getViewId().getIdString());
      jgen.writeEndObject();
   }

   /**
    * Default implementation will write type prefix, call regular serialization method (since assumption is that value
    * itself does not need JSON Array or Object start/end markers), and then write type suffix. This should work for
    * most cases; some sub-classes may want to change this behavior.
    */
   @Override
   public void serializeWithType(BranchId branch, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
      typeSer.writeTypePrefixForScalar(branch, jgen);
      serialize(branch, jgen, provider);
      typeSer.writeTypeSuffixForScalar(branch, jgen);
   }

   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      return createSchemaNode("string", true);
   }
}
