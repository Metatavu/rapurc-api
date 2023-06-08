package fi.metatavu.rapurc.api.impl.groups

import fi.metatavu.rapurc.api.model.JoinRequestStatus
import fi.metatavu.rapurc.api.persistence.dao.GroupJoinRequestDAO
import fi.metatavu.rapurc.api.persistence.model.GroupJoinRequest
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller to manage users' requests and invitations to join Keycloak groups
 */
@ApplicationScoped
class GroupJoinController {

    @Inject
    lateinit var groupJoinRequestDAO: GroupJoinRequestDAO

    /**
     * Lists group join requests
     *
     * @param groupId group id
     * @param status status
     * @return list of group join requests
     */
    fun listGroupJoinRequests(groupId: UUID, status: JoinRequestStatus? = null): List<GroupJoinRequest> {
        return groupJoinRequestDAO.list(groupId = groupId, status = status)
    }

    /**
     * Creates a new group join request
     *
     * @param email email
     * @param groupId group id
     * @param userId user id
     * @return created group join request
     */
    fun createGroupJoinRequest(email: String, groupId: UUID, userId: UUID): GroupJoinRequest {
        return groupJoinRequestDAO.create(
            id = UUID.randomUUID(),
            email = email,
            groupId = groupId,
            status = JoinRequestStatus.PENDING,
            creatorId = userId,
            lastModifierId = userId
        )
    }

    /**
     * Finds a group join request
     *
     * @param groupId group id
     * @param requestId request id
     * @return found group join request or null if not found
     */
    fun findGroupJoinRequest(groupId: UUID, requestId: UUID): GroupJoinRequest? {
        val found = groupJoinRequestDAO.findById(requestId)
        if (found?.groupId != groupId) {
            return null
        }
        return found
    }

    /**
     * Updates group join request status
     *
     * @param request group join request
     * @param status status
     * @param userId user id
     * @return updated group join request
     */
    fun updateGroupJoinRequest(request: GroupJoinRequest, status: JoinRequestStatus, userId: UUID): GroupJoinRequest {
        return groupJoinRequestDAO.updateStatus(request, status, userId)
    }

    /**
     * Deletes a group join request
     *
     * @param request group join request
     */
    fun deleteGroupJoinRequest(request: GroupJoinRequest) {
        return groupJoinRequestDAO.delete(request)
    }
}