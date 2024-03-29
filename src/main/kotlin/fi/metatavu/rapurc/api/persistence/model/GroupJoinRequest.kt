package fi.metatavu.rapurc.api.persistence.model

import fi.metatavu.rapurc.api.model.JoinRequestStatus
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
class GroupJoinRequest : Metadata() {

    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var email: String

    @Column(nullable = false)
    lateinit var groupId: UUID

    @Column(nullable = false)
    @Enumerated(javax.persistence.EnumType.STRING)
    lateinit var status: JoinRequestStatus

    @Column(nullable = false)
    @Enumerated(javax.persistence.EnumType.STRING)
    lateinit var requestType: JoinRequestType

    // Optional field for invite requests
    @Column
    var invitingUserName: String? = null
}

/**
 * Type of request, where invite means that user is offered to join the group and request means
 * that user is asking to join
 */
enum class JoinRequestType {
    INVITE,
    REQUEST
}