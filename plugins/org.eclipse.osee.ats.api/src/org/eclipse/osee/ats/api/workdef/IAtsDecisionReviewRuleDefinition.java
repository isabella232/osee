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
package org.eclipse.osee.ats.api.workdef;

import java.util.List;

/**
 * @author Mark Joy
 */
public interface IAtsDecisionReviewRuleDefinition extends IAtsRuleDefinition, IExecutableRule {

   public abstract String getRelatedToState();

   public abstract ReviewBlockType getBlockingType();

   public abstract boolean isAutoTransitionToDecision();

   public abstract List<IAtsDecisionReviewOption> getOptions();

}
