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
package org.eclipse.osee.orcs.core.internal.types;

import static org.eclipse.osee.framework.core.enums.RelationSide.SIDE_A;
import static org.eclipse.osee.framework.core.enums.RelationSide.SIDE_B;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.Identity;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.resource.management.IResource;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.OrcsTypesDataStore;
import org.eclipse.osee.orcs.core.internal.SessionContext;
import org.eclipse.osee.orcs.core.internal.types.impl.OrcsTypesImpl;
import org.eclipse.osee.orcs.data.ArtifactTypes;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.data.EnumEntry;
import org.eclipse.osee.orcs.data.EnumType;
import org.eclipse.osee.orcs.data.RelationTypes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.Callables;

/**
 * Test Case for {@link OrcsTypesImpl}
 * 
 * @author Roberto E. Escobar
 */
public class OrcsTypesTest {

   //@formatter:off
   private static final String SESSION_ID = "Test Session";
   
   private static final String TEST_TYPE_MODEL = "testTypeModel.osee";
   
   private static final IArtifactType ARTIFACT = TokenFactory.createArtifactType(0x0000000000000001L, "Artifact");
   private static final IArtifactType REQUIREMENT = TokenFactory.createArtifactType(0x0000000000000015L, "Requirement");
   private static final IArtifactType SOFTWARE_REQUIREMENT = TokenFactory.createArtifactType(0x0000000000000018L, "Software Requirement");
   private static final IArtifactType SYSTEM_REQUIREMENT = TokenFactory.createArtifactType(0x000000000000001EL, "System Requirement");
   private static final IArtifactType SUBSYSTEM_REQUIREMENT = TokenFactory.createArtifactType(0x000000000000001DL, "SubSystem Requirement");
   private static final IArtifactType OTHER_ARTIFACT = TokenFactory.createArtifactType(0x0000000000000020L, "Other Artifact");
   private static final IArtifactType LAST_ARTIFACT = TokenFactory.createArtifactType(0x0000000000000021L, "Last Artifact");
   
   private static final IAttributeType NAME = TokenFactory.createAttributeType(0x1000000000000070L, "Name");
   private static final IAttributeType ANNOTATION = TokenFactory.createAttributeType(0x1000000000000076L, "Annotation");
   private static final IAttributeType WORDML = TokenFactory.createAttributeType(0x100000000000007AL, "WordML");
   private static final IAttributeType FIELD_1 = TokenFactory.createAttributeType(0x1000000000000080L, "Field 1");
   private static final IAttributeType FIELD_2 = TokenFactory.createAttributeType(0x1000000000000081L, "Field 2");
   
   private static final IRelationType REQUIREMENT_REL = TokenFactory.createRelationType(0x2000000000000157L, "Requirement Relation");
   private static final IRelationType ANOTHER_REL = TokenFactory.createRelationType(0x2000000000000158L, "Another Relation");

   private static final IOseeBranch BRANCH_A = TokenFactory.createBranch("AU2FErW1QwSXWPiP4cwA", "Branch A");
   private static final IOseeBranch BRANCH_B = TokenFactory.createBranch("AU2JKsKQAQAvzkTkk+gA", "Branch B");
   private static final IOseeBranch BRANCH_C = TokenFactory.createBranch(GUID.create(), "Branch C");
   private static final IOseeBranch BRANCH_D = TokenFactory.createBranch(GUID.create(), "Branch D");
   private static final IOseeBranch BRANCH_E = TokenFactory.createBranch(GUID.create(), "Branch E");
   
   @Mock private Log logger;
   @Mock private OrcsTypesDataStore dataStore;
   @Mock private BranchHierarchyProvider hierarchyProvider;
   @Mock private SessionContext session;
   //@formatter:on

   private OrcsTypes orcsTypes;
   private List<InputSupplier<? extends InputStream>> resources;
   private Multimap<IOseeBranch, IOseeBranch> branchHierachies;
   private OrcsTypesModule module;

   @Before
   public void setUp() throws Exception {
      MockitoAnnotations.initMocks(this);

      module = new OrcsTypesModule(logger, dataStore, hierarchyProvider);
      module.start(session);

      orcsTypes = module.createOrcsTypes(session);
      resources = new ArrayList<InputSupplier<? extends InputStream>>();

      URI uri = new URI("osee:/types.test.data.osee");

      IResource resource = new MultiResource(uri, resources);
      when(dataStore.getOrcsTypesLoader(SESSION_ID)).thenReturn(Callables.returning(resource));

      when(session.getSessionId()).thenReturn(SESSION_ID);

      resources.add(getResource(TEST_TYPE_MODEL));

      branchHierachies = ArrayListMultimap.create();

      // Field 1 will only be visible on branch A and its descendants
      // Field 2 will only be visible on branch B and its descendants
      branchHierachies.put(CoreBranches.SYSTEM_ROOT, CoreBranches.SYSTEM_ROOT);
      branchHierachies.putAll(BRANCH_A, Arrays.asList(BRANCH_A, CoreBranches.SYSTEM_ROOT));
      branchHierachies.putAll(BRANCH_B, Arrays.asList(BRANCH_B, CoreBranches.SYSTEM_ROOT));
      branchHierachies.putAll(BRANCH_C, Arrays.asList(BRANCH_C, CoreBranches.SYSTEM_ROOT));
      branchHierachies.putAll(BRANCH_D, Arrays.asList(BRANCH_D, BRANCH_A, CoreBranches.SYSTEM_ROOT));
      branchHierachies.putAll(BRANCH_E, Arrays.asList(BRANCH_E, BRANCH_B, CoreBranches.SYSTEM_ROOT));

      when(hierarchyProvider.getParentHierarchy(any(IOseeBranch.class))).thenAnswer(
         new Answer<Iterable<? extends IOseeBranch>>() {

            @Override
            public Iterable<? extends IOseeBranch> answer(InvocationOnMock invocation) throws Throwable {
               IOseeBranch branchToGet = (IOseeBranch) invocation.getArguments()[0];
               return branchHierachies.get(branchToGet);
            }
         });

   }

   @After
   public void tearDown() {
      if (module != null) {
         module.stop();
      }
   }

   @Test
   public void testGetAllArtifactTypes() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertEquals(7, artTypes.size());
      assertEquals(false, artTypes.isEmpty());

      //@formatter:off
      assertContains(artTypes.getAll(), ARTIFACT, REQUIREMENT, SOFTWARE_REQUIREMENT, SYSTEM_REQUIREMENT, SUBSYSTEM_REQUIREMENT, OTHER_ARTIFACT, LAST_ARTIFACT);
      //@formatter:on
   }

   @Test
   public void testGetArtifactTypesByUuid() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertEquals(ARTIFACT, artTypes.getByUuid(ARTIFACT.getGuid()));
      assertEquals(REQUIREMENT, artTypes.getByUuid(REQUIREMENT.getGuid()));
      assertEquals(SOFTWARE_REQUIREMENT, artTypes.getByUuid(SOFTWARE_REQUIREMENT.getGuid()));
      assertEquals(SYSTEM_REQUIREMENT, artTypes.getByUuid(SYSTEM_REQUIREMENT.getGuid()));
      assertEquals(SUBSYSTEM_REQUIREMENT, artTypes.getByUuid(SUBSYSTEM_REQUIREMENT.getGuid()));
      assertEquals(OTHER_ARTIFACT, artTypes.getByUuid(OTHER_ARTIFACT.getGuid()));
      assertEquals(LAST_ARTIFACT, artTypes.getByUuid(LAST_ARTIFACT.getGuid()));
   }

   @Test
   public void testExistsArtifactType() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertEquals(true, artTypes.exists(ARTIFACT));
      assertEquals(true, artTypes.exists(REQUIREMENT));
      assertEquals(true, artTypes.exists(SOFTWARE_REQUIREMENT));
      assertEquals(true, artTypes.exists(SYSTEM_REQUIREMENT));
      assertEquals(true, artTypes.exists(SUBSYSTEM_REQUIREMENT));
      assertEquals(true, artTypes.exists(OTHER_ARTIFACT));
      assertEquals(true, artTypes.exists(LAST_ARTIFACT));
   }

   @Test
   public void testIsAbstract() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertEquals(false, artTypes.isAbstract(ARTIFACT));
      assertEquals(true, artTypes.isAbstract(REQUIREMENT));
      assertEquals(false, artTypes.isAbstract(SOFTWARE_REQUIREMENT));
      assertEquals(false, artTypes.isAbstract(SYSTEM_REQUIREMENT));
      assertEquals(false, artTypes.isAbstract(SUBSYSTEM_REQUIREMENT));
      assertEquals(false, artTypes.isAbstract(OTHER_ARTIFACT));
      assertEquals(true, artTypes.isAbstract(LAST_ARTIFACT));
   }

   @Test
   public void testHasSuperArtifactTypes() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertEquals(false, artTypes.hasSuperArtifactTypes(ARTIFACT));
      assertEquals(true, artTypes.hasSuperArtifactTypes(REQUIREMENT));
      assertEquals(true, artTypes.hasSuperArtifactTypes(SOFTWARE_REQUIREMENT));
      assertEquals(true, artTypes.hasSuperArtifactTypes(SYSTEM_REQUIREMENT));
      assertEquals(true, artTypes.hasSuperArtifactTypes(SUBSYSTEM_REQUIREMENT));
      assertEquals(true, artTypes.hasSuperArtifactTypes(OTHER_ARTIFACT));
      assertEquals(true, artTypes.hasSuperArtifactTypes(LAST_ARTIFACT));
   }

   @Test
   public void testGetSuperTypes() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertEquals(true, artTypes.getSuperArtifactTypes(ARTIFACT).isEmpty());

      assertContains(artTypes.getSuperArtifactTypes(REQUIREMENT), ARTIFACT);
      assertContains(artTypes.getSuperArtifactTypes(SOFTWARE_REQUIREMENT), REQUIREMENT);
      assertContains(artTypes.getSuperArtifactTypes(SYSTEM_REQUIREMENT), REQUIREMENT);
      assertContains(artTypes.getSuperArtifactTypes(SUBSYSTEM_REQUIREMENT), REQUIREMENT, OTHER_ARTIFACT);
      assertContains(artTypes.getSuperArtifactTypes(OTHER_ARTIFACT), ARTIFACT);
      assertContains(artTypes.getSuperArtifactTypes(LAST_ARTIFACT), SUBSYSTEM_REQUIREMENT);
   }

   @Test
   public void testInheritsFrom() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertEquals(false, artTypes.inheritsFrom(ARTIFACT, REQUIREMENT));
      assertEquals(true, artTypes.inheritsFrom(REQUIREMENT, ARTIFACT));

      assertEquals(false, artTypes.inheritsFrom(ARTIFACT, OTHER_ARTIFACT));
      assertEquals(false, artTypes.inheritsFrom(OTHER_ARTIFACT, REQUIREMENT));
      assertEquals(true, artTypes.inheritsFrom(OTHER_ARTIFACT, ARTIFACT));

      assertEquals(false, artTypes.inheritsFrom(ARTIFACT, SOFTWARE_REQUIREMENT));
      assertEquals(false, artTypes.inheritsFrom(REQUIREMENT, SOFTWARE_REQUIREMENT));
      assertEquals(true, artTypes.inheritsFrom(SOFTWARE_REQUIREMENT, ARTIFACT));
      assertEquals(true, artTypes.inheritsFrom(SOFTWARE_REQUIREMENT, REQUIREMENT));
      assertEquals(false, artTypes.inheritsFrom(SOFTWARE_REQUIREMENT, OTHER_ARTIFACT));

      assertEquals(false, artTypes.inheritsFrom(ARTIFACT, SYSTEM_REQUIREMENT));
      assertEquals(false, artTypes.inheritsFrom(REQUIREMENT, SYSTEM_REQUIREMENT));
      assertEquals(true, artTypes.inheritsFrom(SYSTEM_REQUIREMENT, ARTIFACT));
      assertEquals(true, artTypes.inheritsFrom(SYSTEM_REQUIREMENT, REQUIREMENT));
      assertEquals(false, artTypes.inheritsFrom(SYSTEM_REQUIREMENT, OTHER_ARTIFACT));

      assertEquals(false, artTypes.inheritsFrom(ARTIFACT, SUBSYSTEM_REQUIREMENT));
      assertEquals(false, artTypes.inheritsFrom(REQUIREMENT, SUBSYSTEM_REQUIREMENT));
      assertEquals(true, artTypes.inheritsFrom(SUBSYSTEM_REQUIREMENT, ARTIFACT));
      assertEquals(true, artTypes.inheritsFrom(SUBSYSTEM_REQUIREMENT, REQUIREMENT));
      assertEquals(true, artTypes.inheritsFrom(SUBSYSTEM_REQUIREMENT, OTHER_ARTIFACT));

      assertEquals(false, artTypes.inheritsFrom(ARTIFACT, LAST_ARTIFACT));
      assertEquals(false, artTypes.inheritsFrom(REQUIREMENT, LAST_ARTIFACT));
      assertEquals(false, artTypes.inheritsFrom(SUBSYSTEM_REQUIREMENT, LAST_ARTIFACT));
      assertEquals(true, artTypes.inheritsFrom(LAST_ARTIFACT, ARTIFACT));
      assertEquals(true, artTypes.inheritsFrom(LAST_ARTIFACT, REQUIREMENT));
      assertEquals(true, artTypes.inheritsFrom(LAST_ARTIFACT, OTHER_ARTIFACT));
      assertEquals(true, artTypes.inheritsFrom(LAST_ARTIFACT, SUBSYSTEM_REQUIREMENT));
   }

   @Test
   public void testGetAllDescendants() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      //@formatter:off
      assertContains(artTypes.getAllDescendantTypes(ARTIFACT), REQUIREMENT, SOFTWARE_REQUIREMENT, SYSTEM_REQUIREMENT, SUBSYSTEM_REQUIREMENT, OTHER_ARTIFACT, LAST_ARTIFACT);
      assertContains(artTypes.getAllDescendantTypes(REQUIREMENT), SOFTWARE_REQUIREMENT, SYSTEM_REQUIREMENT, SUBSYSTEM_REQUIREMENT, LAST_ARTIFACT);
      assertEquals(true, artTypes.getAllDescendantTypes(SOFTWARE_REQUIREMENT).isEmpty());
      assertEquals(true, artTypes.getAllDescendantTypes(SYSTEM_REQUIREMENT).isEmpty());
      assertEquals(true, artTypes.getAllDescendantTypes(SYSTEM_REQUIREMENT).isEmpty());
      assertEquals(true, artTypes.getAllDescendantTypes(LAST_ARTIFACT).isEmpty());
      assertContains(artTypes.getAllDescendantTypes(SUBSYSTEM_REQUIREMENT), LAST_ARTIFACT);
      assertContains(artTypes.getAllDescendantTypes(OTHER_ARTIFACT), SUBSYSTEM_REQUIREMENT, LAST_ARTIFACT);
      //@formatter:on
   }

   @Test
   public void testGetAllDescendantsWithDepth() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      //@formatter:off
      assertEquals(true, artTypes.getDescendantTypes(ARTIFACT, 0).isEmpty());
      assertContains(artTypes.getDescendantTypes(ARTIFACT, 1), REQUIREMENT, OTHER_ARTIFACT);
      assertContains(artTypes.getDescendantTypes(ARTIFACT, 2), REQUIREMENT, OTHER_ARTIFACT, SOFTWARE_REQUIREMENT, SYSTEM_REQUIREMENT, SUBSYSTEM_REQUIREMENT);
      assertContains(artTypes.getDescendantTypes(ARTIFACT, 3), REQUIREMENT, OTHER_ARTIFACT, SOFTWARE_REQUIREMENT, SYSTEM_REQUIREMENT, SUBSYSTEM_REQUIREMENT, LAST_ARTIFACT);
      
      assertContains(artTypes.getDescendantTypes(ARTIFACT, 10), REQUIREMENT, OTHER_ARTIFACT, SOFTWARE_REQUIREMENT, SYSTEM_REQUIREMENT, SUBSYSTEM_REQUIREMENT, LAST_ARTIFACT);
      assertContains(artTypes.getDescendantTypes(ARTIFACT, -1), REQUIREMENT, OTHER_ARTIFACT, SOFTWARE_REQUIREMENT, SYSTEM_REQUIREMENT, SUBSYSTEM_REQUIREMENT, LAST_ARTIFACT);
      //@formatter:on
   }

   @Test
   public void testIsValidAttributeType() throws OseeCoreException {
      // Field 1 will only be visible on branch A and Branch D
      // Field 2 will only be visible on branch B and Branch E

      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertEquals(true, artTypes.isValidAttributeType(OTHER_ARTIFACT, CoreBranches.SYSTEM_ROOT, NAME));
      assertEquals(true, artTypes.isValidAttributeType(OTHER_ARTIFACT, CoreBranches.SYSTEM_ROOT, ANNOTATION));
      assertEquals(false, artTypes.isValidAttributeType(OTHER_ARTIFACT, CoreBranches.SYSTEM_ROOT, WORDML));

      assertEquals(true, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, CoreBranches.SYSTEM_ROOT, NAME));
      assertEquals(true, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, CoreBranches.SYSTEM_ROOT, ANNOTATION));
      assertEquals(true, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, CoreBranches.SYSTEM_ROOT, WORDML));
      assertEquals(false, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, CoreBranches.SYSTEM_ROOT, FIELD_1));
      assertEquals(true, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_A, FIELD_1));
      assertEquals(false, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_B, FIELD_1));
      assertEquals(false, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_C, FIELD_1));
      assertEquals(true, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_D, FIELD_1));
      assertEquals(false, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_E, FIELD_1));
      assertEquals(false, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, CoreBranches.SYSTEM_ROOT, FIELD_2));
      assertEquals(false, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_A, FIELD_2));
      assertEquals(false, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_B, FIELD_2));
      assertEquals(false, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_C, FIELD_2));
      assertEquals(false, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_D, FIELD_2));
      assertEquals(false, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_E, FIELD_2));

      assertEquals(true, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_E, NAME));
      assertEquals(true, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_E, ANNOTATION));
      assertEquals(true, artTypes.isValidAttributeType(SUBSYSTEM_REQUIREMENT, BRANCH_E, WORDML));

      assertEquals(true, artTypes.isValidAttributeType(LAST_ARTIFACT, CoreBranches.SYSTEM_ROOT, NAME));
      assertEquals(true, artTypes.isValidAttributeType(LAST_ARTIFACT, CoreBranches.SYSTEM_ROOT, ANNOTATION));
      assertEquals(true, artTypes.isValidAttributeType(LAST_ARTIFACT, CoreBranches.SYSTEM_ROOT, WORDML));
      assertEquals(false, artTypes.isValidAttributeType(LAST_ARTIFACT, CoreBranches.SYSTEM_ROOT, FIELD_1));
      assertEquals(true, artTypes.isValidAttributeType(LAST_ARTIFACT, BRANCH_A, FIELD_1));
      assertEquals(false, artTypes.isValidAttributeType(LAST_ARTIFACT, BRANCH_B, FIELD_1));
      assertEquals(false, artTypes.isValidAttributeType(LAST_ARTIFACT, BRANCH_C, FIELD_1));
      assertEquals(true, artTypes.isValidAttributeType(LAST_ARTIFACT, BRANCH_D, FIELD_1));
      assertEquals(false, artTypes.isValidAttributeType(LAST_ARTIFACT, BRANCH_E, FIELD_1));

      assertEquals(false, artTypes.isValidAttributeType(LAST_ARTIFACT, CoreBranches.SYSTEM_ROOT, FIELD_2));
      assertEquals(false, artTypes.isValidAttributeType(LAST_ARTIFACT, BRANCH_A, FIELD_2));
      assertEquals(true, artTypes.isValidAttributeType(LAST_ARTIFACT, BRANCH_B, FIELD_2));
      assertEquals(false, artTypes.isValidAttributeType(LAST_ARTIFACT, BRANCH_C, FIELD_2));
      assertEquals(false, artTypes.isValidAttributeType(LAST_ARTIFACT, BRANCH_D, FIELD_2));
      assertEquals(true, artTypes.isValidAttributeType(LAST_ARTIFACT, BRANCH_E, FIELD_2));
   }

   @Test
   public void testGetAttributeTypes() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertContains(artTypes.getAttributeTypes(OTHER_ARTIFACT, CoreBranches.SYSTEM_ROOT), NAME, ANNOTATION);
      assertContains(artTypes.getAttributeTypes(LAST_ARTIFACT, CoreBranches.SYSTEM_ROOT), NAME, ANNOTATION, WORDML);

      //@formatter:off
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, CoreBranches.SYSTEM_ROOT), NAME, ANNOTATION, WORDML);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_A), NAME, ANNOTATION, WORDML, FIELD_1);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_B), NAME, ANNOTATION, WORDML);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_C), NAME, ANNOTATION, WORDML);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_D), NAME, ANNOTATION, WORDML, FIELD_1);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_E), NAME, ANNOTATION, WORDML);
      
      assertContains(artTypes.getAttributeTypes(LAST_ARTIFACT, CoreBranches.SYSTEM_ROOT), NAME, ANNOTATION, WORDML);
      assertContains(artTypes.getAttributeTypes(LAST_ARTIFACT, BRANCH_A), NAME, ANNOTATION, WORDML, FIELD_1);
      assertContains(artTypes.getAttributeTypes(LAST_ARTIFACT, BRANCH_B), NAME, ANNOTATION, WORDML, FIELD_2);
      assertContains(artTypes.getAttributeTypes(LAST_ARTIFACT, BRANCH_C), NAME, ANNOTATION, WORDML);
      assertContains(artTypes.getAttributeTypes(LAST_ARTIFACT, BRANCH_D), NAME, ANNOTATION, WORDML, FIELD_1);
      assertContains(artTypes.getAttributeTypes(LAST_ARTIFACT, BRANCH_E), NAME, ANNOTATION, WORDML, FIELD_2);
      //@formatter:on
   }

   @Test
   public void testReloadAddArtifactType() throws OseeCoreException {
      String addTypeDef = "artifactType \"Added Artifact Type\" extends \"Other Artifact\" {\n" + //
      "guid \"AUsuRi68hVhYLH76ENgA\" \n" + //
      "uuid 0x0000000000000023 \n" + //
      "}";

      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertEquals(7, artTypes.size());

      orcsTypes.invalidateAll();

      resources.add(asInput(addTypeDef));

      assertEquals(8, artTypes.size());
      IArtifactType artifactType = artTypes.getByUuid(0x0000000000000023L);

      assertEquals("Added Artifact Type", artifactType.getName());
      assertEquals(Long.valueOf(0x0000000000000023L), artifactType.getGuid());

      assertEquals(false, artTypes.isAbstract(artifactType));
      assertEquals(true, artTypes.inheritsFrom(artifactType, OTHER_ARTIFACT));
      assertEquals(true, artTypes.inheritsFrom(artifactType, ARTIFACT));
      assertEquals(false, artTypes.inheritsFrom(artifactType, REQUIREMENT));

      assertEquals(true, artTypes.exists(artifactType));
   }

   @Test
   public void testArtifactTypeOverride() throws OseeCoreException {
      ArtifactTypes artTypes = orcsTypes.getArtifactTypes();

      assertEquals(7, artTypes.size());

      assertContains(artTypes.getAttributeTypes(OTHER_ARTIFACT, CoreBranches.SYSTEM_ROOT), NAME, ANNOTATION);

      //@formatter:off
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, CoreBranches.SYSTEM_ROOT), NAME, ANNOTATION, WORDML);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_A), NAME, ANNOTATION, WORDML, FIELD_1);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_B), NAME, ANNOTATION, WORDML);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_C), NAME, ANNOTATION, WORDML);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_D), NAME, ANNOTATION, WORDML, FIELD_1);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_E), NAME, ANNOTATION, WORDML);
      //@formatter:on

      //@formatter:off
      String overrideArtTypes = 
         "\n overrides artifactType \"Artifact\" {\n" +
         "      inheritAll \n" +
         "      update attribute \"Annotation\" branchGuid \"AU2FErW1QwSXWPiP4cwA\"\n" + 
         "}\n" +
         "\n overrides artifactType \"Other Artifact\" {\n" +
         "      inheritAll \n" +
         "      add attribute \"Field 2\" \n" + 
         "}\n" +
         "\n overrides artifactType \"SubSystem Requirement\" {\n" +
         "      inheritAll \n" +
         "      remove attribute \"Field 1\" \n" + 
         "}\n" 
         ;
      //@formatter:on

      resources.add(asInput(overrideArtTypes));
      orcsTypes.invalidateAll();

      assertEquals(7, artTypes.size());

      assertContains(artTypes.getAttributeTypes(OTHER_ARTIFACT, CoreBranches.SYSTEM_ROOT), NAME, FIELD_2);
      assertContains(artTypes.getAttributeTypes(OTHER_ARTIFACT, BRANCH_A), NAME, ANNOTATION, FIELD_2);
      assertContains(artTypes.getAttributeTypes(OTHER_ARTIFACT, BRANCH_B), NAME, FIELD_2);
      assertContains(artTypes.getAttributeTypes(OTHER_ARTIFACT, BRANCH_C), NAME, FIELD_2);
      assertContains(artTypes.getAttributeTypes(OTHER_ARTIFACT, BRANCH_D), NAME, ANNOTATION, FIELD_2);
      assertContains(artTypes.getAttributeTypes(OTHER_ARTIFACT, BRANCH_E), NAME, FIELD_2);

      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, CoreBranches.SYSTEM_ROOT), NAME, WORDML, FIELD_2);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_A), NAME, ANNOTATION, WORDML, FIELD_2);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_B), NAME, WORDML, FIELD_2);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_C), NAME, WORDML, FIELD_2);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_D), NAME, ANNOTATION, WORDML, FIELD_2);
      assertContains(artTypes.getAttributeTypes(SUBSYSTEM_REQUIREMENT, BRANCH_E), NAME, WORDML, FIELD_2);
   }

   @Test
   public void testGetAllAttributeTypes() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      assertEquals(5, attrTypes.size());
      assertEquals(false, attrTypes.isEmpty());

      //@formatter:off
      assertContains(attrTypes.getAll(), NAME, ANNOTATION, WORDML, FIELD_1, FIELD_2);
      //@formatter:on
   }

   @Test
   public void testGetAttributeTypesByUuid() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      assertEquals(NAME, attrTypes.getByUuid(NAME.getGuid()));
      assertEquals(ANNOTATION, attrTypes.getByUuid(ANNOTATION.getGuid()));
      assertEquals(WORDML, attrTypes.getByUuid(WORDML.getGuid()));
      assertEquals(FIELD_1, attrTypes.getByUuid(FIELD_1.getGuid()));
      assertEquals(FIELD_2, attrTypes.getByUuid(FIELD_2.getGuid()));
   }

   @Test
   public void testExistsAttributeTypes() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      assertEquals(true, attrTypes.exists(NAME));
      assertEquals(true, attrTypes.exists(ANNOTATION));
      assertEquals(true, attrTypes.exists(WORDML));
      assertEquals(true, attrTypes.exists(FIELD_1));
      assertEquals(true, attrTypes.exists(FIELD_2));
   }

   @Test
   public void testGetAttributeProviderId() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      //@formatter:off
      assertEquals("org.eclipse.osee.framework.skynet.core.DefaultAttributeDataProvider", attrTypes.getAttributeProviderId(NAME));
      assertEquals("org.eclipse.osee.framework.skynet.core.UriAttributeDataProvider", attrTypes.getAttributeProviderId(ANNOTATION));
      assertEquals("org.eclipse.osee.framework.skynet.core.UriAttributeDataProvider", attrTypes.getAttributeProviderId(WORDML));
      assertEquals("org.eclipse.osee.framework.skynet.core.DefaultAttributeDataProvider", attrTypes.getAttributeProviderId(FIELD_1));
      assertEquals("org.eclipse.osee.framework.skynet.core.UriAttributeDataProvider", attrTypes.getAttributeProviderId(FIELD_2));
      //@formatter:on
   }

   @Test
   public void testGetBaseAttributeTypeId() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      //@formatter:off
      assertEquals("org.eclipse.osee.framework.skynet.core.StringAttribute", attrTypes.getBaseAttributeTypeId(NAME));
      assertEquals("org.eclipse.osee.framework.skynet.core.CompressedContentAttribute", attrTypes.getBaseAttributeTypeId(ANNOTATION));
      assertEquals("org.eclipse.osee.framework.skynet.core.WordAttribute", attrTypes.getBaseAttributeTypeId(WORDML));
      assertEquals("org.eclipse.osee.framework.skynet.core.EnumeratedAttribute", attrTypes.getBaseAttributeTypeId(FIELD_1));
      assertEquals("org.eclipse.osee.framework.skynet.core.DateAttribute", attrTypes.getBaseAttributeTypeId(FIELD_2));
      //@formatter:on
   }

   @Test
   public void testGetDefaultValue() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      //@formatter:off
      assertEquals("unnamed", attrTypes.getDefaultValue(NAME));
      assertEquals(null, attrTypes.getDefaultValue(ANNOTATION));
      assertEquals("<w:p xmlns:w=\"http://schemas.microsoft.com/office/word/2003/wordml\"><w:r><w:t></w:t></w:r></w:p>", attrTypes.getDefaultValue(WORDML));
      assertEquals("this is a field", attrTypes.getDefaultValue(FIELD_1));
      assertEquals(null, attrTypes.getDefaultValue(FIELD_2));
      //@formatter:on
   }

   @Test
   public void testGetDescription() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      //@formatter:off
      assertEquals("Descriptive Name", attrTypes.getDescription(NAME));
      assertEquals("the version \'1.0\' is this \"1.2.0\"", attrTypes.getDescription(ANNOTATION));
      assertEquals("value must comply with WordML xml schema", attrTypes.getDescription(WORDML));
      assertEquals("", attrTypes.getDescription(FIELD_1));
      assertEquals("field 2 description", attrTypes.getDescription(FIELD_2));
      //@formatter:on
   }

   @Test
   public void testGetFileExtension() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      //@formatter:off
      assertEquals("", attrTypes.getFileTypeExtension(NAME));
      assertEquals("", attrTypes.getFileTypeExtension(ANNOTATION));
      assertEquals("xml", attrTypes.getFileTypeExtension(WORDML));
      assertEquals("", attrTypes.getFileTypeExtension(FIELD_1));
      assertEquals("hello", attrTypes.getFileTypeExtension(FIELD_2));
      //@formatter:on
   }

   @Test
   public void testGetMinOccurrence() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      //@formatter:off
      assertEquals(1, attrTypes.getMinOccurrences(NAME));
      assertEquals(0, attrTypes.getMinOccurrences(ANNOTATION));
      assertEquals(0, attrTypes.getMinOccurrences(WORDML));
      assertEquals(2, attrTypes.getMinOccurrences(FIELD_1));
      assertEquals(1, attrTypes.getMinOccurrences(FIELD_2));
      //@formatter:on
   }

   @Test
   public void testGetMaxOccurrences() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      //@formatter:off
      assertEquals(1, attrTypes.getMaxOccurrences(NAME));
      assertEquals(Integer.MAX_VALUE, attrTypes.getMaxOccurrences(ANNOTATION));
      assertEquals(1, attrTypes.getMaxOccurrences(WORDML));
      assertEquals(3, attrTypes.getMaxOccurrences(FIELD_1));
      assertEquals(1, attrTypes.getMaxOccurrences(FIELD_2));
      //@formatter:on
   }

   @Test
   public void testGetTaggerId() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      //@formatter:off
      assertEquals("DefaultAttributeTaggerProvider", attrTypes.getTaggerId(NAME));
      assertEquals("DefaultAttributeTaggerProvider", attrTypes.getTaggerId(ANNOTATION));
      assertEquals("XmlAttributeTaggerProvider", attrTypes.getTaggerId(WORDML));
      assertEquals("", attrTypes.getTaggerId(FIELD_1));
      assertEquals("SomeOtherTagger", attrTypes.getTaggerId(FIELD_2));
      //@formatter:on
   }

   @Test
   public void testGetMediaType() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      //@formatter:off
      assertEquals("plan/text", attrTypes.getMediaType(NAME));
      assertEquals("plan/text", attrTypes.getMediaType(ANNOTATION));
      assertEquals("application/xml", attrTypes.getMediaType(WORDML));
      assertEquals("application/custom", attrTypes.getMediaType(FIELD_1));
      assertEquals("**", attrTypes.getMediaType(FIELD_2));
      //@formatter:on
   }

   @Test
   public void testGetAllTaggable() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      Collection<? extends IAttributeType> allTaggable = attrTypes.getAllTaggable();
      assertContains(allTaggable, NAME, ANNOTATION, WORDML, FIELD_2);
   }

   @Test
   public void testGetOseeEnum() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      EnumType enumType = attrTypes.getEnumType(FIELD_1);

      assertEquals("enum.test.proc.status", enumType.getName());
      assertEquals(Long.valueOf(0x3000000000000178L), enumType.getGuid());

      EnumEntry[] values = enumType.values();

      assertEnumEntry(values[0], "Completed -- Analysis in Work", "APt7j0WUEAIFUyyzVZgA", 1, "");
      assertEnumEntry(values[1], "Completed -- Passed", "APt7j0YZq1AjCER1qzAA", 2, "");
      assertEnumEntry(values[2], "Completed -- With Issues", "APt7j0aZWF2BJc_BqnQA", 3, "");
      assertEnumEntry(values[3], "Completed -- With Issues Resolved", "APt7j0cv9B1ImjckeTAA", 4, "");
      assertEnumEntry(values[4], "Not Performed", "APt7jzRPv2HBlrjQZXAA", 0, "it was not performed");
      assertEnumEntry(values[5], "Partially Complete", "AAvULbOIbxhPUO_oDFQA", 5, "is a partial");

      assertEnumEntry(enumType.valueOf(0), "Not Performed", "APt7jzRPv2HBlrjQZXAA", 0, "it was not performed");
      assertEnumEntry(enumType.valueOf(1), "Completed -- Analysis in Work", "APt7j0WUEAIFUyyzVZgA", 1, "");
      assertEnumEntry(enumType.valueOf(2), "Completed -- Passed", "APt7j0YZq1AjCER1qzAA", 2, "");
      assertEnumEntry(enumType.valueOf(3), "Completed -- With Issues", "APt7j0aZWF2BJc_BqnQA", 3, "");
      assertEnumEntry(enumType.valueOf(4), "Completed -- With Issues Resolved", "APt7j0cv9B1ImjckeTAA", 4, "");
      assertEnumEntry(enumType.valueOf(5), "Partially Complete", "AAvULbOIbxhPUO_oDFQA", 5, "is a partial");

      //@formatter:off
      assertEnumEntry(enumType.valueOf("Not Performed"), "Not Performed", "APt7jzRPv2HBlrjQZXAA", 0, "it was not performed");
      assertEnumEntry(enumType.valueOf("Completed -- Analysis in Work"), "Completed -- Analysis in Work", "APt7j0WUEAIFUyyzVZgA", 1, "");
      assertEnumEntry(enumType.valueOf("Completed -- Passed"), "Completed -- Passed", "APt7j0YZq1AjCER1qzAA", 2, "");
      assertEnumEntry(enumType.valueOf("Completed -- With Issues"), "Completed -- With Issues", "APt7j0aZWF2BJc_BqnQA", 3, "");
      assertEnumEntry(enumType.valueOf("Completed -- With Issues Resolved"), "Completed -- With Issues Resolved", "APt7j0cv9B1ImjckeTAA", 4, "");
      assertEnumEntry(enumType.valueOf("Partially Complete"), "Partially Complete", "AAvULbOIbxhPUO_oDFQA", 5, "is a partial");
    
      assertEnumEntry(enumType.getEntryByGuid("APt7jzRPv2HBlrjQZXAA"), "Not Performed", "APt7jzRPv2HBlrjQZXAA", 0, "it was not performed");
      assertEnumEntry(enumType.getEntryByGuid("APt7j0WUEAIFUyyzVZgA"), "Completed -- Analysis in Work", "APt7j0WUEAIFUyyzVZgA", 1, "");
      assertEnumEntry(enumType.getEntryByGuid("APt7j0YZq1AjCER1qzAA"), "Completed -- Passed", "APt7j0YZq1AjCER1qzAA", 2, "");
      assertEnumEntry(enumType.getEntryByGuid("APt7j0aZWF2BJc_BqnQA"), "Completed -- With Issues", "APt7j0aZWF2BJc_BqnQA", 3, "");
      assertEnumEntry(enumType.getEntryByGuid("APt7j0cv9B1ImjckeTAA"), "Completed -- With Issues Resolved", "APt7j0cv9B1ImjckeTAA", 4, "");
      assertEnumEntry(enumType.getEntryByGuid("AAvULbOIbxhPUO_oDFQA"), "Partially Complete", "AAvULbOIbxhPUO_oDFQA", 5, "is a partial");
      //@formatter:on

      Iterator<String> iterator = enumType.valuesAsOrderedStringSet().iterator();
      assertEquals("Completed -- Analysis in Work", iterator.next());
      assertEquals("Completed -- Passed", iterator.next());
      assertEquals("Completed -- With Issues", iterator.next());
      assertEquals("Completed -- With Issues Resolved", iterator.next());
      assertEquals("Not Performed", iterator.next());
      assertEquals("Partially Complete", iterator.next());
   }

   private void assertEnumEntry(EnumEntry actual, String name, String uuid, int ordinal, String description) {
      assertEquals(name, actual.getName());
      assertEquals(uuid, actual.getGuid());
      assertEquals(ordinal, actual.ordinal());
      assertEquals(description, actual.getDescription());
   }

   @Test
   public void testEnumOverride() throws OseeCoreException {
      //@formatter:off
      String enumOverride = "overrides enum \"enum.test.proc.status\" { \n" +
         "inheritAll \n" +
         "add \"In Work\" entryGuid \"CArJmMckZm_uUjBpStQA\" description \"this is in work\"\n" +
         "remove \"enum.test.proc.status.Completed -- With Issues\" \n" +
      "}\n";
      //@formatter:on

      orcsTypes.invalidateAll();
      resources.add(asInput(enumOverride));

      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      EnumType enumType = attrTypes.getEnumType(FIELD_1);

      assertEquals("enum.test.proc.status", enumType.getName());
      assertEquals(Long.valueOf(0x3000000000000178L), enumType.getGuid());

      Iterator<String> iterator = enumType.valuesAsOrderedStringSet().iterator();
      assertEquals("Completed -- Analysis in Work", iterator.next());
      assertEquals("Completed -- Passed", iterator.next());
      assertEquals("Completed -- With Issues Resolved", iterator.next());
      assertEquals("In Work", iterator.next());
      assertEquals("Not Performed", iterator.next());
      assertEquals("Partially Complete", iterator.next());

      EnumEntry[] values = enumType.values();

      assertEnumEntry(values[0], "Completed -- Analysis in Work", "APt7j0WUEAIFUyyzVZgA", 1, "");
      assertEnumEntry(values[1], "Completed -- Passed", "APt7j0YZq1AjCER1qzAA", 2, "");
      assertEnumEntry(values[2], "Completed -- With Issues Resolved", "APt7j0cv9B1ImjckeTAA", 3, "");

      assertEnumEntry(values[3], "In Work", "CArJmMckZm_uUjBpStQA", 5, "this is in work");

      assertEnumEntry(values[4], "Not Performed", "APt7jzRPv2HBlrjQZXAA", 0, "it was not performed");
      assertEnumEntry(values[5], "Partially Complete", "AAvULbOIbxhPUO_oDFQA", 4, "is a partial");
   }

   @Test
   public void testReloadAddAttributeType() throws OseeCoreException {
      AttributeTypes attrTypes = orcsTypes.getAttributeTypes();

      assertEquals(5, attrTypes.size());

      orcsTypes.invalidateAll();

      //@formatter:off
      String addAttributeType = "attributeType \"Field 3\" extends DateAttribute {" +
        "guid \"AizLp7tWSgr9HNzdmUAA\" \n" +
        "uuid 0x1000000000000082 \n" +
        "dataProvider DefaultAttributeDataProvider \n" +
        "min 1 \n" +
        "max 1 \n" +
        "taggerId AnotherTagger \n" +
        "description \"Added dynamically\" \n" +
      "}\n";
      //@formatter:on

      resources.add(asInput(addAttributeType));

      assertEquals(6, attrTypes.size());

      IAttributeType attrType = attrTypes.getByUuid(0x1000000000000082L);

      //@formatter:off
      assertEquals("Field 3", attrType.getName());
      assertEquals(Long.valueOf(0x1000000000000082L), attrType.getGuid());
      assertEquals("org.eclipse.osee.framework.skynet.core.DefaultAttributeDataProvider", attrTypes.getAttributeProviderId(attrType));
      assertEquals("org.eclipse.osee.framework.skynet.core.DateAttribute", attrTypes.getBaseAttributeTypeId(attrType));
      assertEquals(null, attrTypes.getDefaultValue(attrType));
      assertEquals("Added dynamically", attrTypes.getDescription(attrType));
      assertEquals("", attrTypes.getFileTypeExtension(attrType));
      assertEquals(1, attrTypes.getMinOccurrences(attrType));
      assertEquals(1, attrTypes.getMaxOccurrences(attrType));
      assertEquals("AnotherTagger", attrTypes.getTaggerId(attrType));
      assertEquals(null, attrTypes.getEnumType(attrType));
      assertEquals(false, attrTypes.isEnumerated(attrType));
      assertEquals(true, attrTypes.isTaggable(attrType));
      
      assertEquals(true, attrTypes.exists(attrType));
      //@formatter:on
   }

   @Test
   public void testGetAllRelationTypes() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(2, relTypes.size());
      assertEquals(false, relTypes.isEmpty());

      //@formatter:off
      assertContains(relTypes.getAll(), REQUIREMENT_REL, ANOTHER_REL);
      //@formatter:on
   }

   @Test
   public void testGetRelationTypesByUuid() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(REQUIREMENT_REL, relTypes.getByUuid(REQUIREMENT_REL.getGuid()));
      assertEquals(ANOTHER_REL, relTypes.getByUuid(ANOTHER_REL.getGuid()));
   }

   @Test
   public void testExistsRelationTypes() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(true, relTypes.exists(REQUIREMENT_REL));
      assertEquals(true, relTypes.exists(ANOTHER_REL));
   }

   @Test
   public void testGetArtifactType() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(REQUIREMENT, relTypes.getArtifactType(REQUIREMENT_REL, SIDE_A));
      assertEquals(SUBSYSTEM_REQUIREMENT, relTypes.getArtifactType(REQUIREMENT_REL, SIDE_B));

      assertEquals(OTHER_ARTIFACT, relTypes.getArtifactType(ANOTHER_REL, SIDE_A));
      assertEquals(LAST_ARTIFACT, relTypes.getArtifactType(ANOTHER_REL, SIDE_B));
   }

   @Test
   public void testGetArtifactTypeSideA() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(REQUIREMENT, relTypes.getArtifactTypeSideA(REQUIREMENT_REL));
      assertEquals(OTHER_ARTIFACT, relTypes.getArtifactTypeSideA(ANOTHER_REL));
   }

   @Test
   public void testGetArtifactTypeSideB() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(SUBSYSTEM_REQUIREMENT, relTypes.getArtifactTypeSideB(REQUIREMENT_REL));
      assertEquals(LAST_ARTIFACT, relTypes.getArtifactTypeSideB(ANOTHER_REL));
   }

   @Test
   public void testGetDefaultOrderTypeGuid() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      //@formatter:off
      assertEquals(RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC.getGuid(), relTypes.getDefaultOrderTypeGuid(REQUIREMENT_REL));
      assertEquals(RelationOrderBaseTypes.UNORDERED.getGuid(), relTypes.getDefaultOrderTypeGuid(ANOTHER_REL));
      //@formatter:on
   }

   @Test
   public void testGetMultiplicity() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(RelationTypeMultiplicity.ONE_TO_MANY, relTypes.getMultiplicity(REQUIREMENT_REL));
      assertEquals(RelationTypeMultiplicity.MANY_TO_MANY, relTypes.getMultiplicity(ANOTHER_REL));
   }

   @Test
   public void testGetSideNameA() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals("requirement-sideA", relTypes.getSideAName(REQUIREMENT_REL));
      assertEquals("other-sideA", relTypes.getSideAName(ANOTHER_REL));
   }

   @Test
   public void testGetSideNameB() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals("subsystem-sideB", relTypes.getSideBName(REQUIREMENT_REL));
      assertEquals("last-sideB", relTypes.getSideBName(ANOTHER_REL));
   }

   @Test
   public void testGetSideName() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals("requirement-sideA", relTypes.getSideName(REQUIREMENT_REL, SIDE_A));
      assertEquals("subsystem-sideB", relTypes.getSideName(REQUIREMENT_REL, SIDE_B));

      assertEquals("other-sideA", relTypes.getSideName(ANOTHER_REL, SIDE_A));
      assertEquals("last-sideB", relTypes.getSideName(ANOTHER_REL, SIDE_B));
   }

   @Test
   public void testIsSideName() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(true, relTypes.isSideAName(REQUIREMENT_REL, "requirement-sideA"));
      assertEquals(false, relTypes.isSideAName(REQUIREMENT_REL, "subsystem-sideB"));

      assertEquals(true, relTypes.isSideAName(ANOTHER_REL, "other-sideA"));
      assertEquals(false, relTypes.isSideAName(ANOTHER_REL, "last-sideB"));
   }

   @Test
   public void testIsOrdered() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(true, relTypes.isOrdered(REQUIREMENT_REL));
      assertEquals(false, relTypes.isOrdered(ANOTHER_REL));
   }

   @Test
   public void testIsArtifactTypeAllowed() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(false, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_A, ARTIFACT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_A, REQUIREMENT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_A, SOFTWARE_REQUIREMENT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_A, SYSTEM_REQUIREMENT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_A, SUBSYSTEM_REQUIREMENT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_A, OTHER_ARTIFACT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_A, LAST_ARTIFACT));

      assertEquals(false, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_B, ARTIFACT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_B, REQUIREMENT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_B, SOFTWARE_REQUIREMENT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_B, SYSTEM_REQUIREMENT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_B, SUBSYSTEM_REQUIREMENT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_B, OTHER_ARTIFACT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(REQUIREMENT_REL, SIDE_B, LAST_ARTIFACT));

      assertEquals(false, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_A, ARTIFACT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_A, REQUIREMENT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_A, SOFTWARE_REQUIREMENT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_A, SYSTEM_REQUIREMENT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_A, SUBSYSTEM_REQUIREMENT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_A, OTHER_ARTIFACT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_A, LAST_ARTIFACT));

      assertEquals(false, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_B, ARTIFACT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_B, REQUIREMENT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_B, SOFTWARE_REQUIREMENT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_B, SYSTEM_REQUIREMENT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_B, SUBSYSTEM_REQUIREMENT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_B, OTHER_ARTIFACT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(ANOTHER_REL, SIDE_B, LAST_ARTIFACT));
   }

   @Test
   public void testReloadAddRelationType() throws OseeCoreException {
      RelationTypes relTypes = orcsTypes.getRelationTypes();

      assertEquals(2, relTypes.size());
      orcsTypes.invalidateAll();

      //@formatter:off
      String addType = "relationType \"Dynamic Relation\" {\n"+
          "guid \"Ai1n5tou4mr0pqXEPgQA\" \n"+
          "uuid 0x2000000000000159 \n"+
          "sideAName \"dynamic-sideA\" \n"+
          "sideAArtifactType \"Artifact\" \n"+
          "sideBName \"dynamic-sideB\" \n"+
          "sideBArtifactType \"Other Artifact\" \n"+
          "defaultOrderType Lexicographical_Descending \n"+
          "multiplicity MANY_TO_ONE \n" +
      "}\n";
      //@formatter:on

      resources.add(asInput(addType));

      assertEquals(3, relTypes.size());

      IRelationType relation = relTypes.getByUuid(0x2000000000000159L);

      assertEquals("Dynamic Relation", relation.getName());
      assertEquals(Long.valueOf(0x2000000000000159L), relation.getGuid());

      assertEquals(ARTIFACT, relTypes.getArtifactType(relation, SIDE_A));
      assertEquals(OTHER_ARTIFACT, relTypes.getArtifactType(relation, SIDE_B));
      assertEquals(ARTIFACT, relTypes.getArtifactTypeSideA(relation));
      assertEquals(OTHER_ARTIFACT, relTypes.getArtifactTypeSideB(relation));
      assertEquals(RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC.getGuid(), relTypes.getDefaultOrderTypeGuid(relation));
      assertEquals(RelationTypeMultiplicity.MANY_TO_ONE, relTypes.getMultiplicity(relation));
      assertEquals("dynamic-sideA", relTypes.getSideName(relation, SIDE_A));
      assertEquals("dynamic-sideB", relTypes.getSideName(relation, SIDE_B));
      assertEquals("dynamic-sideA", relTypes.getSideAName(relation));
      assertEquals("dynamic-sideB", relTypes.getSideBName(relation));
      assertEquals(true, relTypes.isOrdered(relation));
      assertEquals(true, relTypes.isSideAName(relation, "dynamic-sideA"));
      assertEquals(false, relTypes.isSideAName(relation, "dynamic-sideB"));
      assertEquals(true, relTypes.isArtifactTypeAllowed(relation, SIDE_A, LAST_ARTIFACT));
      assertEquals(false, relTypes.isArtifactTypeAllowed(relation, SIDE_B, REQUIREMENT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(relation, SIDE_B, OTHER_ARTIFACT));
      assertEquals(true, relTypes.isArtifactTypeAllowed(relation, SIDE_B, LAST_ARTIFACT));

      assertEquals(true, relTypes.exists(relation));
   }

   private static void assertContains(Collection<?> actual, Identity<?>... expected) {
      List<?> asList = Arrays.asList(expected);

      String message = String.format("Actual: [%s] Expected: [%s]", actual, Arrays.deepToString(expected));

      assertEquals(message, asList.size(), actual.size());
      assertEquals(message, true, actual.containsAll(asList));
   }

   private static InputSupplier<? extends InputStream> getResource(String resourcePath) {
      URL resource = Resources.getResource(OrcsTypesTest.class, resourcePath);
      return Resources.newInputStreamSupplier(resource);
   }

   private static InputSupplier<? extends InputStream> asInput(final String data) {
      return new InputSupplier<InputStream>() {
         @Override
         public InputStream getInput() throws java.io.IOException {
            return new ByteArrayInputStream(data.getBytes("UTF-8"));
         }
      };
   }
   private static final class MultiResource implements IResource {
      private final Iterable<? extends InputSupplier<? extends InputStream>> suppliers;
      private final URI resourceUri;

      public MultiResource(URI resourceUri, Iterable<? extends InputSupplier<? extends InputStream>> suppliers) {
         super();
         this.suppliers = suppliers;
         this.resourceUri = resourceUri;
      }

      @Override
      public InputStream getContent() throws OseeCoreException {
         InputStream stream = null;
         InputSupplier<InputStream> join = ByteStreams.join(suppliers);
         try {
            stream = join.getInput();
         } catch (IOException ex) {
            OseeExceptions.wrapAndThrow(ex);
         }
         return stream;
      }

      @Override
      public URI getLocation() {
         return resourceUri;
      }

      @Override
      public String getName() {
         String value = resourceUri.toASCIIString();
         return value.substring(value.lastIndexOf("/") + 1, value.length());
      }

      @Override
      public boolean isCompressed() {
         return false;
      }

   }

}
