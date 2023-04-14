package fi.metatavu.rapurc.api.persistence.dao

import fi.metatavu.rapurc.api.persistence.model.*
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for Building JPA object
 */
@ApplicationScoped
class BuildingDAO: AbstractDAO<Building>() {

    /**
     * Creates a building
     *
     * @param id id
     * @param survey survey
     * @param propertyId property id
     * @param buildingId building id
     * @param buildingType building type
     * @param constructionYear construction year
     * @param space space of building
     * @param volume volume of building
     * @param floors number of floors
     * @param basements number of basements
     * @param foundation foundation
     * @param supportStructure support structure
     * @param facadeMaterial facade material
     * @param roofType roof type
     * @param propertyName property name
     * @param streetAddress street address
     * @param city city
     * @param postCode post code
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created building
     */
    fun create(
        id: UUID?,
        survey: Survey,
        propertyId: String?,
        buildingId: String?,
        buildingType: BuildingType?,
        constructionYear: Int?,
        space: Int?,
        volume: Int?,
        floors: String?,
        basements: Int?,
        foundation: String?,
        supportStructure: String?,
        facadeMaterial: String?,
        roofType: String?,
        propertyName: String?,
        streetAddress: String?,
        city: String?,
        postCode: String?,
        creatorId: UUID,
        lastModifierId: UUID
    ) : Building {
        val building = Building()
        building.id = id
        building.survey = survey
        building.propertyId = propertyId
        building.buildingId = buildingId
        building.buildingType = buildingType
        building.constructionYear = constructionYear
        building.space = space
        building.volume = volume
        building.floors = floors
        building.basements = basements
        building.foundation = foundation
        building.supportStructure = supportStructure
        building.facadeMaterial = facadeMaterial
        building.roofType = roofType
        building.propertyName = propertyName
        building.streetAddress = streetAddress
        building.city = city
        building.postCode = postCode
        building.creatorId = creatorId
        building.lastModifierId = lastModifierId
        return persist(building)
    }

    /**
     * Lists buildings added in survey
     *
     * @param survey survey
     * @param buildingType building type
     * @return filtered buildings list
     */
    fun list(survey: Survey?, buildingType: BuildingType?): List<Building> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<Building> = criteriaBuilder.createQuery(Building::class.java)
        val root: Root<Building> = criteria.from(Building::class.java)

        criteria.select(root)
        val restrictions = ArrayList<Predicate>()

        if (survey != null) {
            restrictions.add(criteriaBuilder.equal(root.get(Building_.survey), survey))
        }

        if (buildingType != null) {
            restrictions.add(criteriaBuilder.equal(root.get(Building_.buildingType), buildingType))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())
        criteria.orderBy(criteriaBuilder.asc(root.get(Building_.createdAt)))
        val query: TypedQuery<Building> = entityManager.createQuery(criteria)
        return query.resultList
    }

    /**
     * Updates property id of the building
     *
     * @param building building to update
     * @param propertyId new property id
     * @param modifierId modifier id
     * @return updated building
     */
    fun updatePropertyId(building: Building, propertyId: String?, modifierId: UUID): Building {
        building.propertyId = propertyId
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates building id of the building
     *
     * @param building building to update
     * @param buildingId new building id
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateBuildingId(building: Building, buildingId: String?, modifierId: UUID): Building {
        building.buildingId = buildingId
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates building type of the building
     *
     * @param building building to update
     * @param buildingType new building type
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateBuildingType(building: Building, buildingType: BuildingType?, modifierId: UUID): Building {
        building.buildingType = buildingType
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates construction year of the building
     *
     * @param building building to update
     * @param constructionYear new construction year
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateConstructionYear(building: Building, constructionYear: Int?, modifierId: UUID): Building {
        building.constructionYear = constructionYear
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates space of the building
     *
     * @param building building to update
     * @param space new space
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateSpace(building: Building, space: Int?, modifierId: UUID): Building {
        building.space = space
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates volume of the building
     *
     * @param building building to update
     * @param volume new volume
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateVolume(building: Building, volume: Int?, modifierId: UUID): Building {
        building.volume = volume
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates floors of the building
     *
     * @param building building to update
     * @param floors new floors
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateFloors(building: Building, floors: String?, modifierId: UUID): Building {
        building.floors = floors
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates basements of the building
     *
     * @param building building to update
     * @param basements new basements
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateBasements(building: Building, basements: Int?, modifierId: UUID): Building {
        building.basements = basements
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates foundation of the building
     *
     * @param building building to update
     * @param foundation new foundation
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateFoundation(building: Building, foundation: String?, modifierId: UUID): Building {
        building.foundation = foundation
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates supporting structure of the building
     *
     * @param building building to update
     * @param supportingStructure new supporting structure
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateSupportStructure(building: Building, supportingStructure: String?, modifierId: UUID): Building {
        building.supportStructure = supportingStructure
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates facade material of the building
     *
     * @param building building to update
     * @param facadeMaterial new facade material
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateFacadeMateria(building: Building, facadeMaterial: String?, modifierId: UUID): Building {
        building.facadeMaterial = facadeMaterial
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates oof type of the building
     *
     * @param building building to update
     * @param roofType new roof type
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateRoofType(building: Building, roofType: String?, modifierId: UUID): Building {
        building.roofType = roofType
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates name of the building
     *
     * @param building building to update
     * @param propertyName new property name
     * @param modifierId modifier id
     * @return updated building
     */
    fun updatePropertyName(building: Building, propertyName: String?, modifierId: UUID): Building {
        building.propertyName = propertyName
        building.lastModifierId = modifierId
        return persist(building)
    }
    
    /**
     * Updates street address of the building
     *
     * @param building building to update
     * @param streetAddress new street address
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateStreetAddress(building: Building, streetAddress: String?, modifierId: UUID): Building {
        building.streetAddress = streetAddress
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates city of the building
     *
     * @param building building to update
     * @param city new city
     * @param modifierId modifier id
     * @return updated building
     */
    fun updateCity(building: Building, city: String?, modifierId: UUID): Building {
        building.city = city
        building.lastModifierId = modifierId
        return persist(building)
    }

    /**
     * Updates postCode of the building
     *
     * @param building building to update
     * @param postCode new postCode
     * @param modifierId modifier id
     * @return updated building
     */
    fun updatePostCode(building: Building, postCode: String?, modifierId: UUID): Building {
        building.postCode = postCode
        building.lastModifierId = modifierId
        return persist(building)
    }

}