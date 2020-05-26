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

package org.eclipse.osee.framework.ui.skynet.artifact.prompt;

import java.util.Collection;
import java.util.Date;
import org.eclipse.jface.window.Window;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.DateSelectionDialog;

/**
 * @author Jeff C. Phillips
 */
public class DateHandlePromptChange implements IHandlePromptChange {
   private final DateSelectionDialog diag;
   private final Collection<? extends Artifact> artifacts;
   private final AttributeTypeId attributeType;
   private final boolean persist;

   public DateHandlePromptChange(Collection<? extends Artifact> artifacts, AttributeTypeId attributeType, String displayName, boolean persist) {
      super();
      this.artifacts = artifacts;
      this.attributeType = attributeType;
      this.persist = persist;
      String diagTitle = "Select " + displayName;
      Date currentDate = null;
      try {
         currentDate = artifacts.size() == 1 ? artifacts.iterator().next().getSoleAttributeValue(attributeType,
            (Date) null) : null;
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      this.diag = new DateSelectionDialog(diagTitle, diagTitle, currentDate);
   }

   @Override
   public boolean promptOk() {
      return diag.open() == Window.OK;
   }

   @Override
   public boolean store() {
      for (Artifact artifact : artifacts) {
         if (diag.isNoneSelected()) {
            artifact.deleteSoleAttribute(attributeType);
         } else {
            artifact.setSoleAttributeValue(attributeType, diag.getSelectedDate());
         }
      }
      if (persist) {
         TransactionManager.persistInTransaction("Persist artifact date change", artifacts);
      }
      return true;
   }
}