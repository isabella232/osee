/*******************************************************************************
 * Copyright (c) 2018 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.widgets.checkbox;

/**
 * @author Donald G. Dunne
 */
public interface ICheckBoxStateTreeViewer {

   boolean isEnabled(Object element);

   boolean isChecked(Object element);

   void setEnabled(Object object, boolean enabled);

}