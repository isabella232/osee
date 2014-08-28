/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.util.widgets;

import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.cpa.CpaFactory;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.database.core.OseeInfo;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.IArtifactWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.XHyperlinkLabel;

public class XCpaOpenPcrWidget extends XHyperlinkLabel implements IArtifactWidget {

   public static final String WIDGET_ID = XCpaOpenPcrWidget.class.getSimpleName();
   private Artifact artifact;
   private final IAttributeType pcrIdAttr;

   public XCpaOpenPcrWidget(IAttributeType pcrIdAttr) {
      super("open", "", false);
      this.pcrIdAttr = pcrIdAttr;
   }

   protected String getCpaBasepath() {
      return OseeInfo.getCachedValue(CpaFactory.CPA_BASEPATH_KEY);
   }

   @Override
   public String getUrl() {
      String url = null;
      String orgPcrId = artifact.getSoleAttributeValueAsString(pcrIdAttr, null);
      String pcrTool = artifact.getSoleAttributeValue(AtsAttributeTypes.PcrToolId, null);
      if (Strings.isValid(orgPcrId) && Strings.isValid(pcrTool)) {
         url = String.format("%s/ats/cpa/decision/%s?pcrSystem=%s", getCpaBasepath(), orgPcrId, pcrTool);
      }
      return url;
   }

   @Override
   public Artifact getArtifact() throws OseeCoreException {
      return artifact;
   }

   @Override
   public void saveToArtifact() throws OseeCoreException {
      // do nothing
   }

   @Override
   public void revert() throws OseeCoreException {
      // do nothing
   }

   @Override
   public Result isDirty() throws OseeCoreException {
      return Result.FalseResult;
   }

   @Override
   public void setArtifact(Artifact artifact) throws OseeCoreException {
      this.artifact = artifact;
   }

}
