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
package org.eclipse.osee.mail.api;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Roberto E. Escobar
 */
public interface MailService {

   MailStatus sendTestMessage();

   Future<MailStatus> sendAsyncTestMessage();

   Future<MailStatus> sendAsyncTestMessage(MailCallback callback);

   List<MailStatus> sendMessages(MailMessage... email);

   List<MailStatus> sendMessages(Iterable<MailMessage> email);

   List<Future<MailStatus>> sendAsyncMessages(MailCallback callback, MailMessage... emails);

   List<Future<MailStatus>> sendAsyncMessages(MailCallback callback, Iterable<MailMessage> emails);

   List<Future<MailStatus>> sendAsyncMessages(MailMessage... emails);

   List<Future<MailStatus>> sendAsyncMessages(Iterable<MailMessage> emails);

}
