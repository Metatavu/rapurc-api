package fi.metatavu.rapurc.api.persistence.dao

import fi.metatavu.rapurc.api.model.JoinRequestStatus
import fi.metatavu.rapurc.api.persistence.model.GroupJoinRequest
import fi.metatavu.rapurc.api.persistence.model.GroupJoinRequest_
import fi.metatavu.rapurc.api.persistence.model.JoinRequestType
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for group join request
 */
@ApplicationScoped
class GroupJoinRequestDAO : AbstractDAO<GroupJoinRequest>() {

    /**
     * Creates new group join request
     *
     * @param id id
     * @param email email of user wishing to join
     * @param groupId group id
     * @param status status
     * @param type type of the join request
     * @param invitingUserFullName full name of user creating invite if applicable
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     */
    fun create(
        id: UUID,
        email: String,
        groupId: UUID,
        status: JoinRequestStatus,
        type: JoinRequestType,
        invitingUserFullName: String?,
        creatorId: UUID,
        lastModifierId: UUID
    ): GroupJoinRequest {
        val groupJoinRequest = GroupJoinRequest()
        groupJoinRequest.id = id
        groupJoinRequest.email = email
        groupJoinRequest.groupId = groupId
        groupJoinRequest.status = status
        groupJoinRequest.requestType = type
        groupJoinRequest.invitingUserName = invitingUserFullName
        groupJoinRequest.creatorId = creatorId
        groupJoinRequest.lastModifierId = lastModifierId
        return persist(groupJoinRequest)
    }

    /**
     * Lists group join requests
     *
     * @param type join request type
     * @param groupId group id
     * @param email email of user joining
     * @param status status
     * @return list of group join requests
     */
    fun list(
        type: JoinRequestType?,
        groupId: UUID?,
        email: String?,
        status: JoinRequestStatus?
    ): List<GroupJoinRequest> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<GroupJoinRequest> = criteriaBuilder.createQuery(GroupJoinRequest::class.java)
        val root: Root<GroupJoinRequest> = criteria.from(GroupJoinRequest::class.java)
        val restrictions = ArrayList<Predicate>()

        if (groupId != null) {
            restrictions.add(criteriaBuilder.equal(root.get(GroupJoinRequest_.groupId), groupId))
        }

        if (type != null) {
            restrictions.add(criteriaBuilder.equal(root.get(GroupJoinRequest_.requestType), type))
        }

        if (email != null) {
            restrictions.add(criteriaBuilder.equal(root.get(GroupJoinRequest_.email), email))
        }

        if (status != null) {
            restrictions.add(criteriaBuilder.equal(root.get(GroupJoinRequest_.status), status))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())
        criteria.orderBy(criteriaBuilder.desc(root.get(GroupJoinRequest_.modifiedAt)))
        val query: TypedQuery<GroupJoinRequest> = entityManager.createQuery(criteria)
        return query.resultList
    }

    /**
     * Updates status of group join request
     *
     * @param request existing request
     * @param status status
     * @param userId user id
     * @return updated group join request
     */
    fun updateStatus(request: GroupJoinRequest, status: JoinRequestStatus, userId: UUID): GroupJoinRequest {
        request.status = status
        request.lastModifierId = userId
        return persist(request)
    }
}