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
package org.eclipse.osee.framework.ui.skynet.artifact.prompt;

import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Jeff C. Phillips
 */
public interface IHandlePromptChange {
   boolean promptOk() throws OseeCoreException;

   boolean store() throws OseeCoreException;

}
