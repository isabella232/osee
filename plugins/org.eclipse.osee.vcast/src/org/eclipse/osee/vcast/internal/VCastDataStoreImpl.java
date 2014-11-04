/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributostmt:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.vcast.internal;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.vcast.VCastDataStore;
import org.eclipse.osee.vcast.model.VCastBranchCoverage;
import org.eclipse.osee.vcast.model.VCastBranchData;
import org.eclipse.osee.vcast.model.VCastCoverageType;
import org.eclipse.osee.vcast.model.VCastFunction;
import org.eclipse.osee.vcast.model.VCastInstrumentedFile;
import org.eclipse.osee.vcast.model.VCastMcdcCoverage;
import org.eclipse.osee.vcast.model.VCastMcdcCoverageCondition;
import org.eclipse.osee.vcast.model.VCastMcdcCoveragePair;
import org.eclipse.osee.vcast.model.VCastMcdcCoveragePairRow;
import org.eclipse.osee.vcast.model.VCastMcdcData;
import org.eclipse.osee.vcast.model.VCastMcdcDataCondition;
import org.eclipse.osee.vcast.model.VCastProject;
import org.eclipse.osee.vcast.model.VCastProjectFile;
import org.eclipse.osee.vcast.model.VCastResult;
import org.eclipse.osee.vcast.model.VCastSetting;
import org.eclipse.osee.vcast.model.VCastSourceFile;
import org.eclipse.osee.vcast.model.VCastSourceFileJoin;
import org.eclipse.osee.vcast.model.VCastStatementCoverage;
import org.eclipse.osee.vcast.model.VCastStatementData;
import org.eclipse.osee.vcast.model.VCastVersion;
import org.eclipse.osee.vcast.model.VCastWritable;

/**
 * @author Shawn F. Cook
 */
public class VCastDataStoreImpl implements VCastDataStore {

   public static interface StatementProvider {
      IOseeStatement getStatement() throws OseeCoreException;
   }

   private final StatementProvider provider;

   public VCastDataStoreImpl(StatementProvider provider) {
      super();
      this.provider = provider;
   }

   private IOseeStatement getStatement() throws OseeCoreException {
      return provider.getStatement();
   }

   @Override
   public Collection<VCastBranchCoverage> getAllBranchCoverages() throws OseeCoreException {
      Collection<VCastBranchCoverage> toReturn = new ArrayList<VCastBranchCoverage>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM branch_coverage");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer function_id = stmt.getInt("function_id");
            Integer line = stmt.getInt("line");
            Integer num_conditions = stmt.getInt("num_conditions");
            Integer true_count = stmt.getInt("true_count");
            Integer false_count = stmt.getInt("false_count");
            Integer max_true_count = stmt.getInt("max_true_count");
            Integer max_false_count = stmt.getInt("max_false_count");
            toReturn.add(new VCastBranchCoverage(id, function_id, line, num_conditions, true_count, false_count,
               max_true_count, max_false_count));
         }
      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastBranchData> getAllBranchData() throws OseeCoreException {
      Collection<VCastBranchData> toReturn = new ArrayList<VCastBranchData>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM branch_data");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Long branch_id = stmt.getLong("branch_id");
            Integer result_id = stmt.getInt("result_id");
            Integer result_line = stmt.getInt("result_line");
            Boolean taken = stmt.getBoolean("taken");
            toReturn.add(new VCastBranchData(id, branch_id, result_id, result_line, taken));
         }
      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastFunction> getAllFunctions() throws OseeCoreException {
      Collection<VCastFunction> toReturn = new ArrayList<VCastFunction>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM functions");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer instrumented_file_id = stmt.getInt("instrumented_file_id");
            Integer findex = stmt.getInt("findex");
            String name = stmt.getString("name");
            String canonical_name = stmt.getString("canonical_name");
            Integer total_lines = stmt.getInt("total_lines");
            Integer complexity = stmt.getInt("complexity");
            Integer numPairsOrPaths = stmt.getInt("num_pairs_or_paths");
            toReturn.add(new VCastFunction(id, instrumented_file_id, findex, name, canonical_name, total_lines,
               complexity, numPairsOrPaths));
         }
      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastInstrumentedFile> getAllInstrumentedFiles() throws OseeCoreException {
      Collection<VCastInstrumentedFile> toReturn = new ArrayList<VCastInstrumentedFile>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM instrumented_files if");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer source_file_id = stmt.getInt("source_file_id");
            Integer project_id = stmt.getInt("project_id");
            Integer unit_index = stmt.getInt("unit_index");
            Integer coverage_type = stmt.getInt("coverage_type");
            String LIS_file = stmt.getString("LIS_file");
            Integer checksum = stmt.getInt("checksum");
            toReturn.add(new VCastInstrumentedFile(id, source_file_id, project_id, unit_index,
               VCastCoverageType.valueOf(coverage_type), LIS_file, checksum));
         }
      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastMcdcCoverage> getAllMcdcCoverages() throws OseeCoreException {
      Collection<VCastMcdcCoverage> toReturn = new ArrayList<VCastMcdcCoverage>();
      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM mcdc_coverage");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer function_id = stmt.getInt("function_id");
            Integer line = stmt.getInt("line");
            Integer source_line = stmt.getInt("source_line");
            Integer num_conditions = stmt.getInt("num_conditions");
            String actual_expr = stmt.getString("actual_expr");
            String simplified_expr = stmt.getString("simplified_expr");
            toReturn.add(new VCastMcdcCoverage(id, function_id, line, source_line, num_conditions, actual_expr,
               simplified_expr));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastMcdcCoverageCondition> getAllMcdcCoverageConditions() throws OseeCoreException {
      Collection<VCastMcdcCoverageCondition> toReturn = new ArrayList<VCastMcdcCoverageCondition>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM mcdc_coverage_conditions");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer mcdc_id = stmt.getInt("mcdc_id");
            Integer cond_index = stmt.getInt("cond_index");
            Integer true_count = stmt.getInt("true_count");
            Integer false_count = stmt.getInt("false_count");
            Integer max_true_count = stmt.getInt("max_true_count");
            Integer max_false_count = stmt.getInt("max_false_count");
            String cond_variable = stmt.getString("cond_variable");
            String cond_expr = stmt.getString("cond_expr");
            toReturn.add(new VCastMcdcCoverageCondition(id, mcdc_id, cond_index, true_count, false_count,
               max_true_count, max_false_count, cond_variable, cond_expr));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastMcdcCoveragePairRow> getAllMcdcCoveragePairRows() throws OseeCoreException {
      Collection<VCastMcdcCoveragePairRow> toReturn = new ArrayList<VCastMcdcCoveragePairRow>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM mcdc_coverage_pair_rows");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer mcdc_id = stmt.getInt("mcdc_id");
            Integer row_value = stmt.getInt("row_value");
            Integer row_result = stmt.getInt("row_result");
            Integer hit_count = stmt.getInt("hit_count");
            Integer max_hit_count = stmt.getInt("max_hit_count");
            toReturn.add(new VCastMcdcCoveragePairRow(id, mcdc_id, row_value, row_result, hit_count, max_hit_count));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastMcdcCoveragePair> getAllMcdcCoveragePairs() throws OseeCoreException {
      Collection<VCastMcdcCoveragePair> toReturn = new ArrayList<VCastMcdcCoveragePair>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM mcdc_coverage_pairs");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer mcdc_cond_id = stmt.getInt("mcdc_cond_id");
            Integer pair_row1 = stmt.getInt("pair_row1");
            Integer pair_row2 = stmt.getInt("pair_row2");
            toReturn.add(new VCastMcdcCoveragePair(id, mcdc_cond_id, pair_row1, pair_row2));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastMcdcData> getAllMcdcData() throws OseeCoreException {
      Collection<VCastMcdcData> toReturn = new ArrayList<VCastMcdcData>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM mcdc_data");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer mcdc_id = stmt.getInt("mcdc_id");
            Integer result_id = stmt.getInt("result_id");
            Integer result_line = stmt.getInt("result_line");
            Integer pair_value = stmt.getInt("pair_value");
            Integer used_value = stmt.getInt("used_value");
            toReturn.add(new VCastMcdcData(id, mcdc_id, result_id, result_line, pair_value, used_value));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastMcdcDataCondition> getAllMcdcDataConditions() throws OseeCoreException {
      Collection<VCastMcdcDataCondition> toReturn = new ArrayList<VCastMcdcDataCondition>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM mcdc_data_conditions");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer mcdc_data_id = stmt.getInt("mcdc_data_id");
            Integer cond_index = stmt.getInt("cond_index");
            Boolean cond_value = stmt.getBoolean("cond_value");
            toReturn.add(new VCastMcdcDataCondition(id, mcdc_data_id, cond_index, cond_value));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastProjectFile> getAllProjectFiles() throws OseeCoreException {
      Collection<VCastProjectFile> toReturn = new ArrayList<VCastProjectFile>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM project_files");
         while (stmt.next()) {
            Integer project_id = stmt.getInt("project_id");
            Integer source_file_id = stmt.getInt("source_file_id");
            Integer instrumented_file_id = stmt.getInt("instrumented_file_id");
            Integer timestamp = stmt.getInt("timestamp");
            String build_md5sum = stmt.getString("build_md5sum");
            toReturn.add(new VCastProjectFile(project_id, source_file_id, instrumented_file_id, timestamp, build_md5sum));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastProject> getAllProjects() throws OseeCoreException {
      Collection<VCastProject> toReturn = new ArrayList<VCastProject>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM projects");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            String name = stmt.getString("name");
            String path = stmt.getString("path");
            toReturn.add(new VCastProject(id, name, path));
         }
      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastResult> getAllResults() throws OseeCoreException {
      Collection<VCastResult> toReturn = new ArrayList<VCastResult>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM results");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            String name = stmt.getString("name");
            Integer project_id = stmt.getInt("project_id");
            String path = stmt.getString("path");
            String fullname = stmt.getString("fullname");
            boolean enabled = stmt.getBoolean("enabled");
            boolean imported = stmt.getBoolean("imported");
            toReturn.add(new VCastResult(id, name, project_id, path, fullname, enabled, imported));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastSetting> getAllSettings() throws OseeCoreException {
      Collection<VCastSetting> toReturn = new ArrayList<VCastSetting>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM settings");
         while (stmt.next()) {
            String setting = stmt.getString("setting");
            String value = stmt.getString("value");
            toReturn.add(new VCastSetting(setting, value));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastSourceFile> getAllSourceFiles() throws OseeCoreException {
      Collection<VCastSourceFile> toReturn = new ArrayList<VCastSourceFile>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM source_files");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            String path = stmt.getString("path");
            String display_name = stmt.getString("display_name");
            Integer checksum = stmt.getInt("checksum");
            String display_path = stmt.getString("display_path");
            toReturn.add(new VCastSourceFile(id, path, display_name, checksum, display_path));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastStatementCoverage> getAllStatementCoverages() throws OseeCoreException {
      Collection<VCastStatementCoverage> toReturn = new ArrayList<VCastStatementCoverage>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM statement_coverage");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer function_id = stmt.getInt("function_id");
            Integer line = stmt.getInt("line");
            Integer hit_count = stmt.getInt("hit_count");
            Integer max_hit_count = stmt.getInt("max_hit_count");
            toReturn.add(new VCastStatementCoverage(id, function_id, line, hit_count, max_hit_count));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastStatementData> getAllStatementData() throws OseeCoreException {
      Collection<VCastStatementData> toReturn = new ArrayList<VCastStatementData>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM statement_data");
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer statement_id = stmt.getInt("statement_id");
            Integer result_id = stmt.getInt("result_id");
            Integer result_line = stmt.getInt("result_line");
            Boolean hit = stmt.getBoolean("hit");
            toReturn.add(new VCastStatementData(id, statement_id, result_id, result_line, hit));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public VCastVersion getVersion() throws OseeCoreException {
      VCastVersion toReturn = null;

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM version");
         if (stmt.next()) {
            Integer vestmtion = stmt.getInt("version");
            String date_created = stmt.getString("date_created");
            toReturn = new VCastVersion(vestmtion, date_created);
         }
      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public VCastWritable getWritable() throws OseeCoreException {
      VCastWritable toReturn = null;

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM writable");
         if (stmt.next()) {
            Integer is_writable = stmt.getInt("is_writable");
            toReturn = new VCastWritable(is_writable);
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public VCastSourceFileJoin getSourceFileJoin(VCastInstrumentedFile instrumentedFile) throws OseeCoreException {
      VCastSourceFileJoin toReturn = null;

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery(
            "SELECT sf.id, sf.path, sf.display_name, sf.checksum, sf.display_path, ifs.unit_index FROM source_files sf join instrumented_files ifs WHERE  sf.id = ifs.source_file_id AND sf.id=?",
            instrumentedFile.getSourceFileId());
         if (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer unit_index = stmt.getInt("unit_index");
            String path = stmt.getString("path");
            String display_name = stmt.getString("display_name");
            Integer checksum = stmt.getInt("checksum");
            String display_path = stmt.getString("display_path");
            toReturn = new VCastSourceFileJoin(id, path, display_name, checksum, display_path, unit_index);
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastFunction> getFunctions(VCastInstrumentedFile instrumentedFile) throws OseeCoreException {
      Collection<VCastFunction> toReturn = new ArrayList<VCastFunction>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM functions f WHERE instrumented_file_id=?", instrumentedFile.getId());
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer findex = stmt.getInt("findex");
            String name = stmt.getString("name");
            String canonical_name = stmt.getString("canonical_name");
            Integer total_lines = stmt.getInt("total_lines");
            Integer complexity = stmt.getInt("complexity");
            Integer numPairsOrPaths = stmt.getInt("num_pairs_or_paths");
            toReturn.add(new VCastFunction(id, instrumentedFile.getId(), findex, name, canonical_name, total_lines,
               complexity, numPairsOrPaths));

         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastStatementCoverage> getStatementCoverageLines(VCastFunction function) throws OseeCoreException {
      Collection<VCastStatementCoverage> toReturn = new ArrayList<VCastStatementCoverage>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM statement_coverage sc WHERE function_id=?", function.getId());
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer line = stmt.getInt("line");
            Integer hit_count = stmt.getInt("hit_count");
            Integer max_hit_count = stmt.getInt("max_hit_count");
            toReturn.add(new VCastStatementCoverage(id, function.getId(), line, hit_count, max_hit_count));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastStatementData> getStatementData(VCastStatementCoverage statementCoverage) throws OseeCoreException {
      Collection<VCastStatementData> toReturn = new ArrayList<VCastStatementData>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM statement_data WHERE statement_id=?", statementCoverage.getId());
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            Integer statement_id = stmt.getInt("statement_id");
            Integer result_id = stmt.getInt("result_id");
            Integer result_line = stmt.getInt("result_line");
            Boolean hit = stmt.getBoolean("hit");
            toReturn.add(new VCastStatementData(id, statement_id, result_id, result_line, hit));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

   @Override
   public Collection<VCastResult> getResults(VCastStatementData statementDataItem) throws OseeCoreException {
      Collection<VCastResult> toReturn = new ArrayList<VCastResult>();

      IOseeStatement stmt = getStatement();
      try {
         stmt.runPreparedQuery("SELECT * FROM results WHERE id=?", statementDataItem.getResultId());
         while (stmt.next()) {
            Integer id = stmt.getInt("id");
            String name = stmt.getString("name");
            Integer project_id = stmt.getInt("project_id");
            String path = stmt.getString("path");
            String fullname = stmt.getString("fullname");
            boolean enabled = stmt.getBoolean("enabled");
            boolean imported = stmt.getBoolean("imported");
            toReturn.add(new VCastResult(id, name, project_id, path, fullname, enabled, imported));
         }

      } finally {
         stmt.close();
      }
      return toReturn;
   }

}
