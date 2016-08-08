/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.internal.writer;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.enums.CoreArtifactTokens;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.rest.model.writer.OrcsWriterToken;
import org.eclipse.osee.orcs.rest.model.writer.config.OrcsWriterInputConfig;
import org.eclipse.osee.orcs.rest.model.writer.config.OrcsWriterRelationSide;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwArtifact;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwArtifactToken;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwArtifactType;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwAttributeType;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwBranch;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwCollector;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwRelation;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwRelationType;

/**
 * @author Donald G. Dunne
 */
public class OrcsWriterCollectorGenerator {
   private OrcsApi orcsApi;
   private final OrcsWriterInputConfig config;
   private OwCollector collector;

   public OrcsWriterCollectorGenerator() {
      this(null);
   }

   public OrcsWriterCollectorGenerator(OrcsWriterInputConfig config) {
      this.config = config;
   }

   private void init(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
      this.collector = new OwCollector();
   }

   public OwCollector run(OrcsApi providedOrcs) {
      init(providedOrcs);
      collector.setPersistComment("Put Comment Here");
      collector.setAsUserId(SystemUser.OseeSystem.getUserId());
      collector.getBranch().setName(COMMON.getName());
      collector.getBranch().setUuid(COMMON.getId());
      collector.getBranch().setData(String.format("[%s]-[%s]", COMMON.getName(), COMMON.getId()));
      createInstructions();
      createCreateSheet();
      createUpdateSheet();
      createDeleteSheet();
      createBranchSheet();
      createArtifactTokenSheet();
      createArtifactTypeSheet();
      createAttributeTypeSheet();
      createRelationTypeSheet();
      return collector;
   }

   private void createInstructions() {
      StringBuilder sb = new StringBuilder();
      sb.append("ORCS Writer provides Create, Update and Delete capabilities through JSON or an Excel spreadsheet\n");
      sb.append("   - Download an Example Excel Workbook (<server>/orcs/writer/sheet) or ");
      sb.append("Example JSON (<server>/orcs/writer/sheet.json)\n");
      sb.append(
         "   - Make modifications to the input. CREATE, MODIFY, DELETE, BRANCH tabs/structures are the only items\n");
      sb.append("     that will be read. Token and Type tabs/structures are for reference only and should be used\n");
      sb.append("     in the CREATE, MODIFY and DELETE tabs/structures.\n");
      sb.append("   - On BRANCH tab, delete all but the single branch to apply changes to.\n");
      sb.append("   - Use <server>/orcs/writer/ui/main.html to validate sheet/json and then apply changes to branch.");
      collector.setInstructions(sb.toString());
   }

   private void createCreateSheet() {
      OwArtifactToken folder = createFolder();
      createSoftwareRequirement(folder, "1");
      createSoftwareRequirement(folder, "2");
   }

   private void createSoftwareRequirement(OwArtifactToken folderToken, String number) {
      Long reqUuid = Lib.generateArtifactIdAsInt();
      String name = "Software Requirement " + number;
      OwArtifact softwareReq = OwFactory.createArtifact(CoreArtifactTypes.SoftwareRequirement, name, reqUuid);
      OwFactory.createAttribute(softwareReq, CoreAttributeTypes.StaticId, "static id field " + number);
      collector.getCreate().add(softwareReq);

      // add to new folder
      OwRelation relation = new OwRelation();
      relation.setType(OwFactory.createRelationType(orcsApi, CoreRelationTypes.Default_Hierarchical__Parent));
      relation.setArtToken(folderToken);
      softwareReq.getRelations().add(relation);
   }

   private OwArtifactToken createFolder() {
      Long folderUuid = Lib.generateArtifactIdAsInt();
      String folderName = "Orcs Writer Import Folder";
      OwArtifact folder = OwFactory.createArtifact(CoreArtifactTypes.Folder, folderName, folderUuid);
      collector.getCreate().add(folder);

      // add to default hierarchy root
      OwRelation relation = new OwRelation();
      relation.setType(OwFactory.createRelationType(orcsApi, CoreRelationTypes.Default_Hierarchical__Parent));
      relation.setArtToken(OwFactory.createArtifactToken(CoreArtifactTokens.DefaultHierarchyRoot));
      folder.getRelations().add(relation);

      return OwFactory.createArtifactToken(folderName, folderUuid);
   }

   private void createUpdateSheet() {
      OwArtifact userGroupArt = OwFactory.createArtifact(CoreArtifactTypes.Folder,
         CoreArtifactTokens.UserGroups.getName(), CoreArtifactTokens.UserGroups.getUuid());
      OwFactory.createAttribute(userGroupArt, CoreAttributeTypes.StaticId, "test static id");
      OwFactory.createAttribute(userGroupArt, CoreAttributeTypes.Annotation, "test annotation");
      collector.getUpdate().add(userGroupArt);
   }

   private void createDeleteSheet() {
      collector.getDelete();
   }

   private void createArtifactTokenSheet() {
      if (config == null) {
         OwArtifactToken owToken = OwFactory.createArtifactToken(CoreArtifactTokens.DefaultHierarchyRoot);
         collector.getArtTokens().add(owToken);
      } else {
         for (OrcsWriterToken token : config.getIncludeTokens()) {
            OwArtifactToken owToken = OwFactory.createArtifactToken(token.getName(), token.getUuid());
            collector.getArtTokens().add(owToken);
         }
      }
   }

   private void createArtifactTypeSheet() {
      Map<String, IArtifactType> types = new HashMap<>(100);
      if (config == null) {
         for (IArtifactType type : orcsApi.getOrcsTypes().getArtifactTypes().getAll()) {
            types.put(type.getName(), type);
         }
      } else {
         for (Long typeUuid : config.getIncludeArtifactTypes()) {
            IArtifactType type = orcsApi.getOrcsTypes().getArtifactTypes().get(typeUuid);
            if (type != null) {
               types.put(type.getName(), type);
            }
         }
      }
      List<String> typeNames = new ArrayList<>();
      typeNames.addAll(types.keySet());
      Collections.sort(typeNames);
      for (String typeName : typeNames) {
         IArtifactType type = types.get(typeName);
         OwArtifactType owType = OwFactory.createArtifactType(type);
         collector.getArtTypes().add(owType);
      }
   }

   private void createBranchSheet() {
      Map<String, IOseeBranch> branches = new HashMap<>(500);
      for (IOseeBranch branch : orcsApi.getQueryFactory().branchQuery().getResults()) {
         branches.put(branch.getName(), branch);
      }

      List<String> branchNames = new ArrayList<>();
      branchNames.addAll(branches.keySet());
      Collections.sort(branchNames);
      for (String branchName : branchNames) {
         IOseeBranch type = branches.get(branchName);
         OwBranch owBranch = OwFactory.createBranchToken(type);
         collector.getBranches().add(owBranch);
      }
   }

   private void createAttributeTypeSheet() {
      Map<String, IAttributeType> types = new HashMap<>(100);
      if (config == null) {
         for (IAttributeType type : orcsApi.getOrcsTypes().getAttributeTypes().getAll()) {
            types.put(type.getName(), type);
         }
      } else {
         for (Long typeUuid : config.getIncludeAttributeTypes()) {
            IAttributeType type = orcsApi.getOrcsTypes().getAttributeTypes().get(typeUuid);
            if (type != null) {
               types.put(type.getName(), type);
            }
         }

      }

      List<String> typeNames = new ArrayList<>();
      typeNames.addAll(types.keySet());
      Collections.sort(typeNames);
      for (String typeName : typeNames) {
         IAttributeType type = types.get(typeName);
         OwAttributeType owType = OwFactory.createAttributeType(type);
         collector.getAttrTypes().add(owType);
      }
   }

   private void createRelationTypeSheet() {
      Map<String, RelationTypeToken> types = new HashMap<>(100);
      if (config == null) {
         for (RelationTypeToken type : orcsApi.getOrcsTypes().getRelationTypes().getAll()) {
            types.put(type.getName(), type);
         }
      } else {
         for (OrcsWriterRelationSide token : config.getIncludeRelationTypes()) {
            Long relationTypeUuid = token.getRelationTypeUuid();
            RelationTypeToken type = orcsApi.getOrcsTypes().getRelationTypes().get(relationTypeUuid);
            if (type != null) {
               types.put(type.getName(), type);
            }
         }

      }
      List<String> typeNames = new ArrayList<>();
      typeNames.addAll(types.keySet());
      Collections.sort(typeNames);
      for (String typeName : typeNames) {
         RelationTypeToken type = types.get(typeName);
         writeRelationType(type);
      }
   }

   private void writeRelationType(RelationTypeToken type) {
      String sideAName = orcsApi.getOrcsTypes().getRelationTypes().getSideAName(type);
      OwRelationType owType = OwFactory.createRelationType(type, sideAName, true);
      collector.getRelTypes().add(owType);
      String sideBName = orcsApi.getOrcsTypes().getRelationTypes().getSideBName(type);
      owType = OwFactory.createRelationType(type, sideBName, false);
      collector.getRelTypes().add(owType);
   }

}
