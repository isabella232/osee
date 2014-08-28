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
package org.eclipse.osee.mail.internal;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.mail.MailConstants;
import org.eclipse.osee.mail.MailMessage;
import org.eclipse.osee.mail.MailUtils;

/**
 * @author Roberto E. Escobar
 */
public class MailMessageFactory {

   private final Log logger;
   private final AtomicInteger testCounter;

   public MailMessageFactory(Log logger, AtomicInteger testCounter) {
      this.logger = logger;
      this.testCounter = testCounter;
   }

   public Session createSession(String transport, String host, int port, boolean requiresAuthentication) {
      final Properties props = new Properties();

      props.put("mail.transport.protocol", transport);
      props.put("mail." + transport + ".host", host);
      props.put("mail." + transport + ".port", String.valueOf(port));
      props.put("mail." + transport + ".auth", String.valueOf(requiresAuthentication).toLowerCase());

      boolean isDebugEnabled = logger != null && logger.isDebugEnabled();
      props.put("mail.debug", isDebugEnabled);

      Session session = Session.getDefaultInstance(props);
      session.setDebug(isDebugEnabled);
      if (isDebugEnabled) {
         session.setDebugOut(new PrintStream(new ForwardingStream() {
            @Override
            protected void forward(String data) {
               logger.debug(data);
            }
         }));
      }
      return session;
   }

   public Transport createTransport(Session session, String username, String password) throws MessagingException {
      Transport transport = session.getTransport();
      if (!transport.isConnected()) {
         if (Strings.isValid(username) && Strings.isValid(password)) {
            transport.connect(username, password);
         } else {
            transport.connect();
         }
      }
      return transport;
   }

   public MimeMessage createMimeMessage(Session session, MailMessage email) throws AddressException, MessagingException {
      MimeMessage message = new MimeMessage(session);
      message.setFrom(toAddress(email.getFrom()));
      message.setSubject(email.getSubject(), "UTF-8");
      message.setReplyTo(toAddress(email.getReplyTo()));
      message.setRecipients(Message.RecipientType.TO, toAddress(email.getRecipients()));
      message.addHeader(MailConstants.MAIL_UUID_HEADER, email.getId());
      message.setSentDate(new Date());

      Multipart multiPart = new MimeMultipart();
      for (DataHandler handler : email.getAttachments()) {
         MimeBodyPart part = new MimeBodyPart();
         part.setDataHandler(handler);
         multiPart.addBodyPart(part);
      }
      message.setContent(multiPart);
      message.saveChanges();
      return message;
   }

   public MailMessage createTestMessage(String adminEmail, String subject, String plainTextBody) {
      MailMessage message = new MailMessage();
      message.setId(UUID.randomUUID().toString());
      message.setFrom(adminEmail);
      message.getRecipients().add(adminEmail);

      String subjectWithCounter = String.format("%s #%s", subject, testCounter.incrementAndGet());
      message.setSubject(subjectWithCounter);

      String htmlData =
         String.format("<html><body><big>%s</big><p>%s</p></body></html>", subjectWithCounter, plainTextBody);
      DataSource source;
      try {
         source = MailUtils.createAlternativeDataSource("test.email", htmlData, plainTextBody);
      } catch (Exception ex) {
         source = MailUtils.createFromString("test.email", plainTextBody);
      }
      message.addAttachment(source);
      return message;
   }

   private Address[] toAddress(Collection<String> rawAddresses) throws AddressException {
      InternetAddress[] toReturn = new InternetAddress[rawAddresses.size()];
      int index = 0;
      for (String rawAddress : rawAddresses) {
         toReturn[index++] = toAddress(rawAddress);
      }
      return toReturn;
   }

   private InternetAddress toAddress(String rawAddress) throws AddressException {
      return new InternetAddress(rawAddress);
   }

}
