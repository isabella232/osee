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
package org.eclipse.osee.ats.artifact;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.window.Window;
import org.eclipse.osee.ats.core.client.artifact.CollectorArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;

/**
 * @author Donald G. Dunne
 */
public abstract class MembersManager<T extends CollectorArtifact> {

   public MembersManager() {
   }

   public abstract IRelationTypeSide getMembersRelationTypeSide();

   public abstract String getItemName();

   public abstract IArtifactType getArtifactType();

   public boolean isHasCollector(Artifact artifact) throws OseeCoreException {
      return artifact.getRelatedArtifactsCount(getMembersRelationTypeSide().getOpposite()) > 0;
   }

   /**
    * change member order for artifact within given member
    */
   public T promptChangeMemberOrder(T memberArt, Artifact artifact) throws OseeCoreException {
      return promptChangeMemberOrder(memberArt, Arrays.asList(artifact));
   }

   public void getCollectors(Artifact artifact, Set<Artifact> collectors, boolean recurse) throws OseeCoreException {
      getCollectors(Arrays.asList(artifact), collectors, recurse);
   }

   public Collection<Artifact> getCollectors(Artifact artifact, boolean recurse) throws OseeCoreException {
      Set<Artifact> collectors = new HashSet<Artifact>();
      getCollectors(artifact, collectors, recurse);
      return collectors;
   }

   public void getCollectors(Collection<Artifact> artifacts, Set<Artifact> goals, boolean recurse) throws OseeCoreException {
      for (Artifact art : artifacts) {
         if (art.isOfType(getArtifactType())) {
            goals.add(art);
         }
         goals.addAll(art.getRelatedArtifacts(getMembersRelationTypeSide().getOpposite()));
         if (recurse && art instanceof AbstractWorkflowArtifact && ((AbstractWorkflowArtifact) art).getParentAWA() != null) {
            getCollectors(((AbstractWorkflowArtifact) art).getParentAWA(), goals, recurse);
         }
      }
   }

   /**
    * change member order for artifacts within given member
    */
   public T promptChangeMemberOrder(T memberArt, List<Artifact> artifacts) throws OseeCoreException {
      StringBuilder currentOrder = new StringBuilder("Current Order: ");
      for (Artifact artifact : artifacts) {
         if (artifacts.size() == 1 && !isHasCollector(artifact) || memberArt == null) {
            AWorkbench.popup(String.format("No %s set for artifact [%s]", getItemName(), artifact));
            return null;
         }
         String currIndexStr = getMemberOrder(memberArt, artifact);
         currentOrder.append(currIndexStr + ", ");
      }

      List<Artifact> members = memberArt.getMembers();
      EntryDialog ed =
         new EntryDialog(String.format("Change %S Order", getItemName()), String.format(
            "%s: %s\n\n%s\n\nEnter New Order Number from 1..%d or %d for last.", getItemName(), memberArt,
            currentOrder.toString().replaceFirst(", $", ""), members.size(), members.size() + 1));
      ed.setNumberFormat(NumberFormat.getIntegerInstance());

      if (ed.open() == Window.OK) {
         String newIndexStr = ed.getEntry();
         Integer enteredIndex = Integer.valueOf(newIndexStr);
         boolean insertLast = enteredIndex > members.size();
         Integer membersIndex = insertLast ? members.size() - 1 : enteredIndex - 1;
         if (membersIndex > members.size()) {
            AWorkbench.popup(String.format("New Order Number [%s] out of range 1..%d", newIndexStr, members.size()));
            return null;
         }
         List<Artifact> reversed = new LinkedList<Artifact>(artifacts);
         Collections.reverse(reversed);
         for (Artifact artifact : reversed) {
            int currentIdx = members.indexOf(artifact);
            Artifact insertTarget = members.get(membersIndex);
            boolean insertAfter = membersIndex > currentIdx;
            memberArt.setRelationOrder(getMembersRelationTypeSide(), insertTarget, insertAfter, artifact);
         }
         memberArt.persist("Prompt-Change " + getItemName());
         return memberArt;
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public String getMemberOrder(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(getArtifactType())) {
         return "";
      }
      if (!isHasCollector(artifact)) {
         return "";
      }
      Collection<Artifact> collectors = getCollectors(artifact, false);
      List<Artifact> collectorsSorted = new ArrayList<Artifact>(collectors);
      Collections.sort(collectorsSorted);
      StringBuffer sb = new StringBuffer();
      for (Artifact member : collectorsSorted) {
         sb.append(String.format("%s-[%s] ", getMemberOrder((T) member, artifact), member));
      }
      return sb.toString();
   }

   public abstract String getMemberOrder(T memberArt, Artifact member) throws OseeCoreException;
}
