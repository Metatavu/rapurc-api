package fi.metatavu.rapurc.api.impl.translate

import fi.metatavu.rapurc.api.model.User
import org.keycloak.representations.idm.UserRepresentation
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for user
 */
@ApplicationScoped
class UserTranslator : AbstractTranslator<UserRepresentation, User>() {

    override fun translate(entity: UserRepresentation): User {
        val result = User()
        result.id = UUID.fromString(entity.id)
        result.username = entity.username
        result.firstName = entity.firstName
        result.lastName = entity.lastName
        result.email = entity.email
        return result
    }
}