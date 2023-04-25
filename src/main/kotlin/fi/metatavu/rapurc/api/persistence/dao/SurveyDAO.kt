package fi.metatavu.rapurc.api.persistence.dao

import fi.metatavu.rapurc.api.model.SurveyStatus
import fi.metatavu.rapurc.api.model.SurveyType
import fi.metatavu.rapurc.api.persistence.model.Survey
import fi.metatavu.rapurc.api.persistence.model.Survey_
import java.time.LocalDate
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for survey
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class SurveyDAO: AbstractDAO<Survey>() {

    /**
     * Creates new Survey
     *
     * @param id id
     * @param status status
     * @param keycloakGroupId keycloak group id
     * @param type type
     * @param dateUnknown demolition date unknown
     * @param startDate start date
     * @param endDate end date
     * @param additionalInformation additional information
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created survey
     */
    fun create(
        id: UUID,
        status: SurveyStatus,
        keycloakGroupId: UUID,
        type: SurveyType,
        dateUnknown: Boolean?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        additionalInformation: String?,
        creatorId: UUID,
        lastModifierId: UUID
    ): Survey {
        val survey = Survey()
        survey.id = id
        survey.status = status
        survey.keycloakGroupId = keycloakGroupId
        survey.type = type
        survey.dateUnknown = dateUnknown
        survey.startDate = startDate
        survey.endDate = endDate
        survey.additionalInformation = additionalInformation
        survey.creatorId = creatorId
        survey.lastModifierId = lastModifierId
        return persist(survey)
    }

    /**
     * Lists surveys with given filters
     *
     * @param firstResult first result
     * @param maxResults maximum amount of results
     * @param address filter by address
     * @param status filter by status
     * @param type filter by type
     * @param dateUnknown filter by date unknown
     * @param startDate filter after start date
     * @param endDate filter before end date
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
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<Survey> = criteriaBuilder.createQuery(Survey::class.java)
        val root: Root<Survey> = criteria.from(Survey::class.java)

        val restrictions = ArrayList<Predicate>()

        if (address != null) {
            TODO("Not yet implemented")
        }

        if (status != null) {
            restrictions.add(criteriaBuilder.equal(root.get(Survey_.status), status))
        }

        if (type != null) {
            restrictions.add(criteriaBuilder.equal(root.get(Survey_.type), type))
        }
                
        if (dateUnknown != null) {
            restrictions.add(criteriaBuilder.equal(root.get(Survey_.dateUnknown), dateUnknown))
        }

        if (startDate != null) {
            restrictions.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Survey_.startDate), startDate))
        }

        if (endDate != null) {
            restrictions.add(criteriaBuilder.lessThanOrEqualTo(root.get(Survey_.endDate), endDate))
        }

        if (keycloakGroupId != null) {
            restrictions.add(criteriaBuilder.equal(root.get(Survey_.keycloakGroupId), keycloakGroupId))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())
        criteria.orderBy(criteriaBuilder.desc(root.get(Survey_.modifiedAt)))
        val query: TypedQuery<Survey> = entityManager.createQuery(criteria)
        query.firstResult = firstResult
        query.maxResults = maxResults
        return query.resultList
    }

    /**
     * Updates start date
     *
     * @param survey survey to update
     * @param startDate new start date
     * @param lastModifierId last modifier's id
     * @return updated survey
     */
    fun updateStartDate(survey: Survey, startDate: LocalDate?, lastModifierId: UUID): Survey {
        survey.startDate = startDate
        survey.lastModifierId = lastModifierId
        return persist(survey)
    }

    /**
     * Updates end date
     *
     * @param survey survey to update
     * @param endDate new end date
     * @param lastModifierId last modifier's id
     * @return updated survey
     */
    fun updateEndDate(survey: Survey, endDate: LocalDate?, lastModifierId: UUID): Survey {
        survey.endDate = endDate
        survey.lastModifierId = lastModifierId
        return persist(survey)
    }

     /**
     * Updates date unknown
     *
     * @param survey survey to update
     * @param dateUnknown new value
     * @param lastModifierId last modifier's id
     * @return updated survey
     */
    fun updateDateUnknown(survey: Survey, dateUnknown: Boolean?, lastModifierId: UUID): Survey {
        survey.dateUnknown = dateUnknown
        survey.lastModifierId = lastModifierId
        return persist(survey)
    }
    
    /**
     * Updates status
     *
     * @param survey survey to update
     * @param status new status
     * @param lastModifierId last modifier's id
     * @return updated survey
     */
    fun updateStatus(survey: Survey, status: SurveyStatus, lastModifierId: UUID): Survey {
        survey.status = status
        survey.lastModifierId = lastModifierId
        return persist(survey)
    }

    /**
     * Updates survey with only modifier id
     *
     * @param survey survey to update
     * @param lastModifierId last modifier's id
     * @return updated survey
     */
    fun update(survey: Survey, lastModifierId: UUID): Survey {
        survey.lastModifierId = lastModifierId
        return persist(survey)
    }

    /**
     * Updates additional information
     *
     * @param survey survey to update
     * @param additionalInformation new additional information
     * @param lastModifierId last modifier's id
     * @return updated survey
     */
    fun updateAdditionalInformation(survey: Survey, additionalInformation: String?, lastModifierId: UUID): Survey {
        survey.additionalInformation = additionalInformation
        survey.lastModifierId = lastModifierId
        return persist(survey)
    }

}