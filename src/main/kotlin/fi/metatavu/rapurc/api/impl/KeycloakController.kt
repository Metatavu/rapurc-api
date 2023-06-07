package fi.metatavu.rapurc.api.impl

import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.representations.idm.GroupRepresentation
import org.keycloak.representations.idm.UserRepresentation
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Keycloak controller
 */
@ApplicationScoped
class KeycloakController {

    @Inject
    @ConfigProperty(name = "keycloak.url")
    lateinit var authServerUrl: String

    @Inject
    @ConfigProperty(name = "keycloak.realm")
    lateinit var realm: String

    @Inject
    @ConfigProperty(name = "rapurc.keycloak.api-admin.secret")
    lateinit var clientSecret: String

    @Inject
    @ConfigProperty(name = "rapurc.keycloak.api-admin.client")
    lateinit var clientId: String

    @Inject
    @ConfigProperty(name = "rapurc.keycloak.api-admin.user")
    lateinit var apiAdminUser: String

    @Inject
    @ConfigProperty(name = "rapurc.keycloak.api-admin.password")
    lateinit var apiAdminPassword: String

    @Inject
    lateinit var logger: Logger

    /* Users */

    /**
     * Finds user by email
     *
     * @param email email
     * @return found user or null if not found
     */
    fun findUserByEmail(email: String): UserRepresentation? {
        return try {
            realm().users().search(
                null,
                null,
                null,
                email,
                null,
                0,
                1,
                null,
                true
            )?.firstOrNull()
        } catch (e: Exception) {
            logger.error("Failed to find user by email", e)
            null
        }
    }

    /**
     * Gets map of all user's attributes
     *
     * @param userId user id
     * @return map of all user's attributes or null if not found
     */
    fun getUserAttributes(userId: UUID): Map<String, List<String>>? {
        return try {
            realm().users().get(userId.toString()).toRepresentation().attributes ?: emptyMap()
        } catch (e: Exception) {
            logger.error("Failed to get user attributes", e)
            null
        }
    }

    /**
     * Updates user's attributes
     *
     * @param userId user id
     * @param attributes new attributes map
     */
    fun updateUserAttributes(userId: UUID, attributes: Map<String, List<String>>) {
        try {
            realm().users().get(userId.toString()).update(
                UserRepresentation().apply {
                    this.attributes = attributes
                }
            )
        } catch (e: Exception) {
            logger.error("Failed to update user attributes", e)
        }
    }

    /**
     * Gets list of groups user has listed in its attributes
     *
     * @param userId user id
     * @return list of groups user has listed in its attributes
     */
    fun getGroupsAttributes(userId: UUID): MutableList<String> {
        val attributes = getUserAttributes(userId) ?: return mutableListOf()
        return attributes.getOrElse(GROUP_ATTRIBUTE_TITLE) { emptyList() }.toMutableList()
    }

    /**
     * Add user as member to another group
     *
     * @param userId user id
     * @param groupId group id
     */
    fun addUserToGroup(userId: UUID, groupId: UUID) {
        try {
            realm().users().get(userId.toString()).joinGroup(groupId.toString())
        } catch (e: Exception) {
            logger.error("Failed to add user to group", e)
        }
    }

    /**
     * Removes user from group
     *
     * @param userId user id
     * @param groupId group id
     */
    fun removeUserFromGroup(userId: UUID, groupId: UUID) {
        try {
            realm().users().get(userId.toString()).leaveGroup(groupId.toString())
        } catch (e: Exception) {
            logger.error("Failed to remove user from group", e)
        }
    }

    /**
     * Set user as group admin (add group id to user's 'groups' attribute)
     *
     * @param groupId group id
     * @param userId user id
     */
    fun assignAsGroupAdmin(groupId: String, userId: UUID) {
        val groups = getGroupsAttributes(userId)
        groups.add(groupId)
        updateUserAttributes(userId, mapOf(GROUP_ATTRIBUTE_TITLE to groups))
    }

    /**
     * Unset user as group admin (remove group id from user's 'groups' attribute)
     *
     * @param groupId group id
     * @param userId user id
     */
    fun unassignAsGroupAdmin(groupId: String, userId: UUID) {
        val groups = getGroupsAttributes(userId)
        groups.remove(groupId)
        updateUserAttributes(userId, mapOf(GROUP_ATTRIBUTE_TITLE to groups))
    }

    /* Groups */

    /**
     * List all groups that the user is admin of
     *
     * @param userId user id
     * @return list of groups that the user is admin of
     */
    fun listGroupsForAdmin(userId: UUID): List<GroupRepresentation> {
        val groupIds = getGroupsAttributes(userId)
        return groupIds.mapNotNull {
            try {
                realm().groups().group(it).toRepresentation()
            } catch (e: Exception) {
                logger.error("Failed to get group $it", e)
                null
            }
        }
    }

    /**
     * Gets ID of the group user belongs to
     *
     * @param userId user id
     * @return user group id if belongs to any
     */
    fun getUserGroupId(userId: UUID): UUID? {
        val groups = realm().users().get(userId.toString())?.groups() ?: return null
        if (groups.size >= 1) {
            return UUID.fromString(groups[0].id)
        }

        return null
    }

    /**
     * Finds group by name
     *
     * @param name group name
     * @return found group or null if not found
     */
    fun findGroupByName(name: String?): GroupRepresentation? {
        return try {
            realm().groups().groups(name, 0, 1).getOrNull(0)
        } catch (e: Exception) {
            logger.error("Failed to find group by name", e)
            null
        }
    }

    /**
     * Lists all groups
     *
     * @return list of all groups
     */
    fun listGroups(): List<GroupRepresentation> {
        return try {
            realm().groups().groups()
        } catch (e: Exception) {
            logger.error("Failed to list groups", e)
            emptyList()
        }
    }

    /**
     * Creates new group
     *
     * @param groupName group name
     * @return created group or null if failed
     */
    fun createGroup(groupName: String): GroupRepresentation? {
        return try {
            val group = GroupRepresentation()
            group.name = groupName
            realm().groups().add(group)
            findGroupByName(groupName)
        } catch (e: Exception) {
            logger.error("Failed to create group", e)
            null
        }
    }

    /**
     * Finds group by id
     *
     * @param groupId group id
     * @return found group or null if not found
     */
    fun findGroupById(groupId: UUID): GroupRepresentation? {
        return try {
            realm().groups().group(groupId.toString()).toRepresentation()
        } catch (e: Exception) {
            logger.error("Failed to find group by id", e)
            null
        }
    }

    /**
     * Deletes group
     *
     * @param groupId group id
     */
    fun deleteGroup(groupId: UUID) {
        try {
            realm().groups().group(groupId.toString()).remove()
        } catch (e: Exception) {
            logger.error("Failed to delete group", e)
        }
    }

    /**
     * Updates group name
     *
     * @param groupId group id
     * @param groupName group name
     */
    fun updateGroup(groupId: UUID, groupName: String) {
        try {
            val group = GroupRepresentation()
            group.name = groupName
            realm().groups().group(groupId.toString()).update(group)
        } catch (e: Exception) {
            logger.error("Failed to update group", e)
        }
    }

    /**
     * Constructs a Keycloak client
     *
     * @return Keycloak client
     */
    private fun getKeycloakClient(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm(realm)
            .username(apiAdminUser)
            .password(apiAdminPassword)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build()
    }

    /**
     * Gets realm
     *
     * @return realm
     */
    private fun realm(): RealmResource {
        return getKeycloakClient().realm(realm)
    }

    companion object {
        private var GROUP_ATTRIBUTE_TITLE = "groups"
    }
}