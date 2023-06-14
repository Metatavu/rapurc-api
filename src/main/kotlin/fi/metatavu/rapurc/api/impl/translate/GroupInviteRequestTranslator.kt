package fi.metatavu.rapurc.api.impl.translate

import fi.metatavu.rapurc.api.persistence.model.GroupJoinRequest
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translates JPA group join requst to REST object
 */
@ApplicationScoped
class GroupInviteRequestTranslator : AbstractTranslator<GroupJoinRequest, fi.metatavu.rapurc.api.model.GroupJoinInvite>() {

    @Inject
    lateinit var metadataTranslator: MetadataTranslator

    override fun translate(entity: GroupJoinRequest): fi.metatavu.rapurc.api.model.GroupJoinInvite {
        val result = fi.metatavu.rapurc.api.model.GroupJoinInvite()
        result.id = entity.id
        result.email = entity.email
        result.groupId = entity.groupId
        result.status = entity.status
        result.metadata = metadataTranslator.translate(entity)
        return result
    }
}
