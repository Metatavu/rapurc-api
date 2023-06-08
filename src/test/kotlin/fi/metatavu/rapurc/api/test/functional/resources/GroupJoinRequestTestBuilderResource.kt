package fi.metatavu.rapurc.api.test.functional.resources

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.rapurc.api.client.apis.GroupJoinRequestsApi
import fi.metatavu.rapurc.api.client.infrastructure.ApiClient
import fi.metatavu.rapurc.api.client.infrastructure.ClientException
import fi.metatavu.rapurc.api.client.models.GroupJoinRequest
import fi.metatavu.rapurc.api.client.models.JoinRequestStatus
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.impl.ApiTestBuilderResource
import org.junit.Assert
import java.util.*

/**
 * Tests for Group Join Requests API
 */
class GroupJoinRequestTestBuilderResource(testBuilder: TestBuilder, private val accessTokenProvider: AccessTokenProvider?, apiClient: ApiClient) : ApiTestBuilderResource<GroupJoinRequest, ApiClient?>(testBuilder, apiClient) {

    override fun clean(joinRequest: GroupJoinRequest) {
        // Cleaned manually since the creator of the request cannot remove it
    }

    override fun getApi(): GroupJoinRequestsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return GroupJoinRequestsApi(testBuilder.settings.apiBasePath)
    }

    fun create(groupId: UUID, request: GroupJoinRequest): GroupJoinRequest {
        return addClosable(api.createGroupJoinRequest(groupId, request))
    }

    fun find(groupId: UUID, joinRequestId: UUID): GroupJoinRequest {
        return api.findGroupJoinRequest(groupId, joinRequestId)
    }

    fun list(groupId: UUID, status: JoinRequestStatus?): Array<GroupJoinRequest> {
        return api.listGroupJoinRequests(groupId, status)
    }

    fun update(groupId: UUID, joinRequestId: UUID, request: GroupJoinRequest): GroupJoinRequest {
        return api.updateGroupJoinRequest(groupId, joinRequestId, request)
    }

    fun delete(groupId: UUID, joinRequestId: UUID) {
        api.deleteGroupJoinRequest(groupId, joinRequestId)
        removeCloseable { closable: Any? ->
            if (closable !is GroupJoinRequest) {
                return@removeCloseable false
            }

            val closeableJoinRequest = closable as GroupJoinRequest
            closeableJoinRequest.id == joinRequestId
        }
    }

    fun assertCreateFailStatus(groupId: UUID, request: GroupJoinRequest, status: Int) {
        try {
            api.createGroupJoinRequest(groupId, request)
            Assert.fail(String.format("Expected create to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status, e.statusCode)
        }
    }

    fun assertFindFailStatus(groupId: UUID, joinRequestId: UUID, status: Int) {
        try {
            api.findGroupJoinRequest(groupId, joinRequestId)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status, e.statusCode)
        }
    }

    fun assertListFailStatus(groupId: UUID, status: Int) {
        try {
            api.listGroupJoinRequests(groupId, null)
            Assert.fail(String.format("Expected list to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status, e.statusCode)
        }
    }

    fun assertUpdateFailStatus(groupId: UUID, joinRequestId: UUID, request: GroupJoinRequest, status: Int) {
        try {
            api.updateGroupJoinRequest(groupId, joinRequestId, request)
            Assert.fail(String.format("Expected update to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status, e.statusCode)
        }
    }

    fun assertDeleteFailStatus(groupId: UUID, joinRequestId: UUID, status: Int) {
        try {
            api.deleteGroupJoinRequest(groupId, joinRequestId)
            Assert.fail(String.format("Expected delete to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status, e.statusCode)
        }
    }
}