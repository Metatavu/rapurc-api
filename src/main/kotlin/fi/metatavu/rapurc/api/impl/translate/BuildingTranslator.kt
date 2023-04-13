package fi.metatavu.rapurc.api.impl.translate

import fi.metatavu.rapurc.api.model.Address
import fi.metatavu.rapurc.api.model.OtherStructure
import fi.metatavu.rapurc.api.persistence.dao.OtherStructureDAO
import fi.metatavu.rapurc.api.persistence.model.Building
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translates JPA Building to REST Building
 */
@ApplicationScoped
class BuildingTranslator: AbstractTranslator<Building, fi.metatavu.rapurc.api.model.Building>() {

    @Inject
    lateinit var otherStructureDAO: OtherStructureDAO

    @Inject
    lateinit var metadataTranslator: MetadataTranslator

    override fun translate(entity: Building): fi.metatavu.rapurc.api.model.Building {
        val result = fi.metatavu.rapurc.api.model.Building()
        result.id = entity.id
        result.surveyId = entity.survey?.id
        result.propertyId = entity.propertyId
        result.buildingId = entity.buildingId
        result.buildingTypeId = entity.buildingType?.id
        result.constructionYear = entity.constructionYear
        result.space = entity.space
        result.volume = entity.volume
        result.floors = entity.floors
        result.basements = entity.basements
        result.foundation = entity.foundation
        result.supportingStructure = entity.supportStructure
        result.facadeMaterial = entity.facadeMaterial
        result.roofType = entity.roofType
        result.propertyName = entity.propertyName
        result.metadata = metadataTranslator.translate(entity)

        result.otherStructures = otherStructureDAO.listByBuilding(building = entity)?.map { jpaStructure ->
            OtherStructure().name(jpaStructure.name).description(jpaStructure.description)
        }

        if (entity.city != null && entity.streetAddress != null && entity.postCode != null) {
            val address = Address()
            address.city = entity.city
            address.streetAddress = entity.streetAddress
            address.postCode = entity.postCode
            result.address = address
        }

        return result
    }

}
