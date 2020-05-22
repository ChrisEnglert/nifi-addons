# nifi-addons
Addons for Apache NiFi, NiFi-Registry



## Keycloak NiFi

https://github.com/ChrisEnglert/nifi-addons/tree/master/nifi-keycloak

### UserGroupProvider
Can load NiFi User and Groups from KeyCloak



## Keycloak NiFi-Registry

https://github.com/ChrisEnglert/nifi-addons/tree/master/nifi-registry-keycloak

### UserGroupProvider
Can load NiFi-Registry User and Groups from KeyCloak

Currently not working due to classloader issue.
Pending https://issues.apache.org/jira/browse/NIFIREG-394
Can solve https://issues.apache.org/jira/browse/NIFIREG-147



## NiFi-Registry OIDC

https://github.com/ChrisEnglert/nifi-addons/tree/master/nifi-registry-oidc

### OpenID Connect Auth Provider
Login to NiFi-Registry using grant_type=password flow
