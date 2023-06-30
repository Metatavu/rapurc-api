package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.Metadata
import fi.metatavu.rapurc.api.client.models.HazardousWaste
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for Hazardous Waste API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class HazardousWasteTestIT {

    /**
     * Tests hazardous waste creation
     */
    @Test
    fun create() {
        TestBuilder().use {
            val surveyA = it.userA.surveys.create(it.userA)
            val surveyB = it.userB.surveys.create(it.userB)
            val wasteCategory = it.admin.wasteCategories.createDefault()
            val hazardousMaterial = it.admin.hazMaterials.create(wasteCategoryId = wasteCategory.id!!)
            val wastespecifier = it.admin.wasteSpecifiers.create()

            val hazardousWaste = HazardousWaste(
                hazardousMaterialId = hazardousMaterial.id!!,
                amount = 100.0,
                metadata = Metadata(),
                wasteSpecifierId = wastespecifier.id!!,
                description = "waste"
            )

            val hazardousWasteNullSpecifier = HazardousWaste(
                hazardousMaterialId = hazardousMaterial.id!!,
                amount = 100.0,
                metadata = Metadata(),
                wasteSpecifierId = null,
                description = "waste"
            )

            val createdWaste = it.userA.hazWastes.create(
                surveyId = surveyA.id!!,
                waste = hazardousWaste
            )

            val createdWasteNullSpecifier = it.userA.hazWastes.create(
                surveyId = surveyA.id!!,
                waste = hazardousWasteNullSpecifier
            )

            assertEquals(hazardousWaste.amount, createdWaste.amount)
            assertEquals(wastespecifier.id, createdWaste.wasteSpecifierId)
            assertEquals(hazardousMaterial.id, createdWaste.hazardousMaterialId)
            assertEquals(hazardousWaste.description, createdWaste.description)
            assertNull(createdWasteNullSpecifier.wasteSpecifierId)

            it.userA.hazWastes.assertCreateFailStatus(404, surveyA.id, createdWaste.copy(hazardousMaterialId = UUID.randomUUID()))
            it.userA.hazWastes.assertCreateFailStatus(404, surveyA.id, createdWaste.copy(wasteSpecifierId = UUID.randomUUID()))
            it.userA.hazWastes.assertCreateFailStatus(403, surveyB.id, createdWaste)
        }
    }

    /**
     * Tests hazardous waste listing and ordering
     */
    @Test
    fun list() {
        TestBuilder().use {
            val surveyA1 = it.userA.surveys.create(it.userA)
            val surveyA2 = it.userA.surveys.create(it.userA)
            val wasteCategory = it.admin.wasteCategories.createDefault()
            val hazardousMaterial = it.admin.hazMaterials.create(wasteCategoryId = wasteCategory.id!!)
            val wasteSpecifier = it.admin.wasteSpecifiers.create()

            it.userA.hazWastes.create(
                surveyId = surveyA1.id!!,
                hazardousMaterialId = hazardousMaterial.id!!,
                wasteSpecifierId = wasteSpecifier.id!!
            )

            val createdWaste = it.userA.hazWastes.create(
                surveyId = surveyA2.id!!,
                hazardousMaterialId = hazardousMaterial.id,
                wasteSpecifierId = wasteSpecifier.id
            )

            it.userA.hazWastes.create(
                surveyId = surveyA2.id,
                hazardousMaterialId = hazardousMaterial.id,
                wasteSpecifierId = wasteSpecifier.id
            )

            assertEquals(1,it.userA.hazWastes.list(surveyA1.id).size)
            assertEquals(2,it.userA.hazWastes.list(surveyA2.id).size)
            assertEquals(createdWaste.id, it.userA.hazWastes.list(surveyA2.id)[0].id)
            it.userB.hazWastes.assertListFailStatus(403, surveyA1.id)
        }
    }

    /**
     * Tests hazardous waste search
     */
    @Test
    fun find() {
        TestBuilder().use {
            val surveyA = it.userA.surveys.create(it.userA)
            val surveyB = it.userB.surveys.create(it.userB)
            val wasteCategory = it.admin.wasteCategories.createDefault()
            val hazardousMaterial = it.admin.hazMaterials.create(wasteCategoryId = wasteCategory.id!!)
            val wasteSpecifier = it.admin.wasteSpecifiers.create()

            val createdWaste = it.userA.hazWastes.create(
                surveyId = surveyA.id!!,
                hazardousMaterialId = hazardousMaterial.id!!,
                wasteSpecifierId = wasteSpecifier.id!!
            )

            val foundWaste = it.userA.hazWastes.findWaste(surveyId = surveyA.id, wasteId = createdWaste.id!!)

            assertEquals(createdWaste.id, foundWaste.id)
            assertEquals(createdWaste.amount, foundWaste.amount)
            assertEquals(createdWaste.hazardousMaterialId, foundWaste.hazardousMaterialId)
            assertEquals(createdWaste.description, foundWaste.description)
            assertEquals(createdWaste.wasteSpecifierId, foundWaste.wasteSpecifierId)

            it.userA.hazWastes.assertFindFailStatus(expectedStatus = 403, surveyId = surveyB.id, hazardousWasteId = createdWaste.id)
            it.userA.hazWastes.assertFindFailStatus(expectedStatus = 404, surveyId = UUID.randomUUID(), hazardousWasteId = createdWaste.id)
            it.userA.hazWastes.assertFindFailStatus(expectedStatus = 404, surveyId = surveyA.id, hazardousWasteId = UUID.randomUUID())
        }
    }

    /**
     * Tests hazardous waste updates
     */
    @Test
    fun update() {
        TestBuilder().use {
            val surveyA = it.userA.surveys.create(it.userA)
            val surveyB = it.userB.surveys.create(it.userB)
            val wasteCategory = it.admin.wasteCategories.createDefault()
            val hazardousMaterial = it.admin.hazMaterials.create(wasteCategoryId = wasteCategory.id!!)
            val wasteSpecifier = it.admin.wasteSpecifiers.create()

            val createdWaste = it.userA.hazWastes.create(
                surveyId = surveyA.id!!,
                hazardousMaterialId = hazardousMaterial.id!!,
                wasteSpecifierId = wasteSpecifier.id!!
            )

            val updateData = createdWaste.copy(
                amount = 1000.0,
                wasteSpecifierId = null,
                description = "update data"
            )

            val updatedWaste = it.userA.hazWastes.update(surveyId = surveyA.id, hazardousWasteId = createdWaste.id!!, hazardousWaste = updateData)

            assertEquals(updateData.id, updatedWaste.id)
            assertEquals(updateData.amount, updatedWaste.amount)
            assertEquals(updateData.hazardousMaterialId, updatedWaste.hazardousMaterialId)
            assertEquals(updateData.description, updatedWaste.description)
            assertEquals(updateData.wasteSpecifierId, updatedWaste.wasteSpecifierId)

            it.userA.hazWastes.assertUpdateFailStatus(expectedStatus = 403, surveyId = surveyB.id!!, hazardousWasteId = createdWaste.id, waste = updatedWaste)
            it.userA.hazWastes.assertUpdateFailStatus(expectedStatus = 404, surveyId = UUID.randomUUID(), hazardousWasteId = createdWaste.id, waste = updatedWaste)
            it.userA.hazWastes.assertUpdateFailStatus(expectedStatus = 404, surveyId = surveyA.id, hazardousWasteId = UUID.randomUUID(), waste = updatedWaste)
            it.userA.hazWastes.assertUpdateFailStatus(expectedStatus = 404, surveyId = surveyA.id, hazardousWasteId = UUID.randomUUID(), waste = updatedWaste.copy(hazardousMaterialId = UUID.randomUUID()))
            it.userA.hazWastes.assertUpdateFailStatus(expectedStatus = 404, surveyId = surveyA.id, hazardousWasteId = UUID.randomUUID(), waste = updatedWaste.copy(wasteSpecifierId = UUID.randomUUID()))
        }
    }

    /**
     * Tests hazardous waste deletion and access rights
     */
    @Test
    fun delete() {
        TestBuilder().use {
            val surveyA = it.userA.surveys.create(it.userA)
            val surveyB = it.userB.surveys.create(it.userB)
            val wasteCategory = it.admin.wasteCategories.createDefault()
            val hazardousMaterial = it.admin.hazMaterials.create(wasteCategoryId = wasteCategory.id!!)
            val wasteSpecifier = it.admin.wasteSpecifiers.create()

            val createdWasteA = it.userA.hazWastes.create(
                surveyId = surveyA.id!!,
                hazardousMaterialId = hazardousMaterial.id!!,
                wasteSpecifierId = wasteSpecifier.id!!
            )

            val createdWasteB = it.userB.hazWastes.create(
                surveyId = surveyB.id!!,
                hazardousMaterialId = hazardousMaterial.id,
                wasteSpecifierId = wasteSpecifier.id
            )

            it.userB.hazWastes.assertDeleteFailStatus(403, surveyB.id, createdWasteA.id!!)
            it.userA.hazWastes.assertDeleteFailStatus(404, UUID.randomUUID(), createdWasteA.id)

            it.userA.hazWastes.delete(surveyA.id, createdWasteA.id)
            assertEquals(0, it.userA.hazWastes.list(surveyA.id).size)
            it.admin.surveys.delete(surveyB)
            it.admin.hazWastes.markAsDeleted(createdWasteB)
            it.userB.hazWastes.assertListFailStatus(404, surveyB.id)
        }
    }
}