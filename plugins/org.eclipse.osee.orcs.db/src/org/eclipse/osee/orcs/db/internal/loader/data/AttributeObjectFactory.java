/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.loader.data;

import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.VersionData;

/**
 * @author Roberto E. Escobar
 */
public interface AttributeObjectFactory extends VersionObjectFactory {

   AttributeData createAttributeData(VersionData version, Integer localId, AttributeTypeId attributeType, ModificationType modType, int artId, Object value, String uri, ApplicabilityId applicId);

   AttributeData createAttributeData(VersionData version, Integer localId, AttributeTypeId attributeType, ModificationType modType, int artId, ApplicabilityId applicId);

   AttributeData createCopy(AttributeData source);
}