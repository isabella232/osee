/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.workitem;

import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.agile.IAgileFeatureGroup;
import org.eclipse.osee.ats.api.agile.IAgileTeam;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.country.IAtsCountry;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.ev.IAtsWorkPackage;
import org.eclipse.osee.ats.api.insertion.IAtsInsertion;
import org.eclipse.osee.ats.api.insertion.IAtsInsertionActivity;
import org.eclipse.osee.ats.api.insertion.JaxInsertion;
import org.eclipse.osee.ats.api.insertion.JaxInsertionActivity;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.core.config.AbstractConfigItemFactory;
import org.eclipse.osee.ats.core.config.ActionableItem;
import org.eclipse.osee.ats.core.config.Country;
import org.eclipse.osee.ats.core.config.Program;
import org.eclipse.osee.ats.core.config.TeamDefinition;
import org.eclipse.osee.ats.core.config.Version;
import org.eclipse.osee.ats.core.insertion.Insertion;
import org.eclipse.osee.ats.core.insertion.InsertionActivity;
import org.eclipse.osee.ats.core.model.WorkPackage;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.rest.IAtsServer;
import org.eclipse.osee.ats.rest.internal.util.AtsChangeSet;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;

/**
 * @author Donald G. Dunne
 * @author David W. Miller
 */
public class ConfigItemFactory extends AbstractConfigItemFactory {

   private final Log logger;
   private final IAtsServer atsServer;

   public ConfigItemFactory(Log logger, IAtsServer atsServer) {
      this.logger = logger;
      this.atsServer = atsServer;
   }

   @Override
   public IAtsConfigObject getConfigObject(ArtifactId artifact) throws OseeCoreException {
      IAtsConfigObject configObject = null;
      try {
         if (artifact instanceof ArtifactReadable) {
            ArtifactReadable artRead = (ArtifactReadable) artifact;
            if (artRead.isOfType(AtsArtifactTypes.Version)) {
               configObject = getVersion(artifact);
            } else if (artRead.isOfType(AtsArtifactTypes.TeamDefinition)) {
               configObject = getTeamDef(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.ActionableItem)) {
               configObject = getActionableItem(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.Program)) {
               configObject = getProgram(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.AgileTeam)) {
               configObject = getAgileTeam(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.AgileFeatureGroup)) {
               configObject = getAgileFeatureGroup(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.Insertion)) {
               configObject = getInsertion(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.InsertionActivity)) {
               configObject = getInsertionActivity(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.Country)) {
               configObject = getCountry(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.WorkPackage)) {
               configObject = getWorkPackage(artRead);
            }
         }
      } catch (OseeCoreException ex) {
         logger.error(ex, "Error getting config object for [%s]", artifact);
      }
      return configObject;
   }

   @Override
   public IAtsWorkPackage getWorkPackage(ArtifactId artifact) {
      IAtsWorkPackage workPackage = null;
      if (artifact instanceof ArtifactReadable && ((ArtifactReadable) artifact).isOfType(
         AtsArtifactTypes.WorkPackage)) {
         workPackage = new WorkPackage(logger, artifact, atsServer.getServices());
      }
      return workPackage;
   }

   @Override
   public IAtsVersion getVersion(ArtifactId artifact) {
      IAtsVersion version = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.Version)) {
            version = new Version(logger, atsServer, artRead);
         }
      }
      return version;
   }

   @Override
   public IAtsTeamDefinition getTeamDef(ArtifactId artifact) throws OseeCoreException {
      IAtsTeamDefinition teamDef = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.TeamDefinition)) {
            teamDef = new TeamDefinition(logger, atsServer, artRead);
         }
      }
      return teamDef;
   }

   @Override
   public IAtsActionableItem getActionableItem(ArtifactId artifact) throws OseeCoreException {
      IAtsActionableItem ai = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.ActionableItem)) {
            ai = new ActionableItem(logger, atsServer, artRead);
         }
      }
      return ai;
   }

   @Override
   public IAtsProgram getProgram(ArtifactId artifact) {
      Program program = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.Program)) {
            program = new Program(logger, atsServer, artRead);
         }
      }
      return program;
   }

   @Override
   public IAgileTeam getAgileTeam(ArtifactId artifact) {
      IAgileTeam agileTeam = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.AgileTeam)) {
            agileTeam = atsServer.getAgileService().getAgileTeam(artRead);
         }
      }
      return agileTeam;
   }

   @Override
   public IAgileFeatureGroup getAgileFeatureGroup(ArtifactId artifact) {
      IAgileFeatureGroup agileTeam = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.AgileFeatureGroup)) {
            agileTeam = atsServer.getAgileService().getAgileFeatureGroup(artRead);
         }
      }
      return agileTeam;
   }

   @Override
   public IAtsInsertion getInsertion(ArtifactId artifact) {
      Insertion insertion = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.Insertion)) {
            insertion = new Insertion(logger, atsServer.getServices(), artRead);
            ArtifactReadable programArt =
               ((ArtifactReadable) artifact).getRelated(AtsRelationTypes.ProgramToInsertion_Program).getOneOrNull();
            if (programArt != null) {
               insertion.setProgramUuid(programArt.getUuid());
            }
         } else {
            throw new OseeCoreException("Requested uuid not Insertion");
         }
      }
      return insertion;
   }

   @Override
   public IAtsInsertionActivity getInsertionActivity(ArtifactId artifact) {
      InsertionActivity insertionActivity = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.InsertionActivity)) {
            insertionActivity = new InsertionActivity(logger, atsServer.getServices(), artRead);
            ArtifactReadable insertionArt = ((ArtifactReadable) artifact).getRelated(
               AtsRelationTypes.InsertionToInsertionActivity_Insertion).getOneOrNull();
            if (insertionArt != null) {
               insertionActivity.setInsertionUuid(insertionArt.getUuid());
            }
         } else {
            throw new OseeCoreException("Requested uuid not Insertion Activity");
         }
      }
      return insertionActivity;
   }

   @Override
   public IAtsInsertion createInsertion(ArtifactId programArtifact, JaxInsertion newInsertion) {

      long uuid = newInsertion.getUuid();
      if (uuid <= 0) {
         uuid = Lib.generateArtifactIdAsInt();
      }
      AtsChangeSet changes = (AtsChangeSet) atsServer.getStoreService().createAtsChangeSet("Create new Insertion",
         atsServer.getUserService().getCurrentUser());
      ArtifactReadable insertionArt = (ArtifactReadable) changes.createArtifact(AtsArtifactTypes.Insertion,
         newInsertion.getName(), GUID.create(), uuid);

      changes.relate(programArtifact, AtsRelationTypes.ProgramToInsertion_Insertion, insertionArt);
      changes.execute();
      return getInsertion(insertionArt);
   }

   @Override
   public IAtsInsertion updateInsertion(JaxInsertion updatedInsertion) {
      AtsChangeSet changes = (AtsChangeSet) atsServer.getStoreService().createAtsChangeSet("Update Insertion",
         atsServer.getUserService().getCurrentUser());
      ArtifactId artifact = atsServer.getCache().getArtifact(updatedInsertion.getUuid());
      changes.setSoleAttributeValue(artifact, CoreAttributeTypes.Name, updatedInsertion.getName());
      changes.execute();
      return getInsertion(atsServer.getQuery().andUuid(updatedInsertion.getUuid()).getResults().getExactlyOne());
   }

   @Override
   public void deleteInsertion(ArtifactId artifact) {
      deleteConfigObject(artifact.getUuid(), "Delete Insertion", AtsArtifactTypes.Insertion);
   }

   @Override
   public IAtsInsertionActivity createInsertionActivity(ArtifactId insertion, JaxInsertionActivity newActivity) {
      long uuid = newActivity.getUuid();
      if (uuid <= 0) {
         uuid = Lib.generateArtifactIdAsInt();
      }
      AtsChangeSet changes =
         (AtsChangeSet) atsServer.getStoreService().createAtsChangeSet("Create new Insertion Activity",
            atsServer.getUserService().getCurrentUser());
      ArtifactReadable insertionActivityArt =
         (ArtifactReadable) changes.createArtifact(AtsArtifactTypes.InsertionActivity, newActivity.getName(),
            GUID.create(), uuid);

      changes.relate(insertion, AtsRelationTypes.InsertionToInsertionActivity_InsertionActivity, insertionActivityArt);
      changes.execute();
      return getInsertionActivity(insertionActivityArt);
   }

   @Override
   public IAtsInsertionActivity updateInsertionActivity(JaxInsertionActivity updatedActivity) {
      AtsChangeSet changes = (AtsChangeSet) atsServer.getStoreService().createAtsChangeSet("Update Insertion",
         atsServer.getUserService().getCurrentUser());
      ArtifactReadable insertionActivityArt =
         atsServer.getQuery().andUuid(updatedActivity.getUuid()).getResults().getExactlyOne();

      changes.getTransaction().setSoleAttributeValue(insertionActivityArt, CoreAttributeTypes.Name,
         updatedActivity.getName());
      ArtifactId artifact = atsServer.getCache().getArtifact(updatedActivity.getUuid());
      changes.setSoleAttributeValue(artifact, CoreAttributeTypes.Name, updatedActivity.getName());
      changes.execute();
      return getInsertionActivity(atsServer.getQuery().andUuid(updatedActivity.getUuid()).getResults().getExactlyOne());
   }

   @Override
   public void deleteInsertionActivity(ArtifactId artifact) {
      deleteConfigObject(artifact.getUuid(), "Delete Insertion Activity", AtsArtifactTypes.InsertionActivity);
   }

   private void deleteConfigObject(long uuid, String comment, IArtifactType type) {
      ArtifactReadable toDelete = atsServer.getArtifact(uuid);
      if (toDelete == null) {
         throw new OseeCoreException("No object found for uuid %d", uuid);
      }

      if (!toDelete.getArtifactType().equals(type)) {
         throw new OseeCoreException("Artifact type does not match for %s", comment);
      }
      TransactionBuilder transaction = atsServer.getOrcsApi().getTransactionFactory().createTransaction(
         AtsUtilCore.getAtsBranch(), toDelete, comment);
      transaction.deleteArtifact(toDelete);
      transaction.commit();
   }

   @Override
   public boolean isAtsConfigArtifact(ArtifactId artifact) {
      return getAtsConfigArtifactTypes().contains(((ArtifactReadable) artifact).getArtifactType());
   }

   @Override
   public IAtsCountry getCountry(ArtifactId artifact) {
      IAtsCountry country = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.Country)) {
            country = new Country(logger, atsServer.getServices(), artRead);
         } else {
            throw new OseeCoreException("Requested uuid not Country");
         }
      }
      return country;
   }

   @Override
   public IAtsCountry getCountry(long uuid) {
      return getCountry(atsServer.getArtifact(uuid));
   }

}
