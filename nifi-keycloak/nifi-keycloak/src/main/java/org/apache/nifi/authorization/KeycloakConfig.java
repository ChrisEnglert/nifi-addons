package org.apache.nifi.authorization;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;

public class KeycloakConfig {

    public static final String PROP_SERVER_URL = "ServerUrl";
    public static final String PROP_REALM = "Realm";
    public static final String PROP_CLIENT_ID = "ClientID";
    public static final String PROP_CLIENT_SECRET = "ClientSecret";

    private final String server;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    private KeycloakConfig(String server,
                           String realm,
                           String clientId,
                           String clientSecret) {

        this.server = server;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public static KeycloakConfig from(AuthorizerConfigurationContext context) {

        return new KeycloakConfig(
            getOrDefault(context, PROP_SERVER_URL, "http://localhost:3000/auth"),
            getOrDefault(context, PROP_REALM, "master"),
            getOrDefault(context, PROP_CLIENT_ID, "admin-cli"),
            getOrDefault(context, PROP_CLIENT_SECRET, "admin")
        );
    }

    private static String getOrDefault(AuthorizerConfigurationContext context, String property, String defaultValue) {
        if (context.getProperty(property) != null && context.getProperty(property).isSet()) {
            return context.getProperty(property).getValue();
        }

        return defaultValue;
    }

    public final String getRealm() {
        return realm;
    }

    public KeycloakBuilder keycloakBuilder() {
        KeycloakBuilder builder = KeycloakBuilder.builder();
        apply(builder);
        return builder;
    }

    public KeycloakBuilder apply(KeycloakBuilder builder) {
        return builder.serverUrl(server)
            .realm(realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientSecret(clientSecret)
            .clientId(clientId);
    }
}
