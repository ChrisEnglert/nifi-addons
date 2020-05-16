# nifi-keycloak
Keycloak NiFi AuthProvider

https://www.keycloak.org


## UserGroupProvider
org.apache.nifi.authorization.KeycloakUserGroupProvider


### NiFi properties
```
# OpenId Connect SSO Properties #
nifi.security.user.oidc.discovery.url=http://{HOST}:{PORT}/auth/realms/{REALM}/.well-known/openid-configuration
nifi.security.user.oidc.connect.timeout=5 secs
nifi.security.user.oidc.read.timeout=5 secs
nifi.security.user.oidc.client.id={CLIENTID}
nifi.security.user.oidc.client.secret={CLIENTSECRET}
nifi.security.user.oidc.preferred.jwsalgorithm=RS256
nifi.security.user.oidc.additional.scopes=
nifi.security.user.oidc.claim.identifying.user=preferred_username
```

### Authorizers.xml
```
<userGroupProvider>
    <identifier>keycloak-user-group-provider</identifier>
    <class>org.apache.nifi.authorization.KeycloakUserGroupProvider</class>
    <property name="ServerUrl">http://{HOST}:{PORT}/auth</property>
    <property name="Realm">{REALM}</property>
    <property name="Username"></property>
    <property name="Password"></property>
    <property name="ClientID">admin-cli</property>
</userGroupProvider>
```    
