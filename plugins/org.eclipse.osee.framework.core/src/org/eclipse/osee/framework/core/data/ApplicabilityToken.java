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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osee.framework.jdk.core.type.NamedIdBase;
import org.eclipse.osee.framework.jdk.core.type.NamedIdSerializer;

/**
 * @author Angel Avila
 */
@JsonSerialize(using = NamedIdSerializer.class)
public class ApplicabilityToken extends NamedIdBase implements ApplicabilityId {
   public static final ApplicabilityToken BASE = new ApplicabilityToken(ApplicabilityId.BASE.getId(), "Base");

   public ApplicabilityToken(long applId, String name) {
      super(applId, name);
   }

   public ApplicabilityToken(Long applId, String name) {
      super(applId, name);
   }

   public static @NonNull ApplicabilityToken valueOf(@JsonProperty("id") long id, @JsonProperty("name") String name) {
      return new ApplicabilityToken(id, name);
   }
}
