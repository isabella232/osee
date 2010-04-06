/*
 * Created on Mar 25, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.event.artifact;

import java.util.Collection;
import org.eclipse.osee.framework.messaging.event.res.event.AttributeChange;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class EventModifiedBasicGuidArtifact extends EventBasicGuidArtifact {

   private final Collection<AttributeChange> attributeChanges;

   public EventModifiedBasicGuidArtifact(Artifact artifact, Collection<AttributeChange> attributeChanges) {
      super(EventModType.Modified, artifact.getBasicGuidArtifact());
      this.attributeChanges = attributeChanges;
   }

   public EventModifiedBasicGuidArtifact(String branchGuid, String fromArtTypeGuid, String artTypeGuid, String guid, Collection<AttributeChange> attributeChanges) {
      super(EventModType.Modified, branchGuid, artTypeGuid, guid);
      this.attributeChanges = attributeChanges;
   }

   public String toString() {
      return String.format("[%s - %s - %s]", EventModType.Modified.name(), getGuid(), getArtTypeGuid(),
            attributeChanges);
   }

   public Collection<AttributeChange> getAttributeChanges() {
      return attributeChanges;
   }
}
