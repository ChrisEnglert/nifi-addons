<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at
      http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
# NiFi Registry OIDC extensions

This modules provides OIDC related extensions for NiFi Registry.

## Prerequisites


## How to install

### Add oidc extensions to existing NiFi Registry

The extension zip will be created as `nifi-registry-extensions/nifi-registry-oidc/nifi-registry-oidc-assembly/target/nifi-registry-oidc-assembly-xxx-bin.zip`.

Unzip the file into arbitrary directory so that NiFi Registry can use, such as `${NIFI_REG_HOME}/ext/oidc`.
For example:

```
mkdir -p ${NIFI_REG_HOME}/ext/oidc
unzip -d ${NIFI_REG_HOME}/ext/oidc nifi-registry-extensions/nifi-registry-oidc/nifi-registry-oidc-assembly/target/nifi-registry-oidc-assembly-xxx-bin.zip
```

## NiFi Registry Configuration

In order to use this extension, the following NiFi Registry files need to be configured.

### nifi-registry.properties

To manually specify the property when adding the OIDC extensions to an existing NiFi registry, configure the following property:
```
# Specify extension dir
nifi.registry.extension.dir.oidc=./ext/oidc/lib
```

### nifi-registry.properties

```
nifi.registry.security.needClientAuth=false
```

### identity-providers.xml

```
<identityProviders>
    <provider>
        <identifier>oidc-identity-provider</identifier>
        <class>org.apache.nifi.registry.web.security.authentication.oidc.OidcPasswordIdentityProvider</class>
        <property name="Token Endpoint">http://localhost:4000/auth/realms/master/protocol/openid-connect/token</property>
        <property name="Client Id">nifi</property>
        <property name="Client Secret">a356d9e5-001e-4844-aac2-be0838052187</property>
        <property name="Identity Claim">sub</property>
        <property name="Username Claim">preferred_username</property>
    </provider>
```

