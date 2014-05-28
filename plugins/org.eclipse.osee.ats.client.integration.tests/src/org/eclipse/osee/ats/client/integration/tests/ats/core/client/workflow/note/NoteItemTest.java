/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.workflow.note;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.core.client.workflow.note.NoteItem;
import org.eclipse.osee.ats.core.client.workflow.note.NoteType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class NoteItemTest {

   @Test
   public void testNoteItemNoteTypeStringStringUserString() throws OseeCoreException {
      Date date = new Date();
      NoteItem item =
         new NoteItem(NoteType.Comment, "Implement", String.valueOf(date.getTime()), AtsClientService.get().getUserService().getCurrentUser(), "my msg");
      validate(item, date);
   }

   public static void validate(NoteItem item, Date date) throws OseeCoreException {
      Assert.assertEquals(NoteType.Comment, item.getType());
      Assert.assertEquals("Implement", item.getState());
      Assert.assertEquals(AtsClientService.get().getUserService().getCurrentUser(), item.getUser());
      Assert.assertEquals("my msg", item.getMsg());
   }

   public static NoteItem getTestNoteItem(Date date) throws OseeCoreException {
      return new NoteItem(NoteType.Comment, "Implement", String.valueOf(date.getTime()), AtsClientService.get().getUserService().getCurrentUser(), "my msg");
   }

   @Test
   public void testNoteItemStringStringStringUserString() throws OseeCoreException {
      Date date = new Date();
      NoteItem item =
         new NoteItem(NoteType.Comment.name(), "Implement", String.valueOf(date.getTime()), AtsClientService.get().getUserService().getCurrentUser(),
            "my msg");
      validate(item, date);
   }

   @Test
   public void testToString() throws OseeCoreException {
      Date date = new Date();
      NoteItem item = getTestNoteItem(date);

      Assert.assertEquals(
         "Note: Comment from " + AtsClientService.get().getUserService().getCurrentUser().getName() + " for \"Implement\" on " + DateUtil.getMMDDYYHHMM(date) + " - my msg",
         item.toString());
   }

   @Test
   public void testToXmlFromXml() throws OseeCoreException {
      Date date = new Date();
      NoteItem item = getTestNoteItem(date);
      NoteItem item2 =
         new NoteItem(NoteType.Question.name(), "Analyze", String.valueOf(date.getTime()), AtsClientService.get().getUserService().getCurrentUser(),
            "another message");

      String xml = NoteItem.toXml(Arrays.asList(item, item2));
      Assert.assertEquals(
         "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><AtsNote>" + //
         "<Item date=\"" + date.getTime() + "\" msg=\"my msg\" state=\"Implement\" type=\"Comment\" userId=\"" + AtsClientService.get().getUserService().getCurrentUser().getUserId() + "\"/>" + //
         "<Item date=\"" + date.getTime() + "\" msg=\"another message\" state=\"Analyze\" type=\"Question\" userId=\"" + AtsClientService.get().getUserService().getCurrentUser().getUserId() + "\"/></AtsNote>",
         xml);

      List<NoteItem> items = NoteItem.fromXml(xml, "ASDF4");
      validate(items.iterator().next(), date);

      NoteItem fromXmlItem2 = items.get(1);
      Assert.assertEquals(NoteType.Question, fromXmlItem2.getType());
      Assert.assertEquals("Analyze", fromXmlItem2.getState());
      Assert.assertEquals(AtsClientService.get().getUserService().getCurrentUser(), fromXmlItem2.getUser());
      Assert.assertEquals("another message", fromXmlItem2.getMsg());

   }

   @Test
   public void testToHTML() throws OseeCoreException {
      Date date = new Date();
      NoteItem item = getTestNoteItem(date);

      Assert.assertEquals(
         "<b>Note:</b>Comment from " + AtsClientService.get().getUserService().getCurrentUser().getName() + " for \"Implement\" on " + DateUtil.getMMDDYYHHMM(date) + " - my msg",
         item.toHTML());
   }

}
