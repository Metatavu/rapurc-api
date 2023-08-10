package fi.metatavu.rapurc.api.test.functional.resources

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.rapurc.api.client.apis.UserGroupsApi
import fi.metatavu.rapurc.api.client.infrastructure.ApiClient
import fi.metatavu.rapurc.api.client.infrastructure.ClientException
import fi.metatavu.rapurc.api.client.models.User
import fi.metatavu.rapurc.api.client.models.UserGroup
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.impl.ApiTestBuilderResource
import org.junit.Assert
import org.junit.Assert.fail
import java.util.*

/**
 * Test resource for User groups API
 */
class UserGroupTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<UserGroup, ApiClient?>(testBuilder, apiClient) {

    override fun clean(p0: UserGroup?) {
        api.deleteUserGroup(p0!!.id!!)
    }

    override fun getApi(): UserGroupsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return UserGroupsApi(testBuilder.settings.apiBasePath)
    }

    fun create(): UserGroup {
        return addClosable(
            api.createUserGroup(
                UserGroup(
                    "Test group"
                )
            )
        )
    }

    fun create(userGroup: UserGroup): UserGroup {
        return addClosable(api.createUserGroup(userGroup))
    }

    fun find(userGroupId: UUID): UserGroup {
        return api.findUserGroup(userGroupId)
    }

    fun list(admin: Boolean? = null, member: Boolean? = null): Array<UserGroup> {
        return api.listUserGroups(admin = admin, member = member)
    }

    fun update(id: UUID, userGroup: UserGroup): UserGroup {
        return api.updateUserGroup(id, userGroup)
    }

    fun delete(userGroupId: UUID) {
        api.deleteUserGroup(userGroupId)
        removeCloseable { closable: Any? ->
            if (closable !is UserGroup) {
                return@removeCloseable false
            }
            closable.id == userGroupId
        }
    }

    fun listGroupMembers(groupId: UUID): Array<User> {
        return api.listGroupMembers(groupId)
    }

    fun deleteGroupUser(groupId: UUID, userId: UUID) {
        api.deleteGroupUser(groupId, userId)
    }

    fun assertCount(expected: Int) {
        Assert.assertEquals(
            expected,
            api.listUserGroups(null, null).size
        )
    }

    fun assertDeleteGroupUserFailStatus(expectedStatus: Int, groupId: UUID, userId: UUID) {
        try {
            api.deleteGroupUser(groupId, userId)
            fail(String.format("Expected delete group user to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    fun assertListGroupMembersFailStatus(expectedStatus: Int, groupId: UUID) {
        try {
            api.listGroupMembers(groupId)
            fail(String.format("Expected list group members to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    fun assertListFailStatus(expectedStatus: Int) {
        try {
            api.listUserGroups(null, null)
            fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    fun assertFindFailStatus(expectedStatus: Int, id: UUID) {
        try {
            api.findUserGroup(id)
            fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    fun assertCreateFailStatus(expectedStatus: Int) {
        try {
            create()
            fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    fun assertUpdateFailStatus(expectedStatus: Int, id: UUID, group: UserGroup) {
        try {
            update(id, group)
            fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    fun assertDeleteFailStatus(expectedStatus: Int, id: UUID) {
        try {
            api.deleteUserGroup(id)
            fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }
}