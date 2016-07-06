/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.ds;

/**
 * @author Angel Avila
 */
import org.eclipse.osee.orcs.core.internal.tuple.TupleVisitor;

public interface TupleData extends OrcsData {

   Long getTupleType();

   Long getElement1();

   Long getElement2();

   Long getElement3();

   Long getElement4();

   void setElement1(Long e1);

   void setElement2(Long e2);

   void setElement3(Long e3);

   void setElement4(Long e4);

   void setTupleType(Long tupleType);

   void setRationale(String rationale);

   void accept(TupleVisitor visitor);
}
