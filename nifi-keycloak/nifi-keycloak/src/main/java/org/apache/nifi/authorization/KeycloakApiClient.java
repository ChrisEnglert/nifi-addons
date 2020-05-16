package org.apache.nifi.authorization;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KeycloakApiClient {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakApiClient.class);

    private final Keycloak keycloak;
    private final KeycloakConfig config;

    public KeycloakApiClient(KeycloakConfig config) {
        this.config = config;

        this.keycloak = config.keycloakBuilder()
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(2).build())
                .build();
    }



    public Set<Group> getGroups() {
        return mapGroups(realm().groups().groups());
    }

    public Set<User> getUsers() {
        return mapUsers(realm().users().list());
    }


    private Set<Group> mapGroups(List<GroupRepresentation> groups) {
        return groups.stream().map(this::map).collect(Collectors.toSet());
    }

    private Set<User> mapUsers(List<UserRepresentation> users) {
        return users.stream().map(this::map).collect(Collectors.toSet());
    }

    private Group map(GroupRepresentation kcGroup) {

        GroupResource groupResource = realm().groups().group(kcGroup.getId());
        Set<String> users = groupResource.members().stream().map(UserRepresentation::getId).collect(Collectors.toSet());

        return new Group.Builder()
                .identifier(kcGroup.getId())
                .name(kcGroup.getName())
                .addUsers(users)
                .build();
    }

    private User map(UserRepresentation kcUser) {
        return new User.Builder()
                .identifier(kcUser.getId())
                .identity(kcUser.getUsername())
                .build();
    }

    private RealmResource realm() {
        return keycloak.realm(config.getRealm());
    }
}
