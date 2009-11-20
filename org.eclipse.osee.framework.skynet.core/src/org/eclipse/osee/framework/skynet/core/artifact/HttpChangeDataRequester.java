/*
 * Created on Nov 10, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.artifact;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.ChangeReportRequest;
import org.eclipse.osee.framework.core.data.ChangeReportResponse;
import org.eclipse.osee.framework.core.data.OseeServerContext;
import org.eclipse.osee.framework.core.enums.Function;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecord;

/**
 * @author Jeff C. Phillips
 */
public class HttpChangeDataRequester {

   public static ChangeReportResponse getChanges(TransactionRecord toTransactionRecord, TransactionRecord fromTransactionRecord, IProgressMonitor monitor, boolean isHistorical) throws OseeCoreException {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("function", Function.CHANGE_REPORT.name());

      ChangeReportRequest requestData =
            new ChangeReportRequest(toTransactionRecord, fromTransactionRecord, isHistorical);
      ChangeReportResponse response =
            HttpMessage.send(OseeServerContext.BRANCH_CONTEXT, parameters, requestData, ChangeReportResponse.class);

      if (response.wasSuccessful()) {
         //OseeEventManager.kickBranchEvent(HttpBranchCreation.class, , branch.getId());
      }
      return response;
   }
}
