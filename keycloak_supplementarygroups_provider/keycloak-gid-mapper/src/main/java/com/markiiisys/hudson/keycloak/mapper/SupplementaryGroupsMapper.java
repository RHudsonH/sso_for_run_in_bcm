
package com.markiiisys.hudson.keycloak.mapper;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.List;

public class SupplementaryGroupsMapper extends AbstractOIDCProtocolMapper implements OIDCIDTokenMapper, OIDCAccessTokenMapper, UserInfoTokenMapper {

	public static final String PROVIDER_ID = "supplementary-groups-mapper";

	private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

	static {
		OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
		OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, SupplementaryGroupsMapper.class);
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "Supplementary Groups (GID) Mapper";
	}

	@Override
	public String getDisplayCategory() {
		return TOKEN_MAPPER_CATEGORY;
	}

	@Override
	public String getHelpText() {
		return "Maps LDAP group gidNumbers to SUPPLEMENTRYGROUPS claim.";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return configProperties;
	}

	@Override
	protected void setClaim(IDToken token,
							ProtocolMapperModel mappingModel,
							UserSessionModel userSession,
							KeycloakSession keycloakSession,
							ClientSessionContext clientSessionCtx) {

		List<String> groupNames = new ArrayList<>();
		List<Integer> gidNumbers = new ArrayList<>();

		userSession.getUser()
					.getGroupsStream()
					.forEach(group -> {
						groupNames.add(group.getName());

						String gid = group.getFirstAttribute("gidNumber");
						if (gid != null) {
							try {
								gidNumbers.add(Integer.parseInt(gid.trim()));
							} catch (NumberFormatException e) {
								// skip groups without a valid integer gidNumber
							}
						}
					});


		token.getOtherClaims().put("GROUPS", groupNames);
		token.getOtherClaims().put("SUPPLEMENTARYGROUPS", gidNumbers);
	}

}

