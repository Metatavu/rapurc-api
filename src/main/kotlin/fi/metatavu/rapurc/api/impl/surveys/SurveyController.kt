package fi.metatavu.rapurc.api.impl.surveys

import fi.metatavu.rapurc.api.impl.buildings.BuildingController
import fi.metatavu.rapurc.api.impl.materials.ReusableController
import fi.metatavu.rapurc.api.impl.owners.OwnerInformationController
import fi.metatavu.rapurc.api.impl.surveyors.SurveyorController
import fi.metatavu.rapurc.api.impl.waste.HazardousWasteController
import fi.metatavu.rapurc.api.impl.waste.WasteController
import fi.metatavu.rapurc.api.model.SurveyStatus
import fi.metatavu.rapurc.api.model.SurveyType
import fi.metatavu.rapurc.api.persistence.dao.SurveyDAO
import fi.metatavu.rapurc.api.persistence.model.Survey
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for surveys
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class SurveyController {

    @Inject
    lateinit var surveyDAO: SurveyDAO

    @Inject
    lateinit var surveyorController: SurveyorController

    @Inject
    lateinit var buildingController: BuildingController

    @Inject
    lateinit var ownerInformationController: OwnerInformationController

    @Inject
    lateinit var reusableController: ReusableController

    @Inject
    lateinit var wasteController: WasteController

    @Inject
    lateinit var hazardousWasteController: HazardousWasteController

    @Inject
    lateinit var attachmentController: AttachmentController

    /**
     * Lists surveys with given filters
     *
     * @param firstResult first result
     * @param maxResults maximum amount of results
     * @param address filter by address
     * @param status filter by status
     * @param type survey type
     * @param dateUnknown demolition date unknown
     * @param startDate start date
     * @param endDate end date
     * @param keycloakGroupId filter by group id
     * @return List of visitor variables
     */
    fun list(
        firstResult: Int,
        maxResults: Int,
        address: String?,
        status: SurveyStatus?,
        type: SurveyType?,
        dateUnknown: Boolean?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        keycloakGroupId: UUID?
        ): List<Survey> {
        return surveyDAO.list(
            firstResult = firstResult,
            maxResults = maxResults,
            address = address,
            status = status,
            type = type,
            dateUnknown = dateUnknown,
            startDate = startDate,
            endDate = endDate,
            keycloakGroupId = keycloakGroupId
        )
    }

    /**
     * Creates new survey
     *
     * @param status new status
     * @param keycloakGroupId keycloak group id
     * @param type survey type
     * @param startDate estimated demolition start
     * @param dateUnknown date of demolition is unknown
     * @param endDate estimated demolition end
     * @param additionalInformation additional information
     * @param creatorDisplayName creator's name
     * @param creatorId creator's ID
     * @return created survey
     */
    fun create(
        status: SurveyStatus,
        keycloakGroupId: UUID,
        type: SurveyType,
        dateUnknown: Boolean?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        additionalInformation: String?,
        creatorDisplayName: String,
        creatorId: UUID
    ): Survey {
        return surveyDAO.create(
            id = UUID.randomUUID(),
            status = status,
            keycloakGroupId = keycloakGroupId,
            type = type,
            dateUnknown = dateUnknown,
            startDate = startDate,
            endDate = endDate,
            additionalInformation = additionalInformation,
            creatorDisplayName = creatorDisplayName,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Finds survey with id
     *
     * @param surveyId survey ID
     * @return survey or null if not found
     */
    fun find(surveyId: UUID): Survey? {
        return surveyDAO.findById(id = surveyId)
    }

    /**
     * Updates survey
     *
     * @param survey survey to update
     * @param status new status
    * @param dateUnknown date unknown
     * @param startDate start date
     * @param endDate end date
     * @param lastModifierId last modifier's id
     * @return updated survey
     */
    fun update(
        survey: Survey,
        status: SurveyStatus,
        dateUnknown: Boolean?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        additionalInformation: String?,
        lastModifierId: UUID
    ): Survey {
        // If survey gets the status updated as done, it should have the new markedAsUpdated value set
        if (status == SurveyStatus.DONE && survey.status != SurveyStatus.DONE) {
            surveyDAO.updateMarkedAsDone(survey = survey, markedAsDone = OffsetDateTime.now(), lastModifierId = lastModifierId)
        }

        // if survey is losing its done status, the markedAsDone value should be reset
        if (status != SurveyStatus.DONE && survey.status == SurveyStatus.DONE) {
            surveyDAO.updateMarkedAsDone(survey = survey, markedAsDone = null, lastModifierId = lastModifierId)
        }

        val result = surveyDAO.updateStatus(survey = survey, status = status, lastModifierId = lastModifierId)
        surveyDAO.updateDateUnknown(survey = result, dateUnknown = dateUnknown, lastModifierId = lastModifierId)
        surveyDAO.updateStartDate(survey = result, startDate = startDate, lastModifierId = lastModifierId)
        surveyDAO.updateEndDate(survey = result, endDate = endDate, lastModifierId = lastModifierId)
        surveyDAO.updateAdditionalInformation(survey = result, additionalInformation = additionalInformation, lastModifierId = lastModifierId)
        return result
    }

    /**
     * Deletes survey and all entities that depend on it
     *
     * @param survey survey to delete
     * @param userId user id
     */
    fun deleteSurvey(survey: Survey, userId: UUID) {
        surveyorController.list(survey = survey).forEach { surveyorController.delete(it, userId) }
        buildingController.list(survey = survey, buildingType = null).forEach { buildingController.delete(it, userId) }
        ownerInformationController.list(survey = survey).forEach { ownerInformationController.delete(it, userId) }
        reusableController.list(survey = survey, material = null)?.forEach { reusableController.delete(it, userId)}
        wasteController.list(survey = survey, wasteMaterial = null, usage = null).forEach { wasteController.delete(it, userId) }
        hazardousWasteController.list(survey = survey, wasteSpecifier = null, hazardousMaterial = null).forEach { hazardousWasteController.delete(it, userId)}
        attachmentController.list(survey).forEach { attachmentController.delete(it, userId) }
        surveyDAO.delete(survey)
    }
}