/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.editor.log.column;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.client.workflow.AtsWorkStateFactory;
import org.eclipse.osee.ats.core.client.workflow.log.LogItem;
import org.eclipse.osee.ats.core.model.IAtsUser;
import org.eclipse.osee.ats.core.model.impl.WorkStateImpl;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.core.workdef.AtsWorkDefinitionService;
import org.eclipse.osee.ats.editor.history.column.EventColumn;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.workdef.api.IAtsStateDefinition;
import org.eclipse.osee.ats.workdef.api.StateType;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

public class LogEventColumn extends XViewerValueColumn {

   private static LogEventColumn instance = new LogEventColumn();

   public static LogEventColumn getInstance() {
      return instance;
   }

   public LogEventColumn() {
      super("ats.log.Event", "Event", 115, SWT.LEFT, true, SortDataType.String, false, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public LogEventColumn copy() {
      LogEventColumn newXCol = new LogEventColumn();
      copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof String) {
            return (String) element;
         }
         if (element instanceof LogItem) {
            return ((LogItem) element).getType().name();
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return LogUtil.getCellExceptionString(ex);
      }
      return "";
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn col, int columnIndex) {
      if (col.getName().equals("Event")) {
         String text = getColumnText(element, LogEventColumn.getInstance(), columnIndex);
         if (text.startsWith("Assigned") || text.equals("UnAssigned")) {
            return ImageManager.getImage(FrameworkImage.USERS);
         } else if (text.startsWith("Statused")) {
            return ImageManager.getImage(FrameworkImage.GREEN_PLUS);
         } else if (text.startsWith("Transition")) {
            return ImageManager.getImage(AtsImage.TRANSITION);
         } else if (text.startsWith("Created")) {
            return ImageManager.getImage(AtsImage.ACTION);
         } else if (text.startsWith("Completed")) {
            return ImageManager.getImage(FrameworkImage.DOT_GREEN);
         } else if (text.startsWith("Cancelled")) {
            return ImageManager.getImage(FrameworkImage.X_RED);
         }
      }
      return null;
   }

   public String processCurrentStateChange(Change change) {
      try {
         IAtsStateDefinition stateDef = AtsWorkDefinitionService.getService().createStateDefinition("");
         stateDef.setStateType(StateType.Working);
         WorkStateImpl was = AtsWorkStateFactory.getFromXml(change.getWasValue());
         WorkStateImpl is = AtsWorkStateFactory.getFromXml(change.getIsValue());
         if (change.getWasValue().equals("")) {
            return "Created in [" + is.getName() + "] state";
         } else if (!was.getName().equals(is.getName())) {
            return "Transition from [" + was.getName() + "] to [" + is.getName() + "]";
         }
         if (was.getName().equals(is.getName()) && (was.getPercentComplete() != is.getPercentComplete() || !EventColumn.getHoursSpentStr(
            was).equals(EventColumn.getHoursSpentStr(is)))) {
            return "Statused [" + is.getName() + "] to: " + is.getPercentComplete() + "% and " + is.getHoursSpent() + " hrs";
         }
         Collection<? extends IAtsUser> wasAssignees = was.getAssignees();
         Collection<? extends IAtsUser> isAssignees = is.getAssignees();
         Set<IAtsUser> assigned = new HashSet<IAtsUser>();
         Set<IAtsUser> unAssigned = new HashSet<IAtsUser>();
         for (IAtsUser isAssignee : isAssignees) {
            if (!wasAssignees.contains(isAssignee)) {
               assigned.add(isAssignee);
            }
         }
         for (IAtsUser wasAssignee : wasAssignees) {
            if (!isAssignees.contains(wasAssignee)) {
               unAssigned.add(wasAssignee);
            }
         }
         if (unAssigned.size() > 0) {
            return "UnAssigned [" + is.getName() + "] removed " + AtsObjects.toString("; ", unAssigned);
         }
         if (assigned.size() > 0) {
            return "Assigned [" + is.getName() + "] to " + AtsObjects.toString("; ", assigned);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return "";
   }

}
