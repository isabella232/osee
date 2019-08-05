package org.eclipse.osee.ats.core.task.internal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.demo.DemoWorkDefinitions;
import org.eclipse.osee.ats.api.task.create.CreateTasksDefinitionBuilder;
import org.eclipse.osee.ats.api.task.create.IAtsTaskSetDefinitionProvider;
import org.eclipse.osee.ats.api.workdef.RuleEventType;
import org.eclipse.osee.ats.api.workdef.StateToken;
import org.eclipse.osee.ats.core.task.DemoTaskSetDefinitionTokens;

/**
 * @author Donald G. Dunne
 */
public class AtsTaskSetDefinitionProvider implements IAtsTaskSetDefinitionProvider {

   @Override
   public Collection<CreateTasksDefinitionBuilder> getTaskSetDefinitions() {
      List<CreateTasksDefinitionBuilder> taskSets = new LinkedList<>();
      taskSets.add(new CreateTasksDefinitionBuilder(DemoTaskSetDefinitionTokens.SawSwDesignTestingChecklist) //
         .andEventType(RuleEventType.Manual) //
         .andTask("1. Run unit tests", "desc", null) //
         .andTask("2. Run integration testsk", "desc2", StateToken.Implement) //
         .andTask("3. Run manual tests", DemoWorkDefinitions.WorkDef_Task_Demo_SwDesign) //
         .andTask("4. Complete testing action")); //

      taskSets.add(new CreateTasksDefinitionBuilder(DemoTaskSetDefinitionTokens.SawSwDesignProcessChecklist) //
         .andEventType(RuleEventType.Manual) //
         .andTask("1. Review processes", "desc", null) //
         .andTask("2. Review work instruction", "desc2", StateToken.Implement) //
         .andTask("3. Consult Mentor", DemoWorkDefinitions.WorkDef_Task_Demo_SwDesign) //
         .andTask("4. Complete process action")); //
      return taskSets;
   }
}
