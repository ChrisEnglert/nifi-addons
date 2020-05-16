/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.authorization;

import org.apache.nifi.authentication.exception.ProviderDestructionException;
import org.apache.nifi.authorization.annotation.AuthorizerContext;
import org.apache.nifi.authorization.exception.AuthorizationAccessException;
import org.apache.nifi.authorization.exception.AuthorizerCreationException;
import org.apache.nifi.util.NiFiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class KeycloakUserGroupProvider implements UserGroupProvider {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserGroupProvider.class);

    private KeycloakConfig keycloakConfig;
    private NiFiProperties properties;
    private ScheduledExecutorService keycloakSync;

    private AtomicReference<TenantHolder> tenants = new AtomicReference<>(null);

    @Override
    public void initialize(UserGroupProviderInitializationContext initializationContext) throws AuthorizerCreationException {
        keycloakSync = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            final ThreadFactory factory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                final Thread thread = factory.newThread(r);
                thread.setName(String.format("%s (%s) - background sync thread", getClass().getSimpleName(), initializationContext.getIdentifier()));
                return thread;
            }
        });
    }

    @Override
    public void onConfigured(AuthorizerConfigurationContext configurationContext) throws AuthorizerCreationException {

        keycloakConfig = KeycloakConfig.from(configurationContext);

        final long syncInterval = 60;

        try {
            // perform the initial load, tenants must be loaded as the configured UserGroupProvider is supplied
            // to the AccessPolicyProvider for granting initial permissions
            load();

            // ensure the tenants were successfully synced
            if (tenants.get() == null) {
                throw new AuthorizerCreationException("Unable to sync users and groups.");
            }

            // schedule the background thread to load the users/groups
            keycloakSync.scheduleWithFixedDelay(() -> {
                try {
                    load();
                } catch (final Throwable t) {
                    logger.error("Failed to sync User/Groups from LDAP due to {}. Will try again in {} seconds.", new Object[] {t.toString(), syncInterval});
                    if (logger.isDebugEnabled()) {
                        logger.error("", t);
                    }
                }
            }, syncInterval, syncInterval, TimeUnit.SECONDS);
        } catch (final AuthorizationAccessException e) {
            throw new AuthorizerCreationException(e);
        }
    }

    @Override
    public Set<User> getUsers() throws AuthorizationAccessException {
        return tenants.get().getAllUsers();
    }

    @Override
    public User getUser(String identifier) throws AuthorizationAccessException {
        return tenants.get().getUsersById().get(identifier);
    }

    @Override
    public User getUserByIdentity(String identity) throws AuthorizationAccessException {
        return tenants.get().getUser(identity);
    }

    @Override
    public Set<Group> getGroups() throws AuthorizationAccessException {
        return tenants.get().getAllGroups();
    }

    @Override
    public Group getGroup(String identifier) throws AuthorizationAccessException {
        return tenants.get().getGroupsById().get(identifier);
    }

    @Override
    public UserAndGroups getUserAndGroups(String identity) throws AuthorizationAccessException {
        final TenantHolder holder = tenants.get();
        return new UserAndGroups() {
            @Override
            public User getUser() {
                return holder.getUser(identity);
            }

            @Override
            public Set<Group> getGroups() {
                return holder.getGroups(identity);
            }
        };
    }


    @AuthorizerContext
    public void setNiFiProperties(NiFiProperties properties) {
        this.properties = properties;
    }

    @Override
    public final void preDestruction() throws ProviderDestructionException {
        keycloakSync.shutdown();
        try {
            if (!keycloakSync.awaitTermination(10000, TimeUnit.MILLISECONDS)) {
                logger.info("Failed to stop sync thread in 10 sec. Terminating");
                keycloakSync.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void load() {

        KeycloakApiClient client = new KeycloakApiClient(keycloakConfig);
        var userList = client.getUsers();
        var groupList = client.getGroups();

        tenants.set(new TenantHolder(new HashSet<>(userList), new HashSet<>(groupList)));
    }


}
