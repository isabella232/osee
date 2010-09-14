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

package org.eclipse.osee.ats.util.Import;

import java.io.FileFilter;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.ats.artifact.StateMachineArtifact.TransitionOption;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.artifact.TaskableStateMachineArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsNotifyUsers;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ExcelSaxHandler;
import org.eclipse.osee.framework.jdk.core.util.io.xml.RowProcessor;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Donald G. Dunne
 */
public class ExcelAtsTaskArtifactExtractor {

   private final StateMachineArtifact sma;
   private final boolean emailPOCs;
   private final SkynetTransaction transaction;

   private IProgressMonitor monitor;

   public ExcelAtsTaskArtifactExtractor(TeamWorkFlowArtifact artifact, boolean emailPOCs, SkynetTransaction transaction) {
      this.emailPOCs = emailPOCs;
      this.transaction = transaction;
      this.sma = artifact;
   }

   public void process(URI source) throws OseeCoreException {
      try {
         XMLReader xmlReader = XMLReaderFactory.createXMLReader();
         IProgressMonitor monitor = getMonitor();
         if (monitor == null) {
            monitor = new NullProgressMonitor();
         }
         xmlReader.setContentHandler(new ExcelSaxHandler(
            new InternalRowProcessor(monitor, transaction, sma, emailPOCs), true));
         xmlReader.parse(new InputSource(new InputStreamReader(source.toURL().openStream(), "UTF-8")));
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   public String getDescription() {
      return "Extract each row as a task";
   }

   public IProgressMonitor getMonitor() {
      return monitor;
   }

   public void setMonitor(IProgressMonitor monitor) {
      this.monitor = monitor;
   }

   public FileFilter getFileFilter() {
      return null;
   }

   public String getName() {
      return "Excel Ats Tasks";
   }

   private final static class InternalRowProcessor implements RowProcessor {
      private String[] headerRow;
      private int rowNum;
      private final IProgressMonitor monitor;
      private final StateMachineArtifact sma;
      private final SkynetTransaction transaction;
      private final boolean emailPOCs;

      protected InternalRowProcessor(IProgressMonitor monitor, SkynetTransaction transaction, StateMachineArtifact sma, boolean emailPOCs) {
         this.monitor = monitor;
         this.transaction = transaction;
         this.emailPOCs = emailPOCs;
         this.sma = sma;
      }

      @Override
      public void processEmptyRow() {
         // do nothing
      }

      @Override
      public void processCommentRow(String[] row) {
         // do nothing
      }

      @Override
      public void reachedEndOfWorksheet() {
         // do nothing
      }

      @Override
      public void detectedRowAndColumnCounts(int rowCount, int columnCount) {
         // do nothing
      }

      @Override
      public void foundStartOfWorksheet(String sheetName) {
         // do nothing
      }

      @Override
      public void processHeaderRow(String[] headerRow) {
         this.headerRow = headerRow.clone();
      }

      @Override
      public void processRow(String[] row) {
         try {
            rowNum++;
            monitor.setTaskName("Processing Row " + rowNum);
            TaskArtifact taskArt = ((TaskableStateMachineArtifact) sma).createNewTask("");

            monitor.subTask("Validating...");
            boolean fullRow = false;
            for (int i = 0; i < row.length; i++) {
               if (Strings.isValid(row[i])) {
                  fullRow = true;
                  break;
               }
            }
            if (!fullRow) {
               OseeLog.log(AtsPlugin.class, Level.SEVERE, "Empty Row Found => " + rowNum + " skipping...");
               return;
            }
            AtsUtil.setEmailEnabled(false);
            for (int i = 0; i < row.length; i++) {
               if (headerRow[i] == null) {
                  OseeLog.log(AtsPlugin.class, Level.SEVERE, "Null header column => " + i);
               } else if (headerRow[i].equalsIgnoreCase("Originator")) {
                  String userName = row[i];
                  User u = null;
                  if (!Strings.isValid(userName)) {
                     u = UserManager.getUser();
                  } else {
                     u = UserManager.getUserByName(userName);
                  }
                  if (u == null) {
                     OseeLog.log(AtsPlugin.class, Level.SEVERE, String.format(
                        "Invalid Originator \"%s\" for row %d\nSetting to current user.", userName, rowNum));
                  }
                  taskArt.getLog().setOriginator(u);
               } else if (headerRow[i].equalsIgnoreCase("Assignees")) {
                  Set<User> assignees = new HashSet<User>();
                  for (String userName : row[i].split(";")) {
                     userName = userName.replaceAll("^\\s+", "");
                     userName = userName.replaceAll("\\+$", "");
                     User user = null;
                     if (!Strings.isValid(userName)) {
                        user = UserManager.getUser();
                     } else {
                        try {
                           user = UserManager.getUserByName(userName);
                        } catch (OseeCoreException ex) {
                           OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
                        }
                     }
                     if (user == null) {
                        OseeLog.log(AtsPlugin.class, Level.SEVERE,
                           String.format("Invalid Assignee \"%s\" for row %d.  Using current user.", userName, rowNum));
                        user = UserManager.getUser();
                     }
                     assignees.add(user);
                  }
                  taskArt.getStateMgr().setAssignees(assignees);
               } else if (headerRow[i].equalsIgnoreCase("Resolution")) {
                  String str = row[i];
                  if (Strings.isValid(str)) {
                     taskArt.setSoleAttributeValue(AtsAttributeTypes.Resolution, str);
                  }
               } else if (headerRow[i].equalsIgnoreCase("Description")) {
                  String str = row[i];
                  if (Strings.isValid(str)) {
                     taskArt.setSoleAttributeValue(AtsAttributeTypes.Description, str);
                  }
               } else if (headerRow[i].equalsIgnoreCase("Related to State")) {
                  String str = row[i];
                  if (Strings.isValid(str)) {
                     taskArt.setSoleAttributeValue(AtsAttributeTypes.RelatedToState, str);
                  }
               } else if (headerRow[i].equalsIgnoreCase("Notes")) {
                  String str = row[i];
                  if (Strings.isValid(str)) {
                     taskArt.setSoleAttributeValue(AtsAttributeTypes.SmaNote, str);
                  }
               } else if (headerRow[i].equalsIgnoreCase("Title")) {
                  String str = row[i];
                  if (Strings.isValid(str)) {
                     monitor.subTask(String.format("Title \"%s\"", str));
                     taskArt.setName(str);
                  }
               } else if (headerRow[i].equalsIgnoreCase("Percent Complete")) {
                  String str = row[i];
                  Double percent;
                  if (Strings.isValid(str)) {
                     try {
                        percent = new Double(str);
                        if (percent < 1) {
                           percent = percent * 100;
                        }
                     } catch (Exception ex) {
                        throw new OseeArgumentException("Invalid Percent Complete \"%s\" for row %d",
                           str, rowNum);
                     }
                     int percentInt = percent.intValue();
                     sma.getStateMgr().updateMetrics(0, percentInt, true);
                  }
               } else if (headerRow[i].equalsIgnoreCase("Hours Spent")) {
                  String str = row[i];
                  double hours = 0;
                  if (Strings.isValid(str)) {
                     try {
                        hours = new Double(str);
                     } catch (Exception ex) {
                        throw new OseeArgumentException("Invalid Hours Spent \"%s\" for row %d", str,
                           rowNum);
                     }
                     sma.getStateMgr().updateMetrics(hours, sma.getStateMgr().getPercentComplete(), true);
                  }
               } else if (headerRow[i].equalsIgnoreCase("Estimated Hours")) {
                  String str = row[i];
                  double hours = 0;
                  if (Strings.isValid(str)) {
                     try {
                        hours = new Double(str);
                     } catch (Exception ex) {
                        throw new OseeArgumentException("Invalid Estimated Hours \"%s\" for row %d", str,
                           rowNum);
                     }
                     taskArt.setSoleAttributeValue(AtsAttributeTypes.EstimatedHours, hours);
                  }
               } else {
                  OseeLog.log(AtsPlugin.class, Level.SEVERE, "Unhandled column => " + headerRow[i]);
               }
            }
            AtsUtil.setEmailEnabled(true);
            if (taskArt.isCompleted()) {
               taskArt.transitionToCompleted(0, transaction, TransitionOption.None);
            }
            // always persist
            taskArt.persist(transaction);
            if (emailPOCs && !taskArt.isCompleted() && !taskArt.isCancelled()) {
               AtsNotifyUsers.getInstance().notify(sma, AtsNotifyUsers.NotifyType.Assigned);
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         }
      }
   }
}