package fi.metatavu.rapurc.api.test.functional.resources

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.rapurc.api.client.apis.GroupJoinInvitesApi
import fi.metatavu.rapurc.api.client.infrastructure.ApiClient
import fi.metatavu.rapurc.api.client.infrastructure.ClientException
import fi.metatavu.rapurc.api.client.models.GroupJoinInvite
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.impl.ApiTestBuilderResource
import org.junit.Assert
import java.util.*

/**
 * Tests for Group Join Requests API
 */
class GroupJoinInviteTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<GroupJoinInvite, ApiClient?>(testBuilder, apiClient) {

    override fun clean(joinRequest: GroupJoinInvite) {

    }

    override fun getApi(): GroupJoinInvitesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return GroupJoinInvitesApi(testBuilder.settings.apiBasePath)
    }

    fun create(groupId: UUID, request: GroupJoinInvite): GroupJoinInvite {
        return addClosable(api.createGroupJoinInvite(groupId, request))
    }

    fun resendEmail(groupId: UUID, joinRequestId: UUID) {
        api.sendGroupJoinInviteEmail(groupId, joinRequestId)
    }

    fun find(groupId: UUID, joinRequestId: UUID): GroupJoinInvite {
        return api.findGroupJoinInvite(groupId, joinRequestId)
    }

    fun list(groupId: UUID): Array<GroupJoinInvite> {
        return api.listGroupJoinInvites(groupId)
    }

    fun update(groupId: UUID, joinRequestId: UUID, request: GroupJoinInvite): GroupJoinInvite {
        return api.updateGroupJoinInvite(groupId, joinRequestId, request)
    }

    fun delete(groupId: UUID, joinRequestId: UUID) {
        api.deleteGroupJoinInvite(groupId, joinRequestId)
        removeCloseable { closable: Any? ->
            if (closable !is GroupJoinInvite) {
                return@removeCloseable false
            }

            closable.id == joinRequestId
        }
    }

    fun assertCreateFailStatus(groupId: UUID, request: GroupJoinInvite, status: Int) {
        try {
            api.createGroupJoinInvite(groupId, request)
            Assert.fail(String.format("Expected create to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status, e.statusCode)
        }
    }

    fun assertFindFailStatus(groupId: UUID, joinRequestId: UUID, status: Int) {
        try {
            api.findGroupJoinInvite(groupId, joinRequestId)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status, e.statusCode)
        }
    }

    fun assertListFailStatus(groupId: UUID, status: Int) {
        try {
            api.listGroupJoinInvites(groupId)
            Assert.fail(String.format("Expected list to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status, e.statusCode)
        }
    }

    fun assertUpdateFailStatus(groupId: UUID, joinRequestId: UUID, request: GroupJoinInvite, status: Int) {
        try {
            api.updateGroupJoinInvite(groupId, joinRequestId, request)
            Assert.fail(String.format("Expected update to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status, e.statusCode)
        }
    }

    fun assertDeleteFailStatus(groupId: UUID, joinRequestId: UUID, status: Int) {
        try {
            api.deleteGroupJoinInvite(groupId, joinRequestId)
            Assert.fail(String.format("Expected delete to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status, e.statusCode)
        }
    }
}