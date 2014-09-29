/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.model.search.artifact;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.util.Collections;

/**
 * @author John R. Misinco
 * @author Roberto E. Escobar
 */
@XmlRootElement(name = "predicate")
public class Predicate {
   private SearchMethod type;
   private List<String> typeParameters;
   private List<String> values;
   private QueryOption[] options;

   public Predicate() {
   }

   public Predicate(SearchMethod type, List<String> typeParameters, List<String> values, QueryOption... options) {
      this.type = type;
      this.typeParameters = typeParameters;
      this.values = values;
      this.options = options;
   }

   public void setType(SearchMethod type) {
      this.type = type;
   }

   public void setTypeParameters(List<String> typeParameters) {
      this.typeParameters = typeParameters;
   }

   public void setValues(List<String> values) {
      this.values = values;
   }

   public SearchMethod getType() {
      return type;
   }

   public List<String> getTypeParameters() {
      return typeParameters;
   }

   public List<String> getValues() {
      return values;
   }

   public QueryOption[] getOptions() {
      return options;
   }

   public void setOptions(QueryOption[] options) {
      this.options = options;
   }

   @Override
   public String toString() {
      return String.format("type:[%s],typeParameters:[%s],values[%s],options[%s]", type,
         Collections.toString(",", typeParameters), Collections.toString(",", values),
         Collections.toString(",", options));
   }

}