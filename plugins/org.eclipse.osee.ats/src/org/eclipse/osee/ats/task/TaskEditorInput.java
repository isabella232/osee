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
package org.eclipse.osee.ats.task;

import java.util.logging.Level;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Donald G. Dunne
 */
public class TaskEditorInput implements IEditorInput {

   private final ITaskEditorProvider itaskEditorProvider;

   @Override
   public int hashCode() {
      return itaskEditorProvider.hashCode();
   }

   public ITaskEditorProvider getItaskEditorProvider() {
      return itaskEditorProvider;
   }

   public TaskEditorInput(ITaskEditorProvider itaskEditorProvider) {
      this.itaskEditorProvider = itaskEditorProvider;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof TaskEditorInput)) {
         return false;
      }
      TaskEditorInput castObj = (TaskEditorInput) obj;
      return castObj.itaskEditorProvider.equals(this.itaskEditorProvider);
   }

   @Override
   public boolean exists() {
      return false;
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return null;
   }

   @Override
   public IPersistableElement getPersistable() {
      return null;
   }

   @Override
   public String getToolTipText() {
      try {
         return itaskEditorProvider.getName();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return "Exception getting name: " + ex.getLocalizedMessage();
      }
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class adapter) {
      return null;
   }

   @Override
   public String getName() {
      try {
         return itaskEditorProvider.getName();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return "Exception getting name: " + ex.getLocalizedMessage();
      }
   }
}
