/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.util.widgets;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.XArtifactSelectWidgetWithSave;

/**
 * @author Donald G. Dunne
 */
public class XProgramSelectionWidget extends XArtifactSelectWidgetWithSave {

   public static final String WIDGET_ID = XProgramSelectionWidget.class.getSimpleName();

   public XProgramSelectionWidget() {
      super("Program");
   }

   @Override
   public Collection<Artifact> getArtifacts() {
      List<ArtifactId> programArts =
         AtsClientService.get().getQueryService().createQuery(AtsArtifactTypes.Program).andAttr(
            AtsAttributeTypes.Active, "true").getResultArtifacts().getList();
      return Collections.castAll(programArts);
   }
}
