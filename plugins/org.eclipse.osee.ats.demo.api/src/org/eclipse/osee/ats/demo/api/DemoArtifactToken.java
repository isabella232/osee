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
package org.eclipse.osee.ats.demo.api;

import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;

/**
 * @author Donald G. Dunne
 */
public final class DemoArtifactToken {

   public static final IArtifactToken SAW_Test_AI =
      TokenFactory.createArtifactToken(75881049, "ABirZS0j81dpAiAyqUwA", "SAW Test", AtsArtifactTypes.ActionableItem);
   public static final IArtifactToken SAW_Code_AI =
      TokenFactory.createArtifactToken(733306468, "ADubGYmXm0FaNNuDCdAA", "SAW Code", AtsArtifactTypes.ActionableItem);

   public static IArtifactToken Process_Team = TokenFactory.createArtifactToken(55170736, "At2WHxBtYhx4Nxrck6gA",
      "Process_Team", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken Tools_Team =
      TokenFactory.createArtifactToken(4830548, "At2WHxCFyQPidx78iuAA", "Tools_Team", AtsArtifactTypes.TeamDefinition);

   public static final IArtifactToken SAW_Program =
      TokenFactory.createArtifactToken(19196003, "At2WHxCeMCHacr02EkAA", "SAW Program", AtsArtifactTypes.Program);
   public static IArtifactToken SAW_HW =
      TokenFactory.createArtifactToken(2876840, "At2WHxCeMCHfcr02EkAA", "SAW HW", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_Code =
      TokenFactory.createArtifactToken(30013695, "At2WHxC2lxLOGB0YiuQA", "SAW Code", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_Test =
      TokenFactory.createArtifactToken(31608252, "At2WHxDuXkCIJFEtQ0AA", "SAW Test", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_SW_Design = TokenFactory.createArtifactToken(138220, "At2WHxEGxl7nWuqx7FQA",
      "SAW SW Design", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_Requirements = TokenFactory.createArtifactToken(20592, "At2WHxEfLXfCLytmLlAA",
      "SAW Requirements", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_SW =
      TokenFactory.createArtifactToken(3902389, "At2WHxFk5VVE2cafF5AA", "SAW SW", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_CSCI_AI =
      TokenFactory.createArtifactToken(1866, "AZ2uRHybOnvKPIuRqKAA", "SAW CSCI", AtsArtifactTypes.ActionableItem);

   // SAW_SW Versions
   public static IArtifactToken SAW_Bld_1 =
      TokenFactory.createArtifactToken(2749182, "A8msa8LTDG36oWAnq3QA", "SAW_Bld_1", AtsArtifactTypes.Version);
   public static IArtifactToken SAW_Bld_2 =
      TokenFactory.createArtifactToken(7632957, "A8YqcqyKh3HCkcHfEVwA", "SAW_Bld_2", AtsArtifactTypes.Version);
   public static IArtifactToken SAW_Bld_3 =
      TokenFactory.createArtifactToken(577781, "A8msa8LrcxhyrUTsbuwA", "SAW_Bld_3", AtsArtifactTypes.Version);

   public static final IArtifactToken CIS_Program =
      TokenFactory.createArtifactToken(8242414, "At2WHxCeMCHbcr02EkAA", "CIS Program", AtsArtifactTypes.Program);

   public static IArtifactToken CIS_SW =
      TokenFactory.createArtifactToken(695910, "At2WHxF7jmUa8jXR3iwA", "CIS_SW", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken CIS_Code =
      TokenFactory.createArtifactToken(1629262, "At2WHxGo4A1nnGWYjgwA", "CIS_Code", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken CIS_Test =
      TokenFactory.createArtifactToken(541255, "At2WHxHZrl0bKPA6uUgA", "CIS_Test", AtsArtifactTypes.TeamDefinition);

   public static IArtifactToken Facilities_Team =
      TokenFactory.createArtifactToken(4811031, "At2WHxIMOz66yR56eRAA", "Facilities_Team", CoreArtifactTypes.Folder);

   public static IArtifactToken DemoPrograms =
      TokenFactory.createArtifactToken(90120, "Awsk_RtnczAchcuSxagA", "Demo Programs", CoreArtifactTypes.Artifact);

   public static IArtifactToken SAW_Code_Team_WorkPackage_01 =
      TokenFactory.createArtifactToken(38512616, "AZp8M1dPuESWYBPPbDgA", "Work Pkg 01", AtsArtifactTypes.WorkPackage);
   public static IArtifactToken SAW_Code_Team_WorkPackage_02 =
      TokenFactory.createArtifactToken(513994, "AZp8M1d7TCJiBw6A5bgA", "Work Pkg 02", AtsArtifactTypes.WorkPackage);
   public static IArtifactToken SAW_Code_Team_WorkPackage_03 =
      TokenFactory.createArtifactToken(304908, "AZp8M1em4EC1xE6bPEwA", "Work Pkg 03", AtsArtifactTypes.WorkPackage);

   public static IArtifactToken SAW_Test_AI_WorkPackage_0A =
      TokenFactory.createArtifactToken(75666, "AZp8M1fSc1JwMDQBtLwA", "Work Pkg 0A", AtsArtifactTypes.WorkPackage);
   public static IArtifactToken SAW_Test_AI_WorkPackage_0B =
      TokenFactory.createArtifactToken(281326, "AZp8M1hP81QOm6W9yNgA", "Work Pkg 0B", AtsArtifactTypes.WorkPackage);
   public static IArtifactToken SAW_Test_AI_WorkPackage_0C =
      TokenFactory.createArtifactToken(8141323, "AZp8M1kvEGrRt9tybTwA", "Work Pkg 0C", AtsArtifactTypes.WorkPackage);

   private DemoArtifactToken() {
      // Constants
   }
}
