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
package org.eclipse.osee.framework.ui.skynet.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.PresentationType;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.MenuCmdDef;
import org.eclipse.osee.framework.ui.skynet.render.compare.IComparator;
import org.eclipse.osee.framework.ui.skynet.render.word.AttributeElement;
import org.eclipse.osee.framework.ui.skynet.render.word.Producer;

/**
 * @author Jeff C. Phillips
 */
public interface IRenderer {

   public static final int SPECIALIZED_KEY_MATCH = 70;
   public static final int SPECIALIZED_MATCH = 60;
   public static final int PRESENTATION_SUBTYPE_MATCH = 50;
   public static final int PRESENTATION_TYPE = 40;
   public static final int SUBTYPE_TYPE_MATCH = 30;
   public static final int ARTIFACT_TYPE_MATCH = 20;
   public static final int GENERAL_MATCH = 10;
   public static final int BASE_MATCH = 5;
   public static final int NO_MATCH = -1;

   public static final String RESULT_PATH_RETURN = "resultPath";
   public static final String NO_DISPLAY = "noDisplay";
   public static final String SKIP_ERRORS = "skipErrors";
   public static final String SKIP_DIALOGS = "skipDialogs";
   public static final String OPEN_OPTION = "open.option";
   public static final String EXECUTE_VB_SCRIPT = "execute.vb.script";
   public static final String OVERRIDE_DATA_RIGHTS_OPTION = "overrideDataRights";

   public static enum DataRightsClassification {
      governmentPurposeRights("Government Purpose Rights"),
      limitedRights("Limited Rights"),
      proprietary("Proprietary"),
      restrictedRights("Restricted Rights"),
      Unspecified("Unspecified");

      String dataRightsClassification;

      DataRightsClassification(String dataRightsClassification) {
         this.dataRightsClassification = dataRightsClassification;
      }

      public String getDataRightsClassification() {
         return dataRightsClassification;
      }

      public static boolean isValid(String check) {
         for (DataRightsClassification classification : DataRightsClassification.values()) {
            if (classification.getDataRightsClassification().equals(check)) {
               return true;
            }
         }
         return false;
      }
   }

   public static enum CommandGroup {
      PREVIEW(PresentationType.PREVIEW),
      EDIT(PresentationType.SPECIALIZED_EDIT),
      SHOW(PresentationType.SPECIALIZED_EDIT);

      public static CommandGroup[] getReadOnly() {
         return new CommandGroup[] {PREVIEW, SHOW};
      }

      PresentationType presentationType;

      CommandGroup(PresentationType type) {
         this.presentationType = type;
      }

      public PresentationType getPresentationType() {
         return presentationType;
      }

      public boolean isEdit() {
         return CommandGroup.EDIT == this;
      }

      public boolean isPreview() {
         return CommandGroup.PREVIEW == this;
      }

      public boolean isShowIn() {
         return CommandGroup.SHOW == this;
      }
   }

   public void addMenuCommandDefinitions(ArrayList<MenuCmdDef> commands, Artifact artifact);

   public void renderAttribute(AttributeTypeToken attributeType, Artifact artifact, PresentationType presentationType, Producer producer, AttributeElement attributeElement, String footer) throws OseeCoreException;

   public String renderAttributeAsString(AttributeTypeId attributeType, Artifact artifact, PresentationType presentationType, String defaultValue) throws OseeCoreException;

   public int minimumRanking() throws OseeCoreException;

   public void open(List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException;

   public int getApplicabilityRating(PresentationType presentationType, Artifact artifact, Object... objects) throws OseeCoreException;

   public String getName();

   public void setOptions(Object... options) throws OseeArgumentException;

   public void setOption(String optionName, Object value);

   public String getStringOption(String key) throws OseeArgumentException;

   public Object getOption(String key) throws OseeArgumentException;

   public List<Artifact> getArtifactsOption(String key) throws OseeArgumentException;

   public List<IArtifactType> getArtifactTypesOption(String key) throws OseeArgumentException;

   public boolean getBooleanOption(String key) throws OseeArgumentException;

   public BranchId getBranchOption(String key) throws OseeArgumentException;

   public IRenderer newInstance() throws OseeCoreException;

   public boolean supportsCompare();

   public IComparator getComparator();

   public List<AttributeTypeToken> getOrderedAttributeTypes(Artifact artifact, Collection<? extends AttributeTypeToken> attributeTypes);
}
