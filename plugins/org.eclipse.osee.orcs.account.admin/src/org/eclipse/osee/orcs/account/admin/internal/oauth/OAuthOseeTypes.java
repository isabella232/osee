/*********************************************************************
 * Copyright (c) 2014 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.orcs.account.admin.internal.oauth;

import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.Artifact;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.ImageContent;
import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import static org.eclipse.osee.orcs.account.admin.internal.oauth.OAuthTypeTokenProvider.oauth;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.data.AttributeTypeBoolean;
import org.eclipse.osee.framework.core.data.AttributeTypeString;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.orcs.account.admin.internal.oauth.enums.token.OAuthClientGrantAttributeType;

/**
 * @author Roberto E. Escobar
 */
public interface OAuthOseeTypes {
   // @formatter:off
   AttributeTypeString OAuthClientAuthorizedAudience = oauth.createString(7160371155049131554L, "oauth.client.Authorized Audience", MediaType.TEXT_PLAIN, "");
   OAuthClientGrantAttributeType OAuthClientAuthorizedGrantType = oauth.createEnum(new OAuthClientGrantAttributeType());
   AttributeTypeString OAuthClientAuthorizedRedirectUri = oauth.createString(5424134645937614632L, "oauth.client.Authorized Redirect URI", MediaType.TEXT_PLAIN, "");
   AttributeTypeString OAuthClientAuthorizedScope = oauth.createString(3555983643778551674L, "oauth.client.Authorized Scope", MediaType.TEXT_PLAIN, "");
   AttributeTypeBoolean OAuthClientIsConfidential = oauth.createBoolean(537327028164749105L, "oauth.client.Is Confidential", MediaType.TEXT_PLAIN, "");
   AttributeTypeString OAuthClientLogoUri = oauth.createString(7843963586445815729L, "oauth.client.Logo URI", MediaType.TEXT_PLAIN, "");
   AttributeTypeString OAuthClientProperties = oauth.createString(5633616462036881674L, "oauth.client.Properties", MediaType.APPLICATION_JSON, "");
   AttributeTypeString OAuthClientWebsiteUri = oauth.createString(7824657901879283800L, "oauth.client.Website URI", MediaType.TEXT_PLAIN, "");

   ArtifactTypeToken OAuthClient = oauth.add(oauth.artifactType(756912961500447526L, "OAuth Client", false, Artifact)
      .any(ImageContent, "")
      .any(OAuthClientAuthorizedAudience, "")
      .any(OAuthClientAuthorizedGrantType, "")
      .any(OAuthClientAuthorizedRedirectUri, "")
      .any(OAuthClientAuthorizedScope, "")
      .exactlyOne(OAuthClientIsConfidential, "true")
      .zeroOrOne(OAuthClientLogoUri, "")
      .zeroOrOne(OAuthClientProperties, "")
      .zeroOrOne(OAuthClientWebsiteUri, ""));
   ArtifactToken OAUTH_TYPES = ArtifactToken.valueOf(7067755, "OAuthTypes", COMMON, CoreArtifactTypes.OseeTypeDefinition);
   // @formatter:on

}