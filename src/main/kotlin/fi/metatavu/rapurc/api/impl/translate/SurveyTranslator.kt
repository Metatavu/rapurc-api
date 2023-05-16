package fi.metatavu.rapurc.api.impl.translate

import fi.metatavu.rapurc.api.persistence.model.Survey
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA survey entities into REST resources
 */
@ApplicationScoped
class SurveyTranslator: AbstractTranslator<Survey, fi.metatavu.rapurc.api.model.Survey>() {

    @Inject
    lateinit var metadataTranslator: MetadataTranslator

    override fun translate(entity: Survey): fi.metatavu.rapurc.api.model.Survey {
        val result = fi.metatavu.rapurc.api.model.Survey()

        result.id = entity.id
        result.status = entity.status
        result.type = entity.type
        result.dateUnknown = entity.dateUnknown
        result.startDate = entity.startDate
        result.endDate = entity.endDate
        result.markedAsDone = entity.markedAsDone
        result.additionalInformation = entity.additionalInformation
        result.creatorName = entity.creatorName
        result.metadata = metadataTranslator.translate(entity)
        return result
    }

}