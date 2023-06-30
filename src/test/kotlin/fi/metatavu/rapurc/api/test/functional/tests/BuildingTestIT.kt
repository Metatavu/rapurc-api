package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.OtherStructure
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for Buildings API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class BuildingTestIT {


    /**
     * Tests building creation
     */
    @Test
    fun create() {
        TestBuilder().use {
            val createdSurveyA1 = it.userA.surveys.create(it.userA)

            it.userB.buildings.assertCreateFailStatus(
                expectedStatus =  403,
                surveyId = createdSurveyA1.id,
                buildingSurveyId = createdSurveyA1.id!!
            )

            val createdBuilding = it.userA.buildings.create(
                surveyId = createdSurveyA1.id,
                buildingSurveyId = createdSurveyA1.id,
                buildingTypeId = null
            )

            assertNotNull(createdBuilding)
            assertEquals(it.userA.buildings.buildingData.propertyId, createdBuilding!!.propertyId)
            assertEquals(it.userA.buildings.buildingData.buildingId, createdBuilding.buildingId)
            assertEquals(it.userA.buildings.buildingData.floors, createdBuilding.floors)
            assertEquals(it.userA.buildings.buildingData.facadeMaterial, createdBuilding.facadeMaterial)

            assertEquals(2, createdBuilding.otherStructures!!.size)
        }
    }

    /**
     * Tests listing buildings
     */
    @Test
    fun list() {
        TestBuilder().use {
            val createdSurveyA1 = it.userA.surveys.create(it.userA)
            val createdSurveyA2 = it.userA.surveys.create(it.userA)
            it.userA.buildings.create(
                surveyId = createdSurveyA1.id!!,
                buildingSurveyId = createdSurveyA1.id,
                buildingTypeId = null
            )

            it.userA.buildings.create(
                surveyId = createdSurveyA2.id!!,
                buildingSurveyId = createdSurveyA2.id,
                buildingTypeId = null
            )

            it.userA.buildings.assertCount(1, createdSurveyA1.id)
            it.userA.buildings.assertCount(1, createdSurveyA2.id)
            it.userA.buildings.assertListFailStatus(404, UUID.randomUUID())
            it.userB.buildings.assertListFailStatus(403, createdSurveyA2.id)
        }
    }

    /**
     * Tests finding buildings
     */
    @Test
    fun find() {
        TestBuilder().use {
            val createdSurveyA1 = it.userA.surveys.create(it.userA)
            val createdSurveyB1 = it.userB.surveys.create(it.userB)
            val building1 = it.userA.buildings.create(
                surveyId = createdSurveyA1.id!!,
                buildingSurveyId = createdSurveyA1.id,
                buildingTypeId = null
            )

            val building2 = it.userB.buildings.create(
                surveyId = createdSurveyB1.id!!,
                buildingSurveyId = createdSurveyB1.id,
                buildingTypeId = null
            )

            val foundBuilding = it.userA.buildings.findBuilding(createdSurveyA1.id, building1!!.id!!)
            assertNotNull(foundBuilding)
            it.userA.buildings.assertFindFailStatus(403, createdSurveyA1.id, building2!!.id!!)
            it.userA.buildings.assertFindFailStatus(404, createdSurveyA1.id, UUID.randomUUID())
            it.userB.buildings.assertFindFailStatus(403, createdSurveyA1.id, building1.id!!)
        }
    }

    /**
     * Tests building updates
     */
    @Test
    fun update() {
        TestBuilder().use {
            val createdSurveyA1 = it.userA.surveys.create(it.userA)
            val building1 = it.userA.buildings.create(
                surveyId = createdSurveyA1.id!!,
                buildingSurveyId = createdSurveyA1.id,
                buildingTypeId = null
            )

            val newBuldingInfo = building1!!.copy(
                propertyId = "property id 2",
                facadeMaterial = "concrete",
                constructionYear = 1993,
                otherStructures = arrayOf(
                    OtherStructure(
                        name = "trash house",
                        description = "bike house"
                    ),
                    OtherStructure(
                        name = "trash house",
                        description = "trash house"
                    )
                )
            )

            val updatedBuilding = it.userA.buildings.update(createdSurveyA1.id, building1.id!!, newBuldingInfo)

            assertNotNull(updatedBuilding)
            assertEquals(2, updatedBuilding.otherStructures!!.size)
            assertEquals(newBuldingInfo.facadeMaterial, updatedBuilding.facadeMaterial)
            assertEquals(newBuldingInfo.propertyId, updatedBuilding.propertyId)
        }
    }

    /**
     * Tests building deletion
     */
    @Test
    fun delete() {
        TestBuilder().use {
            val createdSurveyA1 = it.userA.surveys.create(it.userA)
            val createdSurveyB1 = it.userB.surveys.create(it.userB)
            val building1 = it.userA.buildings.create(
                surveyId = createdSurveyA1.id!!,
                buildingSurveyId = createdSurveyA1.id,
                buildingTypeId = null
            )
            val building2 = it.userB.buildings.create(
                surveyId = createdSurveyB1.id!!,
                buildingSurveyId = createdSurveyB1.id,
                buildingTypeId = null
            )
            val building3 = it.userB.buildings.create(
                surveyId = createdSurveyB1.id,
                buildingSurveyId = createdSurveyB1.id,
                buildingTypeId = null
            )

            it.userA.buildings.assertDeleteFailStatus(403, building2!!)
            it.userA.buildings.delete(building1!!)
            it.userB.buildings.delete(building2)
            it.admin.buildings.assertCount(0, createdSurveyA1.id)
            it.admin.buildings.assertCount(1, createdSurveyB1.id)

            it.userB.surveys.delete(createdSurveyB1)
            it.userB.buildings.markAsDeleted(building3!!)
            it.userB.buildings.assertListFailStatus(404, createdSurveyB1.id)
        }
    }
}