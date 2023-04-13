package fi.metatavu.rapurc.api.persistence.dao

import fi.metatavu.rapurc.api.persistence.model.*
import java.time.LocalDate
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for Surveyor JPA object
 */
@ApplicationScoped
class SurveyorDAO: AbstractDAO<Surveyor>() {

    /**
     * Creates a surveyor
     *
     * @param id id
     * @param survey survey
     * @param firstName surveyors first name
     * @param lastName surveyors last name
     * @param company surveyors company
     * @param role surveyors role
     * @param phone surveyors phone number
     * @param email surveyors email
     * @param reportDate surveyors report date
     * @param propertyName property name
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created surveyor
     */
    fun create(
        id: UUID?,
        survey: Survey,
        firstName: String,
        lastName: String,
        company: String,
        role: String?,
        phone: String,
        email: String?,
        reportDate: LocalDate?,
        visits: String?,
        creatorId: UUID,
        lastModifierId: UUID
    ) : Surveyor {
        val surveyor = Surveyor()
        surveyor.id = id
        surveyor.survey = survey
        surveyor.firstName = firstName
        surveyor.lastName = lastName
        surveyor.company = company
        surveyor.role = role
        surveyor.phone = phone
        surveyor.email = email
        surveyor.reportDate = reportDate
        surveyor.visits = visits
        surveyor.creatorId = creatorId
        surveyor.lastModifierId = lastModifierId
        return persist(surveyor)
    }

    /**
     * Lists surveyors added in survey
     *
     * @param survey filter by survey
     * @return filtered surveyors list
     */
    fun list(survey: Survey): List<Surveyor> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<Surveyor> = criteriaBuilder.createQuery(Surveyor::class.java)
        val root: Root<Surveyor> = criteria.from(Surveyor::class.java)

        criteria.select(root)
        val restrictions = ArrayList<Predicate>()

        restrictions.add(criteriaBuilder.equal(root.get(Surveyor_.survey), survey))

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())
        criteria.orderBy(criteriaBuilder.asc(root.get(Surveyor_.createdAt)))
        val query: TypedQuery<Surveyor> = entityManager.createQuery(criteria)
        return query.resultList
    }

    /**
     * Updates first name of the surveyor
     *
     * @param surveyorToUpdate surveyor to update
     * @return updated surveyor
     */
    fun updateSurveyor(surveyorToUpdate: Surveyor): Surveyor {
        return persist(surveyorToUpdate)
    }

}