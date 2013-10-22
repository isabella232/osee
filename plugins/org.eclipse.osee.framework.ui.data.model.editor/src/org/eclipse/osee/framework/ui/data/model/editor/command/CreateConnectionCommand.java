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
package org.eclipse.osee.framework.ui.data.model.editor.command;

import java.util.logging.Level;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.gef.commands.Command;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.data.model.editor.internal.Activator;
import org.eclipse.osee.framework.ui.data.model.editor.model.ArtifactDataType;
import org.eclipse.osee.framework.ui.data.model.editor.model.ConnectionModel;
import org.eclipse.osee.framework.ui.data.model.editor.model.InheritanceLinkModel;

/**
 * @author Roberto E. Escobar
 */
public class CreateConnectionCommand extends Command {

   private final ConnectionModel<ArtifactDataType> connectionModel;
   private final ArtifactDataType source;
   private ArtifactDataType target;
   private ArtifactDataType oldAncestor;

   public CreateConnectionCommand(ConnectionModel<ArtifactDataType> connectionModel, ArtifactDataType source) {
      super("Create connection");
      this.connectionModel = connectionModel;
      this.source = source;
      this.oldAncestor = null;
   }

   @Override
   public boolean canExecute() {
      boolean result = source != null && target != null && connectionModel != null;
      result = result && !source.getSuperTypes().contains(target);
      return result;
   }

   @Override
   public void execute() {
      redo();
   }

   public void setTarget(ArtifactDataType target) {
      this.target = target;
   }

   @Override
   public void redo() {
      try {
         if (connectionModel instanceof InheritanceLinkModel) {

         }
         oldAncestor = source.getSuperType();
         source.setSuperType(target);
         if (source == target) {
            connectionModel.getBendpoints().add(new AbsoluteBendpoint(source.getLocation().getTranslated(-10, 10)));
            connectionModel.getBendpoints().add(new AbsoluteBendpoint(source.getLocation().getTranslated(-10, -10)));
            connectionModel.getBendpoints().add(new AbsoluteBendpoint(source.getLocation().getTranslated(10, -10)));
         }
         connectionModel.setSource(source);
         connectionModel.setTarget(target);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   @Override
   public void undo() {
      try {
         connectionModel.setSource(null);
         connectionModel.setTarget(null);
         if (source == target) {
            connectionModel.getBendpoints().clear();
         }
         source.setSuperType(oldAncestor);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }
}
