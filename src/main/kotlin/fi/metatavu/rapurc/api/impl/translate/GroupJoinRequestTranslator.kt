package fi.metatavu.rapurc.api.impl.translate

import fi.metatavu.rapurc.api.impl.KeycloakController
import fi.metatavu.rapurc.api.persistence.model.GroupJoinRequest
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translates JPA group join reuset to REST object
 */
@ApplicationScoped
class GroupJoinRequestTranslator : AbstractTranslator<GroupJoinRequest, fi.metatavu.rapurc.api.model.GroupJoinRequest>() {

    @Inject
    lateinit var metadataTranslator: MetadataTranslator

    @Inject
    lateinit var keycloakController: KeycloakController

    override fun translate(entity: GroupJoinRequest): fi.metatavu.rapurc.api.model.GroupJoinRequest {
        val result = fi.metatavu.rapurc.api.model.GroupJoinRequest()
        val user = keycloakController.findUserByEmail(entity.email)
        result.id = entity.id
        result.email = entity.email
        result.firstName = user?.firstName
        result.lastName = user?.lastName
        result.username = user?.username
        result.groupId = entity.groupId
        result.status = entity.status
        result.metadata = metadataTranslator.translate(entity)
        return result
    }
}
