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
package org.eclipse.osee.framework.core.model;

import org.eclipse.osee.framework.core.model.access.AccessTestSuite;
import org.eclipse.osee.framework.core.model.cache.CacheTestSuite;
import org.eclipse.osee.framework.core.model.change.ChangeTestSuite;
import org.eclipse.osee.framework.core.model.fields.FieldTestSuite;
import org.eclipse.osee.framework.core.model.type.TypeTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   AccessTestSuite.class,
   CacheTestSuite.class,
   ChangeTestSuite.class,
   FieldTestSuite.class,
   TypeTestSuite.class})
/**
 * @author Roberto E. Escobar
 */
public class AllCoreModelTestSuite {
   // Test Suite
}
