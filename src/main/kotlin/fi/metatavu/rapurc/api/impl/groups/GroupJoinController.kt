package fi.metatavu.rapurc.api.impl.groups

import fi.metatavu.rapurc.api.impl.KeycloakController
import fi.metatavu.rapurc.api.impl.email.EmailController
import fi.metatavu.rapurc.api.model.JoinRequestStatus
import fi.metatavu.rapurc.api.persistence.dao.GroupJoinRequestDAO
import fi.metatavu.rapurc.api.persistence.model.GroupJoinRequest
import fi.metatavu.rapurc.api.persistence.model.JoinRequestType
import org.keycloak.representations.idm.GroupRepresentation
import org.keycloak.representations.idm.UserRepresentation
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

    @Inject
    lateinit var emailController: EmailController

    @Inject
    lateinit var keycloakController: KeycloakController

    /**
     * Lists group join requests
     *
     * @param groupId group id
     * @param type of join request
     * @param email email of joining user
     * @param status status
     * @return list of group join requests
     */
    fun listGroupJoins(
        groupId: UUID,
        type: JoinRequestType? = null,
        email: String? = null,
        status: JoinRequestStatus? = null
    ): List<GroupJoinRequest> {
        return groupJoinRequestDAO.list(groupId = groupId, type = type, email = email, status = status)
    }

    /**
     * Creates request to join group
     *
     * @param joiningUser user which is going to be added to the group
     * @param groupId target group id
     * @param groupName target group name
     * @param groupAdmin target group admin
     * @param userId creator id
     * @return created join request
     */
    fun createGroupJoinRequest(
        joiningUser: UserRepresentation,
        groupId: UUID,
        groupName: String,
        groupAdmin: UserRepresentation,
        userId: UUID
    ): GroupJoinRequest {
        val userFullName = joiningUser.firstName + " " + joiningUser.lastName
            informGroupAdmin(
                admin = groupAdmin,
                user = joiningUser,
                subject = "User $userFullName has requested to join group $groupName",
                body = "User $userFullName has requested to join group $groupName. Please log in to the system to accept or reject the request."
            )
            return groupJoinRequestDAO.create(
                id = UUID.randomUUID(),
                email = joiningUser.email,
                groupId = groupId,
                status = JoinRequestStatus.PENDING,
                type = JoinRequestType.REQUEST,
                creatorId = userId,
                lastModifierId = userId
            )
    }

    /**
     * Creates invitation to the group
     *
     * @param joiningUserEmail invited email
     * @param groupId target group id
     * @param groupName target group name
     * @param groupAdmin target group admin
     * @param userId creator id
     * @return created invite
     */
    fun createGroupInvite(
        joiningUserEmail: String,
        groupId: UUID,
        groupName: String,
        groupAdmin: UserRepresentation,
        userId: UUID
    ): GroupJoinRequest {
            informUser(
                userEmail = joiningUserEmail,
                admin = groupAdmin,
                subject = "You were invited to join group $groupName",
                body = "You were invited to join group $groupName. Please log in to the system to accept or reject the invitation."
            )
            return groupJoinRequestDAO.create(
                id = UUID.randomUUID(),
                email = joiningUserEmail,
                groupId = groupId,
                status = JoinRequestStatus.PENDING,
                type = JoinRequestType.INVITE,
                creatorId = userId,
                lastModifierId = userId
            )
    }

    /**
     * Finds a group join request
     *
     * @param groupId group id
     * @param type join type
     * @param requestId request id
     * @param email joining user email
     * @return found group join request or null if not found
     */
    fun findGroupJoin(
        groupId: UUID,
        type: JoinRequestType,
        requestId: UUID,
        email: String? = null
        ): GroupJoinRequest? {
        val found = groupJoinRequestDAO.findById(requestId)
        if (found?.groupId != groupId || found.requestType != type) {
            return null
        }
        if (email != null && found.email != email) {
            return null
        }
        return found
    }

    /**
     * Updates group join request status, adds user to group if needed and sends email notifications
     *
     * @param request group join request
     * @param targetGroup target group
     * @param targetGroupAdmin target group admin
     * @param newStatus new status of request
     * @param joiningUser joining user
     * @param modifierId modifier id
     * @return updated group join request
     */
    fun updateGroupJoinRequest(
        request: GroupJoinRequest,
        targetGroup: GroupRepresentation,
        targetGroupAdmin: UserRepresentation,
        newStatus: JoinRequestStatus,
        joiningUser: UserRepresentation,
        modifierId: UUID
    ): GroupJoinRequest {
        val groupName = targetGroup.name
        val updated = groupJoinRequestDAO.updateStatus(request, newStatus, modifierId)
        val userName = getFullName(joiningUser)

        if (newStatus == JoinRequestStatus.ACCEPTED){
            keycloakController.addUserToGroup(joiningUser.id, targetGroup.id)
        }

        when (request.requestType) {
            JoinRequestType.REQUEST -> {
                // if user's request to join got updated, he/she will get email about acceptance or rejection
                informUser(
                    userEmail = joiningUser.email,
                    subject = "Your group join request was updated",
                    admin = targetGroupAdmin,
                    body = "Your request to join $groupName group has been updated to ${newStatus.name.lowercase()}",
                )
            }
            JoinRequestType.INVITE -> {
                //if invited user accepts or rejects the invite inform the admin
                informGroupAdmin(
                    subject = "Your group join invitation was updated",
                    body = "User $userName has updated your invitation to join group $groupName to ${newStatus.name.lowercase()}",
                    admin = targetGroupAdmin,
                    user = joiningUser
                )
            }
        }

        return updated
    }

    /**
     * Deletes a group join request
     *
     * @param request group join request
     */
    fun deleteGroupJoin(request: GroupJoinRequest) {
        return groupJoinRequestDAO.delete(request)
    }

    /**
     * Sends email to user
     *
     * @param userEmail joining user to get email
     * @param admin admin sending the email
     * @param subject emails subkect
     * @param body email body
     */
    fun informUser(userEmail: String, admin: UserRepresentation, subject: String, body: String) {
        emailController.sendEmail(
            to = userEmail,
            from = admin.email,
            subject = subject,
            content = body
        )
    }

    /**
     * Sends email to admin
     *
     * @param admin group admin to get email
     * @param user user sending the email
     * @param subject emails subkect
     * @param body email body
     */
    private fun informGroupAdmin(admin: UserRepresentation, user: UserRepresentation, subject: String, body: String) {
        emailController.sendEmail(
            to = admin.email,
            from = user.email,
            subject = subject,
            content = body
        )
    }

    /**
     * Gets user's full name for email or username
     *
     * @param user user
     * @return full name
     */
    private fun getFullName(user: UserRepresentation): String {
        return if (user.firstName != null && user.firstName.isNotEmpty() && user.lastName != null && user.lastName.isNotEmpty()) user.firstName + " " + user.lastName else user.username
    }
}