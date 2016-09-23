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
package org.eclipse.osee.framework.skynet.core.relation.order;

import java.util.List;
import org.eclipse.osee.framework.core.data.RelationSorter;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;

/**
 * @author Andrew M. Finkbeiner
 */
public interface IRelationSorter {

   RelationSorter getSorterId();

   void sort(List<? extends IArtifact> relatives, List<String> relativeSequence);
}
