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
package org.eclipse.osee.framework.jdk.core.type;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Roberto E. Escobar
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   CompositeKeyHashMapTest.class,
   MatchLocationTest.class,
   PairTest.class,
   PropertyStoreTest.class,
   PropertyStoreWriterTest.class,
   QuadTest.class,
   ResultSetIterableTest.class,
   TripletTest.class})
public class JdkCoreTypeTestSuite {
   // Test Suite
}
