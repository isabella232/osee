/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.messaging.definitions;

import java.io.Serializable;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.messaging.Message;
import org.eclipse.osee.framework.messaging.Source;
import org.eclipse.osee.framework.messaging.id.MessageId;

/**
 * @author Michael P. Masterson
 */
public class PropertyStoreMessage implements Message, Serializable {

   private static final long serialVersionUID = -8736301654726742145L;

   private final MessageId messageId;
   private final Source source;
   private final PropertyStore store;

   public PropertyStoreMessage(MessageId messageId, Source source) {
      this.messageId = messageId;
      this.source = source;
      this.store = new PropertyStore("org.eclipse.osee.framework.messaging.definitions.PropertyStoreMessage");
   }

   @Override
   public String toString() {
      return String.format("PropertyStoreMessage\t[MessageId: %s]\t[source: %s]", messageId, source);
   }

   @Override
   public MessageId getId() {
      return messageId;
   }

   @Override
   public Source getSource() {
      return source;
   }

   public PropertyStore getStore() {
      return store;
   }

}
