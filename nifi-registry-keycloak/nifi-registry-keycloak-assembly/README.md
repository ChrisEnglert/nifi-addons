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
# NiFi Registry Keycloak extensions


## Prerequisites


## How to install

### Add extensions to existing NiFi Registry

To add Keycloak extensions to an existing NiFi Registry, build the extension with the following command:

```
cd nifi-registry
mvn clean install -f nifi-registry-extensions/nifi-registry-keycloak
```

The extension zip will be created as `nifi-registry-extensions/nifi-registry-keycloak/nifi-registry-keycloak-assembly/target/nifi-registry-keycloak-assembly-xxx-bin.zip`.

Unzip the file into arbitrary directory so that NiFi Registry can use, such as `${NIFI_REG_HOME}/ext/keycloak`.
For example:

```
mkdir -p ${NIFI_REG_HOME}/ext/keycloak
unzip -d ${NIFI_REG_HOME}/ext/keycloak nifi-registry-extensions/nifi-registry-keycloak/nifi-registry-keycloak-assembly/target/nifi-registry-keycloak-assembly-xxx-bin.zip
```

## NiFi Registry Configuration

In order to use this extension, the following NiFi Registry files need to be configured.

### nifi-registry.properties

To manually specify the property when adding the keycloak extensions to an existing NiFi registry, configure the following property:
```
# Specify keycloak extension dir
nifi.registry.extension.dir.keycloak=./ext/keycloak/lib
```

### authorizers.xml

Example config UserGroupProvider
```
    <userGroupProvider>
        <identifier>keycloak-user-group-provider</identifier>
        <class>org.apache.nifi.registry.keycloak.authorization.KeycloakUserGroupProvider</class>
        <property name="ServerUrl">http://localhost:4000/auth</property>
        <property name="Realm">master</property>
        <property name="Username">admin</property>
    	<property name="Password">admin</property>
        <property name="ClientID">admin-cli</property>
    </userGroupProvider>

    <accessPolicyProvider>
        <property name="User Group Provider">keycloak-user-group-provider</property>
    ...
```



NOTE: Remember to remove, or comment out, the FileSystemBundlePersistenceProvider since there can only be one defined.

