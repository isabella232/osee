/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.script.dsl.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Roberto E. Escobar
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   OrcsScriptDslBindingTest.class,
   OrcsScriptDslFormatterTest.class,
   OrcsScriptDslParserTest.class,
   OrcsScriptDslValidatorTest.class,
   OsFieldResolverTest.class,
   TimestampConverterTest.class})
public class OrcsScriptDslTestSuite {
   // Test Suite
}
