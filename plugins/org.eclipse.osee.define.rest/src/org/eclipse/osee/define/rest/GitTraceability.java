/*******************************************************************************
 * Copyright (c) 2018 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.define.rest;

import static org.eclipse.osee.framework.core.enums.CoreArtifactTokens.BaselineEventsFolder;
import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.CertificationBaselineEvent;
import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.CodeUnit;
import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.GitCommit;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.BaselinedBy;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.BaselinedTimestamp;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.GitChangeId;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.ReviewId;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.ReviewStoryId;
import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import static org.eclipse.osee.framework.core.enums.CoreTupleTypes.GitCommitFile;
import static org.eclipse.osee.framework.core.enums.CoreTupleTypes.GitLatest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.define.api.CertBaselineData;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.UserId;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.search.TupleQuery;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;

/**
 * @author Ryan D. Brooks
 */
public final class GitTraceability {
   private static final SimpleDateFormat filenameDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
   //   sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

   private static final Pattern mergePattern = Pattern.compile("Merge: \\w+ \\w+");
   private static final Pattern delimiterPattern = Pattern.compile("\\n+");
   private static final String endShowMarker = "GitShowEndsHere";
   private static final Pattern commitShaPattern = Pattern.compile("commit (\\w+)");
   private static final Pattern authorPattern = Pattern.compile("Author:\\s+(.*)");
   private static final Pattern datePattern = Pattern.compile("Date:\\s+(.*)");
   private static final Pattern signedOffPattern = Pattern.compile("\\s*Signed-off-by:\\s+(.*)");
   private static final Pattern changeIdPattern = Pattern.compile("\\s+Change-Id: (I\\w{40})");
   /*
    * must support rename case "R093    oldPath     newPath" also must support files with spaces (not consecutive) in
    * name A .gitignore
    */
   private static final Pattern filePattern = Pattern.compile("([CRM][01]\\d{2}|[ACDMRTUXB]{1,2})\\t([^\\t]+)\\t?(.*)");
   private final Matcher commitShaMatcher = commitShaPattern.matcher("");
   private final Matcher authorMatcher = authorPattern.matcher("");
   private final Matcher dateMatcher = datePattern.matcher("");
   private final Matcher signedOffMatcher = signedOffPattern.matcher("");
   private final Matcher changeIdMatcher = changeIdPattern.matcher("");
   private final Matcher fileMatcher = filePattern.matcher("");
   private final OrcsApi orcsApi;
   private final QueryFactory queryFactory;
   private final TupleQuery tupleQuery;
   private final ArtifactId repository;
   private final BranchId branch;

   public GitTraceability(OrcsApi orcsApi, BranchId branch, ArtifactId repository) {
      this.orcsApi = orcsApi;
      this.queryFactory = orcsApi.getQueryFactory();
      this.tupleQuery = queryFactory.tupleQuery();
      this.branch = branch;
      this.repository = repository;
   }

   public GitTraceability(OrcsApi orcsApi, BranchId branch, String repositoryName) {
      this(orcsApi, branch, getRepo(orcsApi.getQueryFactory(), branch, repositoryName));
   }

   private static ArtifactId getRepo(QueryFactory queryFactory, BranchId branch, String repositoryName) {
      return queryFactory.fromBranch(branch).andNameEquals(repositoryName).andTypeEquals(
         CoreArtifactTypes.GitRepository).loadArtifactId();
   }

   public ArtifactId baselineFiles(CertBaselineData baselineData, UserId account) {
      TransactionBuilder tx =
         orcsApi.getTransactionFactory().createTransaction(branch, account, "rest - baseline  files");

      ArtifactId baselineEvent =
         tx.createArtifact(BaselineEventsFolder, CertificationBaselineEvent, baselineData.eventName);

      ArtifactId baselinedByUser = queryFactory.fromBranch(COMMON).and(CoreAttributeTypes.UserId,
         baselineData.baselinedByUserId).loadArtifactId();

      tx.addChild(repository, baselineEvent);

      ArtifactId baselineCommit = queryFactory.fromBranch(branch).and(GitChangeId, baselineData.changeId).andTypeEquals(
         GitCommit).loadArtifactId();

      tx.setSoleAttributeValue(baselineEvent, GitChangeId, baselineData.changeId);
      tx.setSoleAttributeValue(baselineEvent, BaselinedBy, baselinedByUser);
      tx.setSoleAttributeValue(baselineEvent, BaselinedTimestamp, new Date());
      if (baselineData.reviewId != null) {
         tx.setSoleAttributeValue(baselineEvent, ReviewId, baselineData.reviewId);
      }
      if (baselineData.reviewStoryId != null) {
         tx.setSoleAttributeValue(baselineEvent, ReviewStoryId, baselineData.reviewStoryId);
      }

      for (String path : baselineData.files) {
         ArtifactId codeUnit = loadCodeUnit(repository, branch, path);
         if (codeUnit.isInvalid()) {
            throw new OseeArgumentException("No code unit found for path [%s]", path);
         }
         tx.relate(baselineEvent, CoreRelationTypes.SupportingInfo_SupportedBy, codeUnit);
         updateLGitLatestTuple(tx, codeUnit, baselineCommit);
      }

      tx.commit();
      return baselineEvent;
   }

   private ArtifactId loadCodeUnit(ArtifactId repository, BranchId branch, String path) {
      List<ArtifactId> codeUnits =
         queryFactory.fromBranch(branch).andNameEquals(path).andTypeEquals(CodeUnit).loadArtifactIds();
      for (ArtifactId codeUnit : codeUnits) {
         //TODO: if CoreTupleTypes.GitCommitFile for this repository and path matches then return this codeUnit
         //tupleQuery.doesTuple3E3Exist(tupleType, e3);
         return codeUnit;
      }

      return ArtifactId.SENTINEL;
   }

   private void updateLGitLatestTuple(TransactionBuilder tx, ArtifactId codeUnit, ArtifactId baselineCommit) {
      ArtifactId[] commitWraper = new ArtifactId[1];
      tupleQuery.getTuple4E3E4FromE1E2(GitLatest, branch, repository, codeUnit,
         (changeCommit, ignore) -> commitWraper[0] = changeCommit);

      tx.deleteTuple4ByE1E2(GitLatest, repository, codeUnit);

      tx.addTuple4(GitLatest, repository, codeUnit, commitWraper[0], baselineCommit);
   }

   public void parseGitHistory(String gitHistory, BranchId branch, UserId account, HistoryImportStrategy importStrategy) {
      Scanner scanner = new Scanner(gitHistory);
      scanner.useDelimiter(delimiterPattern);

      while (scanner.hasNext()) {
         parseGitCommit(repository, scanner, branch, account, importStrategy);
      }
      importStrategy.finishImport();
   }

   private void parseGitCommit(ArtifactId repository, Scanner scanner, BranchId branch, UserId account, HistoryImportStrategy importStrategy) {
      TransactionBuilder tx = importStrategy.getTransactionBuilder(orcsApi, repository, branch, account);

      String commitSHA = parseLine(scanner, commitShaMatcher);
      System.out.println(commitSHA);

      if (scanner.hasNext(mergePattern)) {
         scanner.next();
      }
      String author = parseLine(scanner, authorMatcher);
      String dateString = parseLine(scanner, dateMatcher);
      Date authorDate;
      try {
         authorDate = filenameDateFormat.parse(dateString);
      } catch (ParseException ex) {
         throw OseeCoreException.wrap(ex);
      }

      StringBuilder commitMessage = new StringBuilder(400);

      String commitMessageFirstLine;
      if (scanner.hasNext(filePattern)) { // will find file pattern here if commit message is missing
         commitMessageFirstLine = "";
      } else {
         commitMessageFirstLine = scanner.next();
         commitMessage.append(commitMessageFirstLine);
      }

      ArtifactId commit = tx.createArtifact(GitCommit, commitMessageFirstLine);
      tx.setSoleAttributeValue(commit, CoreAttributeTypes.GitCommitSHA, commitSHA);
      tx.setSoleAttributeValue(commit, CoreAttributeTypes.UserArtifactId, SystemUser.OseeSystem); //TODO: this must convert author to the corresponding user artifact
      tx.setSoleAttributeValue(commit, CoreAttributeTypes.GitCommitAuthorDate, authorDate);

      parseFileChanges(repository, commitSHA, commit, scanner, branch, tx, commitMessage, importStrategy);

      tx.setSoleAttributeValue(commit, CoreAttributeTypes.GitCommitMessage, commitMessage);

      importStrategy.finishGitCommit(tx);
   }

   private void parseFileChanges(ArtifactId repository, String commitSHA, ArtifactId commit, Scanner scanner, BranchId branch, TransactionBuilder tx, StringBuilder commitMessage, HistoryImportStrategy importStrategy) {
      String commitId = commitSHA;
      while (scanner.hasNext()) {
         String line = scanner.next();
         if (line.equals(endShowMarker)) {
            return;
         } else if (fileMatcher.reset(line).matches()) {
            String changeType = fileMatcher.group(1);
            String path = fileMatcher.group(2);
            String newPath = fileMatcher.group(3);

            ArtifactId codeUnit = importStrategy.getCodeUnit(branch, tx, commitSHA, changeType, path, newPath);
            if (codeUnit.isValid()) {
               importStrategy.handleCodeUnit(branch, codeUnit, tx, repository, commit, changeType);
            }
         } else if (signedOffMatcher.reset(line).matches()) {
            String signer = signedOffMatcher.group(1);
         } else if (changeIdMatcher.reset(line).matches()) {
            commitId = changeIdMatcher.group(1);
            if (tupleQuery.doesTuple4E3Exist(GitCommitFile, commit)) {
               tx.abandon(); // don't save the same commit twice
            }
            tx.setSoleAttributeValue(commit, GitChangeId, commitId);
         } else {
            commitMessage.append(line);
            commitMessage.append('\n');
         }
      }
   }

   private String parseLine(Scanner scanner, Matcher matcher) {
      String line = scanner.next();
      if (matcher.reset(line).find()) {
         return matcher.group(1);
      } else {
         throw new OseeArgumentException("Did not match pattern: %s", matcher.pattern());
      }
   }
}