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
package org.eclipse.osee.ats.core.client.ev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.ev.IAtsWorkPackage;
import org.eclipse.osee.ats.api.util.ColumnAdapter;
import org.eclipse.osee.ats.api.util.ColumnType;
import org.eclipse.osee.ats.api.util.IColumn;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.util.PercentCompleteTotalUtil;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Donald G. Dunne
 */
public class EarnedValueReportOperation extends AbstractOperation {

   private final Collection<IAtsWorkPackage> workPackages;
   private final List<EarnedValueReportResult> results = new ArrayList<EarnedValueReportResult>();

   // @formatter:off
   public static final IColumn Work_Package_Id = new ColumnAdapter("ats.work.package.id", "Work Package Id", ColumnType.String, "");
   public static final IColumn Work_Package_Name = new ColumnAdapter("ats.work.package.name", "Work Package Name", ColumnType.String, "");
   public static final IColumn Work_Package_Percent = new ColumnAdapter("ats.work.package.percent", "Work Package Percent", ColumnType.Percent, "");
   public static final IColumn Related_Action_Percent = new ColumnAdapter("ats.related.action.percent", "Related Action Percent", ColumnType.Percent, "");
   public static final IColumn Related_Action_Id = new ColumnAdapter("ats.related.action.id", "Related Action Id", ColumnType.String, "");
   public static final IColumn Related_Action_Name = new ColumnAdapter("ats.related.action.name", "Related Action Name", ColumnType.String, "");
   public static final IColumn Related_Action_Type = new ColumnAdapter("ats.related.action.type", "Related Action Type", ColumnType.String, "");
   public static final IColumn Related_Action_Guid = new ColumnAdapter("ats.related.action.guid", "Related Action Guid", ColumnType.String, "");
   public static final IColumn Work_Package_Guid = new ColumnAdapter("ats.work.package.guid", "Related Work Package Guid", ColumnType.String, "");
   // @formatter:on

   public static List<IColumn> columns = Arrays.asList(Work_Package_Id, Work_Package_Name, Work_Package_Percent,
      Related_Action_Percent, Related_Action_Id, Related_Action_Name, Related_Action_Type, Related_Action_Guid,
      Work_Package_Guid);

   public EarnedValueReportOperation(String operationName, Collection<IAtsWorkPackage> workPackages) {
      super(operationName, Activator.PLUGIN_ID);
      this.workPackages = workPackages;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws OseeCoreException {
      if (workPackages.isEmpty()) {
         throw new OseeArgumentException("ERROR", "Must provide Work Packages");
      }

      for (IAtsWorkPackage workPkg : workPackages) {
         checkForCancelledStatus(monitor);
         for (Artifact art : ArtifactQuery.getArtifactListFromAttribute(AtsAttributeTypes.WorkPackageGuid,
            workPkg.getGuid(), AtsUtilCore.getAtsBranch())) {
            checkForCancelledStatus(monitor);
            AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) art;
            EarnedValueReportResult result = new EarnedValueReportResult(workPkg, art);
            results.add(result);
            result.setValue(Work_Package_Id, workPkg.getWorkPackageId());
            result.setValue(Work_Package_Name, workPkg.getName());
            result.setValue(Work_Package_Percent, String.valueOf(workPkg.getWorkPackagePercent()));
            result.setValue(Work_Package_Guid, String.valueOf(workPkg.getGuid()));
            result.setValue(Related_Action_Id, getActionId(art));
            result.setValue(Related_Action_Name, art.getName());
            result.setValue(Related_Action_Type, awa.getParentTeamWorkflow().getTeamDefinition().getName());
            result.setValue(Related_Action_Percent, String.valueOf(PercentCompleteTotalUtil.getPercentCompleteTotal(
               (IAtsWorkItem) art, AtsClientService.get().getServices())));
            result.setValue(Related_Action_Guid, art.getGuid());
         }
      }

   }

   private String getActionId(Artifact art) throws OseeCoreException {
      String pcrId = art.getSoleAttributeValue(AtsAttributeTypes.LegacyPcrId, null);
      if (pcrId == null && art instanceof IAtsWorkItem) {
         pcrId = ((IAtsWorkItem) art).getAtsId();
      } else if (pcrId == null && art instanceof IAtsObject) {
         pcrId = ((IAtsObject) art).getGuid();
      }
      return pcrId;
   }

   public List<EarnedValueReportResult> getResults() {
      return results;
   }

}
