/*********************************************************************
 * Copyright (c) 2018 Boeing
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

package org.eclipse.osee.framework.core.dsl.ui.labeling;

import com.google.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.label.DefaultDescriptionLabelProvider;

/**
 * Provides labels for a IEObjectDescriptions and IResourceDescriptions. see
 * http://www.eclipse.org/Xtext/documentation/latest/xtext.html#labelProvider
 * 
 * @author Roberto E. Escobar
 */
public class OseeDslDescriptionLabelProvider extends DefaultDescriptionLabelProvider {

   private final AdapterFactoryLabelProvider adapterFactoryLabelProvider;

   @Inject
   public OseeDslDescriptionLabelProvider(AdapterFactoryLabelProvider adapterFactoryLabelProvider) {
      this.adapterFactoryLabelProvider = adapterFactoryLabelProvider;
   }

   @Override
   protected Object doGetImage(Object element) {
      if (element instanceof IEObjectDescription) {
         EObject eObjectOrProxy = ((IEObjectDescription) element).getEObjectOrProxy();
         return adapterFactoryLabelProvider.getImage(eObjectOrProxy);
      }
      return super.doGetImage(element);
   }

}
