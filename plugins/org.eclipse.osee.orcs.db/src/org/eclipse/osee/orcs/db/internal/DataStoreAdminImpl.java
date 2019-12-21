/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.orcs.db.internal;

import com.google.common.base.Supplier;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.osee.framework.core.data.UserId;
import java.util.stream.Stream;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcMigrationOptions;
import org.eclipse.osee.jdbc.JdbcMigrationResource;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.SystemProperties;
import org.eclipse.osee.orcs.core.ds.DataStoreAdmin;
import org.eclipse.osee.orcs.core.ds.DataStoreConstants;
import org.eclipse.osee.orcs.core.ds.DataStoreInfo;
import org.eclipse.osee.orcs.db.internal.callable.FetchDatastoreInfoCallable;
import org.eclipse.osee.orcs.db.internal.callable.MigrateDatastoreCallable;
import org.eclipse.osee.orcs.db.internal.resource.AttributeLocatorProvider;
import org.eclipse.osee.orcs.db.internal.resource.ResourceConstants;
import org.eclipse.osee.orcs.db.internal.sql.join.IdJoinQuery;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;
import org.eclipse.osee.orcs.db.internal.util.DynamicSchemaResourceProvider;

/**
 * @author Roberto E. Escobar
 */
public class DataStoreAdminImpl implements DataStoreAdmin {

   private final Log logger;
   private final JdbcClient jdbcClient;
   private final SystemProperties properties;
   private final SqlJoinFactory sqlJoinFactory;

   public DataStoreAdminImpl(Log logger, JdbcClient jdbcClient, SystemProperties properties, SqlJoinFactory sqlJoinFactory) {
      this.logger = logger;
      this.jdbcClient = jdbcClient;
      this.properties = properties;
      this.sqlJoinFactory = sqlJoinFactory;
   }

   @Override
   public void createDataStore() {
      Supplier<Iterable<JdbcMigrationResource>> schemaProvider = new DynamicSchemaResourceProvider(logger);

      JdbcMigrationOptions options = new JdbcMigrationOptions(true, true);
      Conditions.checkExpressionFailOnTrue(jdbcClient.getConfig().isProduction(),
         "Error - attempting to initialize a production datastore.");

      jdbcClient.createDataStore(options, schemaProvider.get());

      String attributeDataPath = ResourceConstants.getAttributeDataPath(properties);
      logger.info("Deleting application server binary data [%s]...", attributeDataPath);
      Lib.deleteDir(new File(attributeDataPath));

      properties.putValue(DataStoreConstants.DATASTORE_ID_KEY, GUID.create());

      addDefaultPermissions();

      jdbcClient.invalidateSequences();
   }

   private void addDefaultPermissions() {
      List<Object[]> data = new LinkedList<>();
      for (PermissionEnum permission : PermissionEnum.values()) {
         data.add(new Object[] {permission.getPermId(), permission.getName()});
      }
      jdbcClient.runBatchUpdate("INSERT INTO OSEE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES (?,?)", data);
   }

   @Override
   public Callable<DataStoreInfo> migrateDataStore(OrcsSession session) {
      Supplier<Iterable<JdbcMigrationResource>> schemaProvider = new DynamicSchemaResourceProvider(logger);
      JdbcMigrationOptions options = new JdbcMigrationOptions(false, false);

      return new MigrateDatastoreCallable(session, logger, jdbcClient, properties, schemaProvider, options);
   }

   @Override
   public Callable<DataStoreInfo> getDataStoreInfo(OrcsSession session) {
      Supplier<Iterable<JdbcMigrationResource>> schemaProvider = new DynamicSchemaResourceProvider(logger);
      return new FetchDatastoreInfoCallable(logger, jdbcClient, schemaProvider, properties);
   }

   @Override
   public JdbcClient getJdbcClient() {
      return jdbcClient;
   }

   @Override
   public void updateBootstrapUser(UserId accountId) {
      jdbcClient.runPreparedUpdate("UPDATE osee_tx_details SET author = ? where author <= 0", accountId);
   }
   private void recurseDir(File parentDirectory) throws IOException {
      File[] files = parentDirectory.listFiles();
      boolean hasSubDirectory = false;
      for (int i = 0; i < files.length; i++) {
         if (files[i].isDirectory()) {
            hasSubDirectory = true;
            recurseDir(files[i]);
         }
      }
      if (!hasSubDirectory) {

      }
   }

   private Path attributeDataPath;
   private IdJoinQuery gammaJoin;
   private long countPurged = 0;

   private void walker() throws IOException {
      Path attributeDataPath = Paths.get(ResourceConstants.getAttributeDataPath(preferences));

      Files.walkFileTree(attributeDataPath, new SimpleFileVisitor<Path>() {
         @Override
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.SKIP_SUBTREE;
         }

         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
         }

         @Override
         public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
            if (e == null) {
               Files.delete(dir);
               return FileVisitResult.CONTINUE;
            } else {
               // directory iteration failed
               throw e;
            }
         }
      });

   }

   private void handleFile(Path path) {
      File file = path.toFile();
      if (!file.isDirectory()) {
         Path pathRelative = attributeDataPath.relativize(path);
         StringBuilder strB = new StringBuilder();
         int stop = pathRelative.getNameCount() - 1;
         for (int i = 0; i < stop; i++) {
            strB.append(pathRelative.getName(i));
         }
         gammaJoin.add(Long.valueOf(strB.toString()));
      }
   }

   @Override
   public long purgeUnusedBinaryAttributes() {
      //      return loadBinaryGammasIntoJoinTable();
      findUnusedGammas();
      return countPurged;

   }

   private long loadBinaryGammasIntoJoinTable() {
      try (IdJoinQuery idJoin = sqlJoinFactory.createIdJoinQuery()) {
         gammaJoin = idJoin;
         try (Stream<String> stream = Files.lines(Paths.get("C:\\Users\\b1117361\\Desktop\\g4.txt"))) {
            stream.forEach(gamma -> gammaJoin.add(Long.valueOf(gamma)));
         } catch (IOException ex) {
            logger.error(ex, "in purgeUnusedBinaryAttributes");
         }
         gammaJoin.store();
         System.out.println(gammaJoin.getQueryId());
         System.out.println(gammaJoin.getQueryId());
         // don't let try statement complete or the join table will be cleaned out
      }
      return gammaJoin.size();
   }

   private void findAllBinaryGammas() {
      try (IdJoinQuery idJoin = sqlJoinFactory.createIdJoinQuery()) {
         gammaJoin = idJoin;
         //               String attributeDataPathStr = ResourceConstants.getAttributeDataPath(preferences);
         String attributeDataPathStr = "\\\\osee.msc.az.boeing.com\\osee_server_data\\attr\\975";
         attributeDataPath = Paths.get(attributeDataPathStr);

         try (Stream<Path> walk = Files.walk(attributeDataPath)) {
            walk.sorted(Comparator.reverseOrder()).forEach(this::handleFile);
         }
         gammaJoin.store();

      } catch (IOException ex) {
         logger.error(ex, "in purgeUnusedBinaryAttributes");
      }
   }

   private void findUnusedGammas() {
      String attributeDataPathStr = ResourceConstants.getAttributeDataPath(preferences);
      String sql = "SELECT id FROM osee_join_id WHERE query_id = ? MINUS SELECT gamma_id FROM osee_txs";

      try (BufferedWriter out = new BufferedWriter(new FileWriter("C:\\Users\\b1117361\\Desktop\\remove11.sh"))) {

         jdbcClient.runQuery(stmt -> {
            String gammaId = stmt.getString("id");
            StringBuilder builder = new StringBuilder();
            AttributeLocatorProvider.seedTo(builder, gammaId);

            //         File file = new File(attributeDataFile, builder.toString());
            //         Path removePath = Paths.get(attributeDataPathStr, builder.toString());
            try {
               out.write("rm -rf " + builder + "\n");
            } catch (IOException ex) {
               logger.error(ex, "in findUnusedGammas");
            }
            countPurged++;
            //         Lib.deleteFileAndEmptyParents(attributeDataPathStr, file);
         }, sql, 726199745L /* gammaJoin.getQueryId() */);
         out.flush();
      } catch (IOException ex) {
         logger.error(ex, "in findUnusedGammas");
      }
   }

   private void deleteDirectory(Path removePath) {
      try {
         Files.walk(removePath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      } catch (IOException ex) {
         logger.error(ex, "in purgeUnusedBinaryAttributes");
      }
   }

}