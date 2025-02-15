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
package org.eclipse.osee.framework.skynet.core.importing.parsers;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   DoorsColumnTypeTest.class,
   DoorsDataTypeTest.class,
   DoorsTableRowCollectorTest.class,
   DoorsTableRowTest.class,
   OutlineResolutionAndNumberTest.class,
   RoughArtifactMetaDataTest.class,
   WordMLExtractorDelegateTableOfContentsTest.class,
   WordMLExtractorDelegateNoNumberTest.class})
public class ParsersSuite {
   // do nothing
}
