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
package org.eclipse.osee.define.report.internal;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import org.eclipse.osee.define.report.SafetyReportGenerator;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;

/**
 * @author Ryan D. Brooks
 * @author David W. Miller
 */
public final class SafetyStreamingOutput implements StreamingOutput {
   private final OrcsApi orcsApi;
   private final String branchGuid;
   private final String codeRoot;
   private final Log logger;

   public SafetyStreamingOutput(Log logger, OrcsApi orcsApi, String branchGuid, String codeRoot) {
      this.logger = logger;
      this.orcsApi = orcsApi;
      this.branchGuid = branchGuid;
      this.codeRoot = codeRoot;
   }

   @Override
   public void write(OutputStream output) {
      try {
         Writer writer = new OutputStreamWriter(output);
         SafetyReportGenerator safetyReport = new SafetyReportGenerator(logger);
         safetyReport.runOperation(orcsApi, branchGuid, codeRoot, writer);
      } catch (Exception ex) {
         throw new WebApplicationException(ex);
      }
   }
}
