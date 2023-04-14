package fi.metatavu.rapurc.api.impl.translate

import fi.metatavu.rapurc.api.persistence.model.Surveyor
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA surveyor entities into REST resources
 */
@ApplicationScoped
class SurveyorTranslator: AbstractTranslator<Surveyor, fi.metatavu.rapurc.api.model.Surveyor>() {

    @Inject
    lateinit var metadataTranslator: MetadataTranslator

    override fun translate(entity: Surveyor): fi.metatavu.rapurc.api.model.Surveyor {
        val result = fi.metatavu.rapurc.api.model.Surveyor()

        result.id = entity.id
        result.surveyId = entity.survey?.id
        result.firstName = entity.firstName
        result.lastName = entity.lastName
        result.company = entity.company
        result.role = entity.role
        result.phone = entity.phone
        result.email = entity.email
        result.reportDate = entity.reportDate
        result.visits = entity.visits
        result.metadata = metadataTranslator.translate(entity)
        return result
    }

}