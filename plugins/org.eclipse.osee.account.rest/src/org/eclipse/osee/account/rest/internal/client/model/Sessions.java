/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.account.rest.internal.client.model;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class Sessions {

   public List<ClientSession> sessions = new LinkedList<ClientSession>();

   public void add(ClientSession clientSession) {
      sessions.add(clientSession);
   }

   public List<ClientSession> get() {
      return sessions;
   }

}
