package fi.metatavu.rapurc.api.test.functional.resources

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.rapurc.api.client.apis.UserGroupsApi
import fi.metatavu.rapurc.api.client.infrastructure.ApiClient
import fi.metatavu.rapurc.api.client.infrastructure.ClientException
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

    fun create(
        userGroup: UserGroup
    ): UserGroup {
        return addClosable(api.createUserGroup(userGroup))
    }

    fun find(
        userGroupId: UUID
    ): UserGroup {
        return api.findUserGroup(userGroupId)
    }

    fun list(adminEmail: String? = null): Array<UserGroup> {
        return api.listUserGroups(adminEmail = adminEmail)
    }

    fun update(
        id: UUID,
        userGroup: UserGroup
    ): UserGroup {
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


    fun assertCount(expected: Int) {
        Assert.assertEquals(
            expected,
            api.listUserGroups(null).size
        )
    }

    fun assertListFailStatus(expectedStatus: Int, adminEmail: String? = null) {
        try {
            api.listUserGroups(adminEmail = adminEmail)
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