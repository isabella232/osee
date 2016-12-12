package org.eclipse.osee.ats.core.workflow.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.notify.AtsNotificationEventFactory;
import org.eclipse.osee.ats.api.notify.AtsNotifyType;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.util.IAtsUtilService;
import org.eclipse.osee.ats.api.util.ISequenceProvider;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAttribute;
import org.eclipse.osee.ats.api.workflow.log.IAtsLog;
import org.eclipse.osee.ats.api.workflow.log.LogType;
import org.eclipse.osee.ats.core.util.AtsCoreFactory;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * Duplicate Workflow including all fields and, states.
 *
 * @author Donald G. Dunne
 */
public class DuplicateWorkflowAsIsOperation extends AbstractDuplicateWorkflowOperation {

   private final boolean duplicateTasks;
   private List<AttributeTypeId> excludeTypes;
   private static String ATS_CONFIG_EXCLUDE_DUPLICATE_TYPE_IDS_KEY =
      "DuplicateWorkflowAsIsOperation_ExcludeAttrTypeIds";

   public DuplicateWorkflowAsIsOperation(Collection<IAtsTeamWorkflow> teamWfs, boolean duplicateTasks, String title, IAtsUser asUser, IAtsServices services) {
      super(teamWfs, title, asUser, services);
      this.duplicateTasks = duplicateTasks;
   }

   @Override
   public XResultData run() throws OseeCoreException {
      XResultData results = validate();
      if (results.isErrors()) {
         return results;
      }
      oldToNewMap = new HashMap<>();

      IAtsChangeSet changes = services.getStoreService().createAtsChangeSet("Duplicate Workflow - As-Is", asUser);

      for (IAtsTeamWorkflow teamWf : teamWfs) {

         IAtsWorkItem newTeamWf = duplicateWorkItem(changes, teamWf);

         // add notification for originator, assigned and subscribed
         changes.getNotifications().addWorkItemNotificationEvent(
            AtsNotificationEventFactory.getWorkItemNotificationEvent(asUser, newTeamWf, AtsNotifyType.Originator,
               AtsNotifyType.Assigned, AtsNotifyType.SubscribedTeamOrAi));

         if (duplicateTasks) {
            for (IAtsTask task : services.getTaskService().getTask(teamWf)) {
               IAtsTask dupTaskArt = (IAtsTask) duplicateWorkItem(changes, task);
               dupTaskArt.getLog().addLog(LogType.Note, null, "Task duplicated from " + task.getAtsId(),
                  services.getUserService().getCurrentUser().getUserId());
               changes.relate(newTeamWf.getStoreObject(), AtsRelationTypes.TeamWfToTask_Task, dupTaskArt);
               // for tasks, add notification for subscribed only
               changes.getNotifications().addWorkItemNotificationEvent(
                  AtsNotificationEventFactory.getWorkItemNotificationEvent(asUser, dupTaskArt,
                     AtsNotifyType.SubscribedTeamOrAi));
               changes.add(dupTaskArt);
            }
         }

         oldToNewMap.put(teamWf, (IAtsTeamWorkflow) newTeamWf);

      }

      changes.execute();
      return results;
   }

   private IAtsWorkItem duplicateWorkItem(IAtsChangeSet changes, IAtsWorkItem workItem) {
      ArtifactId newWorkItemArt = changes.createArtifact(
         services.getStoreService().getArtifactType(workItem.getStoreObject()), getTitle(workItem));
      changes.setSoleAttributeFromString(newWorkItemArt, AtsAttributeTypes.AtsId, getNexAtsId(workItem));

      if (workItem.isTeamWorkflow()) {
         changes.relate(newWorkItemArt, AtsRelationTypes.ActionToWorkflow_Action, workItem.getParentAction());
      }
      IAtsLog atsLog = AtsCoreFactory.getLogFactory().getLogLoaded(workItem, services.getAttributeResolver());
      atsLog.addLog(LogType.Note, null, "Workflow duplicated from " + workItem.getAtsId(), asUser.getUserId());

      // assignees == add in existing assignees, leads and originator (current user)
      List<IAtsUser> assignees = new LinkedList<>();
      assignees.addAll(workItem.getStateMgr().getAssignees());
      if (workItem.isTeamWorkflow()) {
         IAtsTeamWorkflow teamWf = (IAtsTeamWorkflow) workItem;
         assignees.addAll(((IAtsTeamWorkflow) workItem).getTeamDefinition().getLeads());
         if (!assignees.contains(asUser)) {
            assignees.add(asUser);
         }
         // Auto-add actions to configured goals
         services.getActionFactory().addActionToConfiguredGoal(teamWf.getTeamDefinition(), teamWf,
            teamWf.getActionableItems(), changes);
      }

      for (IAttribute<Object> attr : services.getAttributeResolver().getAttributes(workItem.getStoreObject())) {
         if (!getExcludeTypes().contains(attr.getAttrType())) {
            changes.addAttribute(newWorkItemArt, attr.getAttrType(), attr.getValue());
         }
      }
      return services.getWorkItemFactory().getWorkItem(newWorkItemArt);
   }

   private List<AttributeTypeId> getExcludeTypes() {
      if (excludeTypes == null) {
         excludeTypes = new LinkedList<>();
         excludeTypes.add(AtsAttributeTypes.AtsId);
         excludeTypes.add(CoreAttributeTypes.Name);
         String value = services.getConfigValue(ATS_CONFIG_EXCLUDE_DUPLICATE_TYPE_IDS_KEY);
         if (Strings.isValid(value)) {
            for (String attrTypeId : value.split(";")) {
               if (Strings.isNumeric(attrTypeId)) {
                  AttributeTypeId attributeType = AttributeTypeId.valueOf(attrTypeId);
                  if (attributeType != null) {
                     excludeTypes.add(attributeType);
                  } else {
                     OseeLog.log(DuplicateWorkflowAsIsOperation.class, Level.SEVERE,
                        String.format("Can't resolve Attribute Type for id %d in AtsConfig.%s", attrTypeId,
                           ATS_CONFIG_EXCLUDE_DUPLICATE_TYPE_IDS_KEY));
                  }
               } else {
                  OseeLog.log(DuplicateWorkflowAsIsOperation.class, Level.SEVERE,
                     String.format("Can't resolve non-numeric Attribute Type for id %s in AtsConfig.%s", attrTypeId,
                        ATS_CONFIG_EXCLUDE_DUPLICATE_TYPE_IDS_KEY));
               }
            }
         }
      }
      return excludeTypes;
   }

   private String getNexAtsId(IAtsWorkItem workItem) {
      IAtsUtilService utilService = services.getUtilService();
      ISequenceProvider sequenceProvider = services.getSequenceProvider();

      IAtsTeamDefinition teamDefinition = workItem.getParentTeamWorkflow().getTeamDefinition();
      String nextAtsId = utilService.getNextAtsId(sequenceProvider, null, teamDefinition);

      return nextAtsId;
   }

}