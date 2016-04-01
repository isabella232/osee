/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.links;

import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.osee.account.rest.client.AccountClient;
import org.eclipse.osee.account.rest.model.AccountWebPreferences;
import org.eclipse.osee.account.rest.model.Link;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.event.EventType;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.TopicEvent;
import org.eclipse.osee.framework.ui.skynet.internal.ServiceUtil;
import org.eclipse.osee.framework.ui.skynet.util.FrameworkEvents;

/**
 * @author Donald G. Dunne
 */
public class LinkUtil {

   private LinkUtil() {
      // Utility class
   }

   public static AccountWebPreferences getAccountsPreferencesData(boolean global) throws Exception {
      return getAccountsPreferencesData(getStoreArtifact(global).getArtId());
   }

   public static AccountWebPreferences getAccountsPreferencesData(int accountId) throws Exception {
      AccountClient client = ServiceUtil.getAccountClient();
      return client.getAccountWebPreferencesByUniqueField(Long.valueOf(accountId));
   }

   /**
    * Delete single link from user or global links and store
    */
   public static void deleteLink(String accountId, Link deleteLink) throws Exception {
      Artifact golbalArtifact = getStoreArtifact(true);
      Conditions.checkNotNull(golbalArtifact, "Guest accountId: " + SystemUser.Anonymous.getUuid());
      deleteLink(deleteLink, true, golbalArtifact);

      Artifact userArt = ArtifactQuery.getArtifactFromId(new Integer(accountId), CoreBranches.COMMON);
      Conditions.checkNotNull(userArt, "User Artifact accountId: " + accountId);
      deleteLink(deleteLink, false, userArt);
   }

   public static void deleteLink(Link deleteLink, boolean global) throws Exception {
      deleteLink(deleteLink, global, getStoreArtifact(global));
   }

   public static void deleteLink(Link deleteLink, boolean global, Artifact useArtifact) throws Exception {
      String webPrefStr = useArtifact.getSoleAttributeValue(CoreAttributeTypes.WebPreferences, "{}");
      AccountWebPreferences webPrefs = new AccountWebPreferences(webPrefStr, useArtifact.getName());
      Link remove = webPrefs.getLinks().remove(deleteLink.getId());
      if (remove != null) {
         saveWebPrefsToArtifactAndKickEvent(global, useArtifact, webPrefs);
      }
   }

   public static void addUpdateLink(Link link, boolean global) throws Exception {
      addUpdateLink(getStoreArtifact(global).getArtId(), link, global);
   }

   /**
    * Update existing link in shared/global web preferences and store
    */
   public static void addUpdateLink(int accountId, Link link, boolean global) throws Exception {
      Artifact useArtifact = getStoreArtifact(global);
      Conditions.checkNotNull(useArtifact, "Could not find store artifact for accountId: " + accountId);

      String webPrefStr = useArtifact.getSoleAttributeValue(CoreAttributeTypes.WebPreferences, null);
      if (webPrefStr != null) {
         AccountWebPreferences webPrefs = new AccountWebPreferences(webPrefStr, useArtifact.getName());
         boolean found = false;
         for (Entry<String, Link> stored : webPrefs.getLinks().entrySet()) {
            if (stored.getKey().equals(link.getId())) {
               setLinkFromLink(link, stored.getValue());
               found = true;
               break;
            }
         }
         if (!found) {
            webPrefs.getLinks().put(link.getId(), link);
         }
         saveWebPrefsToArtifactAndKickEvent(global, useArtifact, webPrefs);
      }
   }

   public static Artifact getStoreArtifact(boolean global) {
      if (global) {
         return ArtifactQuery.getArtifactFromId(SystemUser.Anonymous.getUuid(), CoreBranches.COMMON);
      }
      return LinkUtil.getPersonalLinksArtifact();
   }

   public static void saveWebPreferences(AccountWebPreferences webPrefs, boolean global) throws Exception {
      saveWebPreferences(webPrefs, global, getStoreArtifact(global));
   }

   public static void saveWebPreferences(AccountWebPreferences webPrefs, boolean global, Artifact useArtifact) throws Exception {
      saveWebPrefsToArtifactAndKickEvent(global, useArtifact, webPrefs);
   }

   public static boolean setLinkFromLink(Link fromLink, Link toLink) {
      boolean changed = false;
      if (!toLink.getName().equals(fromLink.getName())) {
         toLink.setName(fromLink.getName());
         changed = true;
      }
      if (!Collections.isEqual(fromLink.getTags(), toLink.getTags())) {
         toLink.setTags(fromLink.getTags());
         changed = true;
      }
      if (!toLink.getTeam().equals(fromLink.getTeam())) {
         toLink.setTeam(fromLink.getTeam());
         changed = true;
      }
      if (!toLink.getUrl().equals(fromLink.getUrl())) {
         toLink.setUrl(fromLink.getUrl());
         changed = true;
      }
      return changed;
   }

   private static void saveWebPrefsToArtifactAndKickEvent(boolean global, Artifact useArtifact, AccountWebPreferences webPrefs) throws Exception {
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(webPrefs);
      useArtifact.setSoleAttributeValue(CoreAttributeTypes.WebPreferences, json);
      useArtifact.persist("Add web preferences links to " + useArtifact.toStringWithId());

      TopicEvent event =
         new TopicEvent((global ? FrameworkEvents.GLOBAL_WEB_PREFERENCES : FrameworkEvents.PERSONAL_WEB_PREFERENCES),
            "links", webPrefs.getLinks().toString(), (global ? EventType.LocalAndRemote : EventType.LocalOnly));
      OseeEventManager.kickTopicEvent(LinkUtil.class, event);
   }

   public static Link getExistingLink(Link link, AccountWebPreferences webPrefs) {
      return getExistingLink(link, webPrefs.getLinks());
   }

   public static Link getExistingLink(Link link, Map<String, Link> links) {
      for (Link existingLink : links.values()) {
         if (existingLink.getId().equals(link.getId())) {
            return existingLink;
         }
      }
      return null;
   }

   public static void upateLinkFromDialog(EditLinkDialog dialog, Link link) throws Exception {
      link.setName(dialog.getEntry());
      link.setUrl(dialog.getUrl());
      boolean global = dialog.isChecked();
      for (String tag : dialog.getTags().split(",")) {
         tag = tag.replaceAll(" ", "");
         link.getTags().add(tag);
      }
      int accountId = 0;
      if (link.getId() == null) {
         link.setId(GUID.create());
      }
      if (global) {
         accountId = SystemUser.Anonymous.getUuid().intValue();
         link.setTeam("Guest");
      } else {
         User user = UserManager.getUser();
         accountId = user.getUuid().intValue();
         link.setTeam(user.getName());
      }
      LinkUtil.addUpdateLink(accountId, link, global);
   }

   public static Artifact getPersonalLinksArtifact() {
      return ArtifactQuery.getArtifactFromId(UserManager.getUser().getArtId(), CoreBranches.COMMON);
   }

}
