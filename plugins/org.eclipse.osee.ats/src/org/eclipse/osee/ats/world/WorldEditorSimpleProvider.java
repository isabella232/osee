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
package org.eclipse.osee.ats.world;

import java.util.Collection;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;

/**
 * @author Donald G. Dunne
 */
public class WorldEditorSimpleProvider extends WorldEditorProvider {

   private final String name;
   private final Collection<? extends Artifact> artifacts;
   private final Artifact expandToArtifact;

   public WorldEditorSimpleProvider(String name, Collection<? extends Artifact> artifacts) {
      this(name, artifacts, null, TableLoadOption.None);
   }

   public WorldEditorSimpleProvider(String name, Collection<? extends Artifact> artifacts, CustomizeData customizeData, TableLoadOption... tableLoadOption) {
      this(name, artifacts, customizeData, null, tableLoadOption);
   }

   /**
    * @param expandToArtifact if given, expand World Editor and highlight this item
    */
   public WorldEditorSimpleProvider(String name, Collection<? extends Artifact> artifacts, CustomizeData customizeData, Artifact expandToArtifact, TableLoadOption... tableLoadOption) {
      super(customizeData, tableLoadOption);
      this.name = name;
      this.artifacts = artifacts;
      this.expandToArtifact = expandToArtifact;
   }

   @Override
   public IWorldEditorProvider copyProvider() {
      return new WorldEditorSimpleProvider(name, artifacts, customizeData, tableLoadOptions);
   }

   @Override
   public void run(WorldEditor worldEditor, SearchType searchtype, boolean forcePend) {
      worldEditor.getWorldComposite().load(name, artifacts, customizeData, expandToArtifact, getTableLoadOptions());
   }

   @Override
   public String getName() {
      return name;
   }

   public Artifact getExpandToArtifact() {
      return expandToArtifact;
   }

}
