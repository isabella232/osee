/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.x.server.integration.tests.performance;

import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.Folder;
import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.GeneralData;
import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.GeneralDocument;
import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.Requirement;
import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.SoftwareRequirement;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.AccessContextId;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.Active;
import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import static org.eclipse.osee.framework.core.enums.DemoBranches.SAW_Bld_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.rest.client.OseeClient;
import org.eclipse.osee.orcs.rest.model.IdeClientEndpoint;
import org.eclipse.osee.orcs.rest.model.IdeVersion;
import org.eclipse.osee.orcs.rest.model.search.artifact.RequestType;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchResult;
import org.eclipse.osee.x.server.integration.tests.util.IntegrationUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class OseeClientQueryTest {

   private static final String GUID1 = SystemUser.Anonymous.getGuid();
   private static final String GUID2 = SystemUser.OseeSystem.getGuid();

   private static OseeClient createClient;

   @BeforeClass
   public static void testSetup() throws OseeCoreException {
      createClient = IntegrationUtil.createClient();

      // Establish initial connection to the db using this random query
      createClient.createQueryBuilder(COMMON).andIds(SystemUser.OseeSystem).getSearchResult(RequestType.IDS);
   }

   @Test
   public void searchForAttributeTypeByTokenId() throws OseeCoreException {
      final int EXPECTED_RESULTS = 1;
      SearchResult results =
         createClient.createQueryBuilder(COMMON).andIds(SystemUser.OseeSystem).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForAttributeTypeByTokenIds() throws OseeCoreException {
      final int EXPECTED_RESULTS = 2;
      SearchResult results =
         createClient.createQueryBuilder(COMMON).andIds(SystemUser.OseeSystem, SystemUser.Anonymous).getSearchResult(
            RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForArtifactByGuid() throws OseeCoreException {
      final int EXPECTED_RESULTS = 1;
      SearchResult results = createClient.createQueryBuilder(COMMON).andGuids(GUID1).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForArtifactByGuids() throws OseeCoreException {
      final int EXPECTED_RESULTS = 2;
      SearchResult results =
         createClient.createQueryBuilder(COMMON).andGuids(GUID1, GUID2).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForArtifactByLocalId() throws OseeCoreException {
      final int EXPECTED_RESULTS = 1;
      SearchResult results = createClient.createQueryBuilder(COMMON).andLocalId(9).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForArtifactByLocalIds() throws OseeCoreException {
      final int EXPECTED_RESULTS = 2;
      SearchResult results = createClient.createQueryBuilder(COMMON).andLocalId(19, 9).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForArtifactByName() throws OseeCoreException {
      final int EXPECTED_RESULTS = 1;
      SearchResult results =
         createClient.createQueryBuilder(COMMON).andNameEquals("Joe Smith").getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForArtifactWithActionInName() throws OseeCoreException {
      final int EXPECTED_RESULTS = 43;
      SearchResult results = createClient.createQueryBuilder(COMMON).and(CoreAttributeTypes.Name, "SAW",
         QueryOption.CASE__IGNORE, QueryOption.TOKEN_MATCH_ORDER__MATCH, QueryOption.TOKEN_DELIMITER__ANY,
         QueryOption.TOKEN_COUNT__IGNORE).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForArtifactType() throws OseeCoreException {
      final int EXPECTED_RESULTS = 7;
      SearchResult results =
         createClient.createQueryBuilder(SAW_Bld_1).andTypeEquals(Folder).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForArtifactTypes() throws OseeCoreException {
      final int EXPECTED_RESULTS = 24;
      SearchResult results = createClient.createQueryBuilder(SAW_Bld_1).andTypeEquals(GeneralData, GeneralDocument,
         SoftwareRequirement).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForArtifactTypesIncludeTypeInheritance() throws OseeCoreException {
      final int EXPECTED_RESULTS = 150;
      SearchResult results = createClient.createQueryBuilder(SAW_Bld_1).andIsOfType(GeneralData, GeneralDocument,
         Requirement).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForExistenceOfAttributeType() throws OseeCoreException {
      final int EXPECTED_RESULTS = 28;
      SearchResult results = createClient.createQueryBuilder(COMMON).andExists(Active).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForExistenceOfAttributeTypeIncludeDeleted() throws OseeCoreException {
      final int EXPECTED_RESULTS = 28;
      SearchResult results =
         createClient.createQueryBuilder(COMMON).andExists(Active).includeDeleted().getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void searchForExistenceOfAttributeTypes() throws OseeCoreException {
      final int EXPECTED_RESULTS = 28;
      SearchResult results =
         createClient.createQueryBuilder(COMMON).andExists(Active, AccessContextId).getSearchResult(RequestType.IDS);
      assertEquals(EXPECTED_RESULTS, results.getTotal());
   }

   @Test
   public void supportedVersions() {
      IdeClientEndpoint endpoint = createClient.getIdeClientEndpoint();
      IdeVersion versions = endpoint.getSupportedVersions();
      assertNotNull(versions);
      Collection<String> supportedVersions = versions.getVersions();
      assertEquals(true, !supportedVersions.isEmpty());
   }

   @Test
   public void orcsScript() {
      String script =
         "start from branch 570 find artifacts where art-type = 'Folder' collect artifacts {id, attributes { value } };";
      String expected = "{\n" + //
      "  'parameters' : {\n" + //
      "    'output.debug' : 'false'\n" + //
      "  },\n" + //
      "  'script' : 'start from branch 570 find artifacts where art-type = 'Folder' collect artifacts {id, attributes { value } };',\n" + //
      "  'results' : [ {\n" + //
      "    'artifacts' : [ {\n" + //
      "      'id' : 8,\n" + //
      "      'attributes' : {\n" + //
      "        'Name' : {\n" + //
      "          'value' : 'User Groups'\n" + //
      "        }\n" + //
      "      }\n" + //
      "    }, {\n" + //
      "      'id' : 26,\n" + //
      "      'attributes' : {\n" + //
      "        'Name' : {\n" + //
      "          'value' : 'Document Templates'\n" + //
      "        }\n" + //
      "      }\n" + //
      "    }, {\n" + //
      "      'id' : 31,\n" + //
      "      'attributes' : {\n" + //
      "        'Name' : {\n" + //
      "          'value' : 'Action Tracking System'\n" + //
      "        }\n" + //
      "      }\n" + //
      "    }, {\n" + //
      "      'id' : 34,\n" + //
      "      'attributes' : {\n" + //
      "        'Name' : {\n" + //
      "          'value' : 'Config'\n" + //
      "        }\n" + //
      "      }\n" + //
      "    }, {\n" + //
      "      'id' : 35,\n" + //
      "      'attributes' : {\n" + //
      "        'Name' : {\n" + //
      "          'value' : 'Work Definitions'\n" + //
      "        }\n" + //
      "      }\n" + //
      "    } ]\n" + //
      "  } ]\n" + //
      "}";

      StringWriter writer = new StringWriter();
      Properties properties = new Properties();
      createClient.executeScript(script, properties, false, MediaType.APPLICATION_JSON_TYPE, writer);

      assertEquals(expected, normalize(writer.toString()));
   }

   private String normalize(String value) {
      value = value.replaceAll("\r\n", "\n");
      value = value.replaceAll("\"", "'");
      return value;
   }
}
