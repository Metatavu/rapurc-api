package fi.metatavu.rapurc.api.impl.buildings

import fi.metatavu.rapurc.api.persistence.dao.BuildingDAO
import fi.metatavu.rapurc.api.persistence.dao.OtherStructureDAO
import fi.metatavu.rapurc.api.persistence.dao.SurveyDAO
import fi.metatavu.rapurc.api.persistence.model.Building
import fi.metatavu.rapurc.api.persistence.model.BuildingType
import fi.metatavu.rapurc.api.persistence.model.Survey
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for Buildings and included Other Structures
 */
@ApplicationScoped
class BuildingController {

    @Inject
    lateinit var buildingDAO: BuildingDAO

    @Inject
    lateinit var otherStuctureDao: OtherStructureDAO

    @Inject
    lateinit var surveyDAO: SurveyDAO

    /**
     * Lists buildings based on survey
     *
     * @param survey survey
     * @param buildingType building type
     * @return filtered building list
     */
    fun list(survey: Survey?, buildingType: BuildingType?): List<Building> {
        return buildingDAO.list(survey, buildingType)
    }

    /**
     * Creates building
     *
     * @param survey survey which the building belongs to
     * @param building building data
     * @param buildingType new building type
     * @param creatorId creator id
     * @return created Building
     */
    fun create(survey: Survey, building: fi.metatavu.rapurc.api.model.Building, buildingType: BuildingType?, creatorId: UUID): Building {
        val createdBuilding = buildingDAO.create(
            id = UUID.randomUUID(),
            survey = survey,
            propertyId = building.propertyId,
            buildingId = building.buildingId,
            buildingType = buildingType,
            constructionYear = building.constructionYear,
            space = building.space,
            volume = building.volume,
            floors = building.floors,
            basements = building.basements,
            foundation = building.foundation,
            supportStructure = building.supportingStructure,
            facadeMaterial = building.facadeMaterial,
            roofType = building.roofType,
            streetAddress = building.address?.streetAddress,
            propertyName = building.propertyName,
            city = building.address?.city,
            postCode = building.address?.postCode,
            creatorId = creatorId,
            lastModifierId = creatorId
        )

        building.otherStructures?.forEach { otherStructure ->
            otherStuctureDao.create(
                id = UUID.randomUUID(),
                name = otherStructure.name,
                description = otherStructure.description,
                building = createdBuilding,
                creatorId = creatorId,
                modifierId = creatorId
            )
        }

        surveyDAO.update(survey, creatorId)
        return createdBuilding
    }

    /**
     * Finds building in the db
     *
     * @param buildingId building id
     * @return found building or null
     */
    fun find(buildingId: UUID): Building? {
        return buildingDAO.findById(buildingId)
    }

    /**
     * Updates building
     *
     * @param buildingToUpdate original building to update
     * @param building new building data
     * @param newBuildingType new building type
     * @param userId modifier id
     * @return update Building
     */
    fun update(
        buildingToUpdate: Building,
        building: fi.metatavu.rapurc.api.model.Building,
        newBuildingType: BuildingType?,
        userId: UUID
    ): Building {
        val result = buildingDAO.updatePropertyId(buildingToUpdate, building.propertyId, userId)
        buildingDAO.updateBuildingId(result, building.buildingId, userId)
        buildingDAO.updateBuildingType(result, newBuildingType, userId)
        buildingDAO.updateConstructionYear(result, building.constructionYear, userId)
        buildingDAO.updateSpace(result, building.space, userId)
        buildingDAO.updateVolume(result, building.volume, userId)
        buildingDAO.updateFloors(result, building.floors, userId)
        buildingDAO.updateBasements(result, building.basements, userId)
        buildingDAO.updateFoundation(result, building.foundation, userId)
        buildingDAO.updateSupportStructure(result, building.supportingStructure, userId)
        buildingDAO.updateFacadeMateria(result, building.facadeMaterial, userId)
        buildingDAO.updateRoofType(result, building.roofType, userId)
        buildingDAO.updatePropertyName(result, building.propertyName, userId)
        buildingDAO.updateStreetAddress(result, building.address?.streetAddress, userId)
        buildingDAO.updateCity(result, building.address?.city, userId)
        buildingDAO.updatePostCode(result, building.address?.postCode, userId)

        otherStuctureDao.listByBuilding(buildingToUpdate)?.forEach(otherStuctureDao::delete)
        building.otherStructures?.forEach { otherStructure ->
            otherStuctureDao.create(
                id = UUID.randomUUID(),
                name = otherStructure.name,
                description = otherStructure.description,
                building = result,
                creatorId = userId,
                modifierId = userId
            )
        }

        surveyDAO.update(buildingToUpdate.survey!!, userId)
        return result
    }

    /**
     * Deletes building
     *
     * @param buildingToDelete building to delete
     * @param userId user id
     */
    fun delete(buildingToDelete: Building, userId: UUID) {
        otherStuctureDao.listByBuilding(buildingToDelete)?.forEach(otherStuctureDao::delete)
        buildingDAO.delete(buildingToDelete)
        surveyDAO.update(buildingToDelete.survey!!, userId)
    }

}
