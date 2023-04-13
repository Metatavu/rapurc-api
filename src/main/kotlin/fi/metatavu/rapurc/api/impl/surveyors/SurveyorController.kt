package fi.metatavu.rapurc.api.impl.surveyors

import fi.metatavu.rapurc.api.persistence.dao.SurveyDAO
import fi.metatavu.rapurc.api.persistence.dao.SurveyorDAO
import fi.metatavu.rapurc.api.persistence.model.Surveyor
import fi.metatavu.rapurc.api.persistence.model.Survey
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for Surveyors and included Other Structures
 */
@ApplicationScoped
class SurveyorController {

    @Inject
    lateinit var surveyDAO: SurveyDAO

    @Inject
    lateinit var surveyorDAO: SurveyorDAO

    /**
     * Lists surveyors based on survey
     *
     * @param survey filter by survey
     * @return filtered surveyor list
     */
    fun list(survey: Survey): List<Surveyor> {
        return surveyorDAO.list(survey = survey)
    }

    /**
     * Creates surveyor
     *
     * @param survey survey
     * @param surveyor surveyor to create
     * @param creatorId creator id
     * @return created Surveyor
     */
    fun create(
        survey: Survey,
        surveyor: fi.metatavu.rapurc.api.model.Surveyor,
        creatorId: UUID
    ): Surveyor {

        return surveyorDAO.create(
            id = UUID.randomUUID(),
            survey = survey,
            firstName = surveyor.firstName,
            lastName = surveyor.lastName,
            company = surveyor.company,
            role = surveyor.role,
            phone = surveyor.phone,
            email = surveyor.email,
            reportDate = surveyor.reportDate,
            visits = surveyor.visits,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Finds surveyor in the db
     *
     * @param surveyorId surveyor id
     * @return found surveyor or null
     */
    fun find(surveyorId: UUID): Surveyor? {
        return surveyorDAO.findById(surveyorId)
    }

    /**
     * Updates surveyor
     *
     * @param surveyorToUpdate original surveyor to update
     * @param surveyor new surveyor data
     * @param lastModifierId modifier id
     * @return update Surveyor
     */
    fun update(
        surveyorToUpdate: Surveyor,
        surveyor: fi.metatavu.rapurc.api.model.Surveyor,
        lastModifierId: UUID
    ): Surveyor {
        surveyorToUpdate.firstName = surveyor.firstName
        surveyorToUpdate.lastName = surveyor.lastName
        surveyorToUpdate.company = surveyor.company
        surveyorToUpdate.role = surveyor.role
        surveyorToUpdate.phone = surveyor.phone
        surveyorToUpdate.email = surveyor.email
        surveyorToUpdate.reportDate = surveyor.reportDate
        surveyorToUpdate.lastModifierId = lastModifierId
        surveyorToUpdate.visits = surveyor.visits

        return surveyorDAO.updateSurveyor(surveyorToUpdate)
    }

    /**
     * Deletes surveyor
     *
     * @param surveyorToDelete surveyor to delete
     * @param userId user ID
     */
    fun delete(surveyorToDelete: Surveyor, userId: UUID) {
        surveyDAO.update(survey = surveyorToDelete.survey!!, userId)
        surveyorDAO.delete(surveyorToDelete)
    }

}
