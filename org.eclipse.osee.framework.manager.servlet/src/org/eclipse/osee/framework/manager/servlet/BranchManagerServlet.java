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

package org.eclipse.osee.framework.manager.servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.osee.framework.core.enums.Function;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.server.OseeHttpServlet;
import org.eclipse.osee.framework.core.services.IOseeBranchServiceProvider;
import org.eclipse.osee.framework.core.services.IOseeDataTranslationProvider;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.manager.servlet.function.ChangeReportFunction;
import org.eclipse.osee.framework.manager.servlet.function.CreateBranchFunction;
import org.eclipse.osee.framework.manager.servlet.function.CreateCommitFunction;
import org.eclipse.osee.framework.manager.servlet.function.PurgeBranchFunction;
import org.eclipse.osee.framework.manager.servlet.internal.Activator;

/**
 * @author Andrew M Finkbeiner
 */
public class BranchManagerServlet extends OseeHttpServlet {

   private static final long serialVersionUID = 226986283540461526L;

   private final IOseeBranchServiceProvider branchServiceProvider;
   private final IOseeDataTranslationProvider dataTransalatorProvider;

   public BranchManagerServlet(IOseeBranchServiceProvider branchServiceProvider, IOseeDataTranslationProvider dataTransalatorProvider) {
      this.branchServiceProvider = branchServiceProvider;
      this.dataTransalatorProvider = dataTransalatorProvider;
   }

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      try {
         String rawFunction = req.getParameter("function");
         Function function = Function.fromString(rawFunction);
         switch (function) {
            case BRANCH_COMMIT:
               Operations.executeAsJob(
                     new CreateCommitFunction(req, resp, branchServiceProvider, dataTransalatorProvider), false).join();
               break;
            case CREATE_BRANCH:
               Operations.executeAsJob(
                     new CreateBranchFunction(req, resp, branchServiceProvider, dataTransalatorProvider), false).join();
               break;
            case CHANGE_REPORT:
               Operations.executeAsJob(
                     new ChangeReportFunction(req, resp, branchServiceProvider, dataTransalatorProvider), false).join();
               break;
            case PURGE_BRANCH:
               Operations.executeAsJob(
                     new PurgeBranchFunction(req, resp, branchServiceProvider, dataTransalatorProvider), false).join();
               break;
            default:
               throw new UnsupportedOperationException();
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE,
               String.format("Branch servlet request error: [%s]", req.toString()), ex);
         resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
         resp.setContentType("text/plain");
         resp.getWriter().write(Lib.exceptionToString(ex));
         resp.getWriter().flush();
         resp.getWriter().close();
      }
   }
}
