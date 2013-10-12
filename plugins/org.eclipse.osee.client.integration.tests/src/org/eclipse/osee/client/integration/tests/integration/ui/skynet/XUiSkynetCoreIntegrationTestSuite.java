/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http:www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.client.integration.tests.integration.ui.skynet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ArtifactImportWizardTest.class,
   ArtifactPasteOperationTest.class,
   AttributeTypeEditPresenterTest.class,
   BlamXWidgetTest.class,
   EmailGroupsBlamTest.class,
   InterArtifactDropTest.class,
   PlainTextEditTest.class,
   PreviewAndMultiPreviewTest.class,
   RelationIntegrityCheckTest.class,
   ReplaceWithBaselineTest.class,
   StringGuidsToArtifactListOperationTest.class,
   TemplateArtifactValidatorTest.class,
   ViewWordChangeAndDiffTest.class,
   WordArtifactElementExtractorTest.class,
   WordEditTest.class,
   WordOutlineAndStyleTest.class,
   WordTemplateProcessorTest.class,
   WordTemplateRendererTest.class,
   WordTrackedChangesTest.class,
   FrameworkImageTest.class,
   OseeEmailTest.class})
public class XUiSkynetCoreIntegrationTestSuite {
   // Test Suite
}
