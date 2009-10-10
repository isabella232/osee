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
package org.eclipse.osee.framework.skynet.core.httpRequests;

import java.util.Map;
import org.eclipse.osee.framework.core.client.server.HttpResponse;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Roberto E. Escobar
 */
public interface IClientLoopbackCmd {

   public abstract void execute(final Map<String, String> parameters, final HttpResponse httpResponse);

   public abstract boolean isApplicable(String cmd);

   public abstract void process(final Artifact artifact, final Map<String, String> parameters, final HttpResponse httpResponse);

}
