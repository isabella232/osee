/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.enums;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.TokenFactory;

/**
 * @author Ryan D. Brooks
 */
public final class CoreArtifactTypes {

   // @formatter:off
   public static final IArtifactType AccessControlModel = TokenFactory.createArtifactType(0x0000000000000002L, "Access Control Model");
   public static final IArtifactType AbstractSoftwareRequirement = TokenFactory.createArtifactType(0x0000000000000017L, "Abstract Software Requirement");
   public static final IArtifactType AbstractSystemRequirement = TokenFactory.createArtifactType(0x000000000000101EL, "Abstract System Requirement");
   public static final IArtifactType AbstractSubsystemRequirement = TokenFactory.createArtifactType(0x000000000001001EL, "Abstract Subsystem Requirement");
   public static final IArtifactType AbstractTestResult = TokenFactory.createArtifactType(0x0000000000000026L, "Abstract Test Result");
   public static final IArtifactType Artifact = TokenFactory.createArtifactType(0x0000000000000001L, "Artifact");
   public static final IArtifactType CodeUnit = TokenFactory.createArtifactType(0x000000000000003AL, "Code Unit");
   public static final IArtifactType Component = TokenFactory.createArtifactType(0x0000000000000039L, "Component");
   public static final IArtifactType Design = TokenFactory.createArtifactType(0x000000000000015AL, "Design");
   public static final IArtifactType DirectSoftwareRequirement = TokenFactory.createArtifactType(0x0000000000000016L, "Direct Software Requirement");
   public static final IArtifactType Folder = TokenFactory.createArtifactType(0x000000000000000BL, "Folder");
   public static final IArtifactType GeneralData = TokenFactory.createArtifactType(0x000000000000000CL, "General Data");
   public static final IArtifactType GeneralDocument = TokenFactory.createArtifactType(0x000000000000000EL, "General Document");
   public static final IArtifactType GlobalPreferences = TokenFactory.createArtifactType(0x0000000000000003L, "Global Preferences");
   public static final IArtifactType HardwareRequirement = TokenFactory.createArtifactType(0x0000000000000021L, "Hardware Requirement");
   public static final IArtifactType HeadingMSWord = TokenFactory.createArtifactType(0x0000000000000038L, "Heading - MS Word");
   public static final IArtifactType HeadingHTML = TokenFactory.createArtifactType(0x0000000000000324L, "Heading - HTML");
   public static final IArtifactType HTMLArtifact = TokenFactory.createArtifactType(0x000000000000031EL, "HTML Artifact");
   public static final IArtifactType ImageArtifact = TokenFactory.createArtifactType(0x0000000000000320L, "Image Artifact");
   public static final IArtifactType IndirectSoftwareRequirement = TokenFactory.createArtifactType(0x0000000000000019L, "Indirect Software Requirement");
   public static final IArtifactType InterfaceRequirement = TokenFactory.createArtifactType(0x0000000000000020L, "Interface Requirement");
   public static final IArtifactType ImplementationDetails = TokenFactory.createArtifactType(0x000000000000001AL, "Implementation Details");
   public static final IArtifactType OseeApp = TokenFactory.createArtifactType(0x0000000000000057L, "OseeApp");
   public static final IArtifactType OseeTypeDefinition = TokenFactory.createArtifactType(0x000000000000003CL, "Osee Type Definition");
   public static final IArtifactType RendererTemplate = TokenFactory.createArtifactType(0x0000000000000009L, "Renderer Template");
   public static final IArtifactType Requirement = TokenFactory.createArtifactType(0x0000000000000015L, "Requirement");
   public static final IArtifactType RootArtifact = TokenFactory.createArtifactType(0x000000000000000AL, "Root Artifact");
   public static final IArtifactType SoftwareDesign = TokenFactory.createArtifactType(0x000000000000002DL, "Software Design");
   public static final IArtifactType SoftwareRequirement = TokenFactory.createArtifactType(0x0000000000000018L, "Software Requirement");
   public static final IArtifactType SoftwareRequirementDataDefinition = TokenFactory.createArtifactType(0x0000000000000319L, "Software Requirement Data Definition");
   public static final IArtifactType SoftwareRequirementDrawing = TokenFactory.createArtifactType(0x000000000000001DL, "Software Requirement Drawing");
   public static final IArtifactType SoftwareRequirementFunction = TokenFactory.createArtifactType(0x000000000000001CL, "Software Requirement Function");
   public static final IArtifactType SoftwareRequirementPlainText = TokenFactory.createArtifactType(0x000000000000318L, "Software Requirement Plain Text");
   public static final IArtifactType SoftwareRequirementProcedure = TokenFactory.createArtifactType(0x000000000000001BL, "Software Requirement Procedure");
   public static final IArtifactType SubsystemDesign = TokenFactory.createArtifactType(0x000000000000002BL, "Subsystem Design");
   public static final IArtifactType SubsystemFunction = TokenFactory.createArtifactType(0x0000000000000024L, "Subsystem Function");
   public static final IArtifactType SubsystemRequirementMSWord = TokenFactory.createArtifactType(0x000000000000001FL, "Subsystem Requirement - MS Word");
   public static final IArtifactType SubsystemRequirementHTML = TokenFactory.createArtifactType(0x000000000000031BL, "Subsystem Requirement - HTML");
   public static final IArtifactType SupportingContent = TokenFactory.createArtifactType(0x0000000000000031L, "Supporting Content");
   public static final IArtifactType SupportDocument = TokenFactory.createArtifactType(0x000000000000000DL, "Support Document");;
   public static final IArtifactType SystemDesign = TokenFactory.createArtifactType(0x000000000000002CL, "System Design");
   public static final IArtifactType SystemFunction = TokenFactory.createArtifactType(0x0000000000000023L, "System Function");
   public static final IArtifactType SystemRequirementMSWord = TokenFactory.createArtifactType(0x000000000000001EL, "System Requirement - MS Word");
   public static final IArtifactType SystemRequirementHTML = TokenFactory.createArtifactType(0x000000000000031AL, "System Requirement - HTML");
   public static final IArtifactType TestCase = TokenFactory.createArtifactType(0x0000000000000052L, "Test Case");
   public static final IArtifactType TestInformationSheet = TokenFactory.createArtifactType(0x0000000000000029L, "Test Information Sheet");
   public static final IArtifactType TestPlanElement = TokenFactory.createArtifactType(0x0000000000000025L, "Test Plan Element");
   public static final IArtifactType TestProcedure = TokenFactory.createArtifactType(0x000000000000002EL, "Test Procedure");
   public static final IArtifactType TestProcedureNative = TokenFactory.createArtifactType(0x0000000000000030L, "Test Procedure Native");
   public static final IArtifactType TestProcedureWML = TokenFactory.createArtifactType(0x000000000000002FL, "Test Procedure WML");
   public static final IArtifactType TestResultNative = TokenFactory.createArtifactType(0x0000000000000027L, "Test Result Native");
   public static final IArtifactType TestResultWML = TokenFactory.createArtifactType(0x0000000000000028L, "Test Result WML");
   public static final IArtifactType TestRun = TokenFactory.createArtifactType(0x0000000000000055L, "Test Run");
   public static final IArtifactType TestRunDisposition = TokenFactory.createArtifactType(0x0000000000000054L, "Test Run Disposition");
   public static final IArtifactType TestSupport = TokenFactory.createArtifactType(0x0000000000000053L, "Test Support");
   public static final IArtifactType TestUnit = TokenFactory.createArtifactType(0x0000000000000004L, "Test Unit");
   public static final IArtifactType UniversalGroup = TokenFactory.createArtifactType(0x0000000000000008L, "Universal Group");
   public static final IArtifactType User = TokenFactory.createArtifactType(0x0000000000000005L, "User");
   public static final IArtifactType UserGroup = TokenFactory.createArtifactType(0x0000000000000007L, "User Group");
   public static final IArtifactType XViewerGlobalCustomization = TokenFactory.createArtifactType(0x0000000000000037L, "XViewer Global Customization");
   // @formatter:on

   private CoreArtifactTypes() {
      // Constants
   }
}