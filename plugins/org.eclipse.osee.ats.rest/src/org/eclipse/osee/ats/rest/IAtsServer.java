/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.ats.rest;

import java.util.Collection;
import org.eclipse.nebula.widgets.xviewer.core.model.CustomizeData;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.ai.IAtsActionableItemService;
import org.eclipse.osee.ats.api.notify.IAtsNotifier;
import org.eclipse.osee.ats.api.review.IAtsReviewServiceProvider;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.util.IAtsDatabaseConversion;
import org.eclipse.osee.ats.api.workflow.AtsActionEndpointApi;
import org.eclipse.osee.ats.api.workflow.IAtsBranchServiceProvider;
import org.eclipse.osee.orcs.OrcsApi;

/**
 * @author Donald G Dunne
 */
public interface IAtsServer extends AtsApi, IAtsNotifier, IAtsReviewServiceProvider, IAtsBranchServiceProvider {

   OrcsApi getOrcsApi();

   Iterable<IAtsDatabaseConversion> getDatabaseConversions();

   void setEmailEnabled(boolean emailEnabled);

   void addAtsDatabaseConversion(IAtsDatabaseConversion conversion);

   Collection<CustomizeData> getCustomizations(String namespace);

   Collection<CustomizeData> getCustomizationsGlobal(String namespace);

   AtsActionEndpointApi getActionEndpoint();

   @Override
   IAtsChangeSet createChangeSet(String string, AtsUser systemUser);

   @Override
   IAtsActionableItemService getActionableItemService();

}