package fi.metatavu.rapurc.api.impl.translate

import fi.metatavu.rapurc.api.model.UserGroup
import org.keycloak.representations.idm.GroupRepresentation
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * Translates Keycloak Group representation to REST User Group
 */
@ApplicationScoped
class UserGroupTranslator: AbstractTranslator<GroupRepresentation, UserGroup>() {
    override fun translate(entity: GroupRepresentation): UserGroup {
        val userGroup = UserGroup()
        userGroup.id = UUID.fromString(entity.id)
        userGroup.name = entity.name
        return userGroup
    }
}