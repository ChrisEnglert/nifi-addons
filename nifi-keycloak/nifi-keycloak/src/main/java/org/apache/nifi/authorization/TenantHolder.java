package org.apache.nifi.authorization;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A holder to provide atomic access to user group data structures.
 */
public class TenantHolder {

    private final Set<User> allUsers;
    private final Map<String,User> usersById;
    private final Map<String,User> usersByIdentity;

    private final Set<Group> allGroups;
    private final Map<String,Group> groupsById;
    private final Map<String, Set<Group>> groupsByUserIdentity;

    /**
     * Creates a new holder and populates all convenience data structures.
     */
    public TenantHolder(final Set<User> allUsers, final Set<Group> allGroups) {
        // create a convenience map to retrieve a user by id
        final Map<String, User> userByIdMap = Collections.unmodifiableMap(createUserByIdMap(allUsers));

        // create a convenience map to retrieve a user by identity
        final Map<String, User> userByIdentityMap = Collections.unmodifiableMap(createUserByIdentityMap(allUsers));

        // create a convenience map to retrieve a group by id
        final Map<String, Group> groupByIdMap = Collections.unmodifiableMap(createGroupByIdMap(allGroups));

        // create a convenience map to retrieve the groups for a user identity
        final Map<String, Set<Group>> groupsByUserIdentityMap = Collections.unmodifiableMap(createGroupsByUserIdentityMap(allGroups, allUsers));

        // set all the holders
        this.allUsers = allUsers;
        this.allGroups = allGroups;
        this.usersById = userByIdMap;
        this.usersByIdentity = userByIdentityMap;
        this.groupsById = groupByIdMap;
        this.groupsByUserIdentity = groupsByUserIdentityMap;
    }

    /**
     * Creates a Map from user identifier to User.
     *
     * @param users the set of all users
     * @return the Map from user identifier to User
     */
    private Map<String,User> createUserByIdMap(final Set<User> users) {
        Map<String,User> usersMap = new HashMap<>();
        for (User user : users) {
            usersMap.put(user.getIdentifier(), user);
        }
        return usersMap;
    }

    /**
     * Creates a Map from user identity to User.
     *
     * @param users the set of all users
     * @return the Map from user identity to User
     */
    private Map<String,User> createUserByIdentityMap(final Set<User> users) {
        Map<String,User> usersMap = new HashMap<>();
        for (User user : users) {
            usersMap.put(user.getIdentity(), user);
        }
        return usersMap;
    }

    /**
     * Creates a Map from group identifier to Group.
     *
     * @param groups the set of all groups
     * @return the Map from group identifier to Group
     */
    private Map<String,Group> createGroupByIdMap(final Set<Group> groups) {
        Map<String,Group> groupsMap = new HashMap<>();
        for (Group group : groups) {
            groupsMap.put(group.getIdentifier(), group);
        }
        return groupsMap;
    }

    /**
     * Creates a Map from user identity to the set of Groups for that identity.
     *
     * @param groups all groups
     * @param users all users
     * @return a Map from User identity to the set of Groups for that identity
     */
    private Map<String, Set<Group>> createGroupsByUserIdentityMap(final Set<Group> groups, final Set<User> users) {
        Map<String, Set<Group>> groupsByUserIdentity = new HashMap<>();

        for (User user : users) {
            Set<Group> userGroups = new HashSet<>();
            for (Group group : groups) {
                for (String groupUser : group.getUsers()) {
                    if (groupUser.equals(user.getIdentifier())) {
                        userGroups.add(group);
                    }
                }
            }

            groupsByUserIdentity.put(user.getIdentity(), userGroups);
        }

        return groupsByUserIdentity;
    }

    public Set<User> getAllUsers() {
        return allUsers;
    }

    public Map<String, User> getUsersById() {
        return usersById;
    }

    public Set<Group> getAllGroups() {
        return allGroups;
    }

    public Map<String, Group> getGroupsById() {
        return groupsById;
    }

    public User getUser(String identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        return usersByIdentity.get(identity);
    }

    public Set<Group> getGroups(String userIdentity) {
        if (userIdentity == null) {
            throw new IllegalArgumentException("User Identity cannot be null");
        }
        return groupsByUserIdentity.get(userIdentity);
    }

}
