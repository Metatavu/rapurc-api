package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.Metadata
import fi.metatavu.rapurc.api.client.models.Waste
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for Waste API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class WasteTestIT {

    /**
     * Tests waste creation
     */
    @Test
    fun create() {
        TestBuilder().use {
            val usage = it.admin.usages.create()

            val userAGroup = it.userA.userGroups.list(member = true).first()
            val userBGroup = it.userB.userGroups.list(member = true).first()

            val surveyA = it.userA.surveys.create(userAGroup.id!!)
            val surveyB = it.userB.surveys.create(userBGroup.id!!)
            val wasteCategory = it.admin.wasteCategories.createDefault()
            val wasteMaterial = it.admin.wasteMaterials.create(wasteCategoryId = wasteCategory.id!!)

            val waste = Waste(
                wasteMaterialId = wasteMaterial.id!!,
                amount = 100.0,
                metadata = Metadata(),
                usageId = usage.id!!,
                description = "waste"
            )

            val createdWaste = it.userA.wastes.create(
                surveyId = surveyA.id!!,
                waste = waste
            )

            assertEquals(waste.amount, createdWaste.amount)
            assertEquals(usage.id, createdWaste.usageId)
            assertEquals(wasteMaterial.id, createdWaste.wasteMaterialId)
            assertEquals(waste.description, createdWaste.description)

            it.userA.wastes.assertCreateFailStatus(404, surveyA.id, createdWaste.copy(wasteMaterialId = UUID.randomUUID()))
            it.userA.wastes.assertCreateFailStatus(404, surveyB.id, createdWaste.copy(wasteMaterialId = UUID.randomUUID()))
            it.userA.wastes.assertCreateFailStatus(404, surveyB.id, createdWaste.copy(usageId = UUID.randomUUID()))
        }
    }

    /**
     * Tests waste listing
     */
    @Test
    fun list() {
        TestBuilder().use {
            val usage = it.admin.usages.create()
            val userAGroup = it.userA.userGroups.list(member = true).first()
            val userBGroup = it.userB.userGroups.list(member = true).first()

            val surveyA1 = it.userA.surveys.create(userAGroup.id!!)
            val surveyA2 = it.userA.surveys.create(userAGroup.id!!)

            val wasteCategory = it.admin.wasteCategories.createDefault()
            val wasteMaterial = it.admin.wasteMaterials.create(wasteCategoryId = wasteCategory.id!!)

            it.userA.wastes.create(
                surveyId = surveyA1.id!!,
                wasteMaterialId = wasteMaterial.id!!,
                usageId = usage.id!!
            )

            it.userA.wastes.create(
                surveyId = surveyA2.id!!,
                wasteMaterialId = wasteMaterial.id,
                usageId = usage.id
            )

            assertEquals(1,it.userA.wastes.list(surveyA1.id).size)
            assertEquals(1,it.userA.wastes.list(surveyA2.id).size)
            it.userB.wastes.assertListFailStatus(403, surveyA1.id)
        }
    }

    /**
     * Tests waste search
     */
    @Test
    fun find() {
        TestBuilder().use {
            val usage = it.admin.usages.create()
            val userAGroup = it.userA.userGroups.list(member = true).first()
            val userBGroup = it.userB.userGroups.list(member = true).first()

            val surveyA = it.userA.surveys.create(userAGroup.id!!)
            val surveyB = it.userB.surveys.create(userBGroup.id!!)
            val wasteCategory = it.admin.wasteCategories.createDefault()
            val wasteMaterial = it.admin.wasteMaterials.create(wasteCategoryId = wasteCategory.id!!)

            val createdWaste = it.userA.wastes.create(
                surveyId = surveyA.id!!,
                wasteMaterialId = wasteMaterial.id!!,
                usageId = usage.id!!
            )

            val foundWaste = it.userA.wastes.findWaste(surveyId = surveyA.id, wasteId = createdWaste.id!!)

            assertEquals(createdWaste.id, foundWaste.id)
            assertEquals(createdWaste.amount, foundWaste.amount)
            assertEquals(createdWaste.wasteMaterialId, foundWaste.wasteMaterialId)
            assertEquals(createdWaste.description, foundWaste.description)
            assertEquals(createdWaste.usageId, foundWaste.usageId)

            it.userA.wastes.assertFindFailStatus(expectedStatus = 403, surveyId = surveyB.id, wasteId = createdWaste.id)
            it.userA.wastes.assertFindFailStatus(expectedStatus = 404, surveyId = UUID.randomUUID(), wasteId = createdWaste.id)
            it.userA.wastes.assertFindFailStatus(expectedStatus = 404, surveyId = surveyA.id, wasteId = UUID.randomUUID())
        }
    }

    /**
     * Tests waste updates
     */
    @Test
    fun update() {
        TestBuilder().use {
            val usage = it.admin.usages.create()
            val userAGroup = it.userA.userGroups.list(member = true).first()
            val userBGroup = it.userB.userGroups.list(member = true).first()

            val surveyA = it.userA.surveys.create(userAGroup.id!!)
            val surveyB = it.userB.surveys.create(userBGroup.id!!)
            val wasteCategory = it.admin.wasteCategories.createDefault()
            val wasteMaterial = it.admin.wasteMaterials.create(wasteCategoryId = wasteCategory.id!!)

            val createdWaste = it.userA.wastes.create(
                surveyId = surveyA.id!!,
                wasteMaterialId = wasteMaterial.id!!,
                usageId = usage.id!!
            )

            val updateData = createdWaste.copy(
                amount = 1000.0,
                description = "update data"
            )

            val updatedWaste = it.userA.wastes.update(surveyId = surveyA.id, wasteId = createdWaste.id!!, waste = updateData)

            assertEquals(updateData.id, updatedWaste.id)
            assertEquals(updateData.amount, updatedWaste.amount)
            assertEquals(updateData.wasteMaterialId, updatedWaste.wasteMaterialId)
            assertEquals(updateData.description, updatedWaste.description)
            assertEquals(updateData.usageId, updatedWaste.usageId)

            it.userA.wastes.assertUpdateFailStatus(expectedStatus = 403, surveyId = surveyB.id!!, wasteId = createdWaste.id, waste = updatedWaste)
            it.userA.wastes.assertUpdateFailStatus(expectedStatus = 404, surveyId = UUID.randomUUID(), wasteId = createdWaste.id, waste = updatedWaste)
            it.userA.wastes.assertUpdateFailStatus(expectedStatus = 404, surveyId = surveyA.id, wasteId = UUID.randomUUID(), waste = updatedWaste)
            it.userA.wastes.assertUpdateFailStatus(expectedStatus = 404, surveyId = surveyA.id, wasteId = UUID.randomUUID(), waste = updatedWaste.copy(wasteMaterialId = UUID.randomUUID()))
            it.userA.wastes.assertUpdateFailStatus(expectedStatus = 404, surveyId = surveyA.id, wasteId = UUID.randomUUID(), waste = updatedWaste.copy(usageId = UUID.randomUUID()))
        }
    }

    /**
     * Tests waste deletion and access rights
     */
    @Test
    fun delete() {
        TestBuilder().use {
            val usage = it.admin.usages.create()
            val userAGroup = it.userA.userGroups.list(member = true).first()
            val userBGroup = it.userB.userGroups.list(member = true).first()

            val surveyA = it.userA.surveys.create(userAGroup.id!!)
            val surveyB = it.userB.surveys.create(userBGroup.id!!)
            val wasteCategory = it.admin.wasteCategories.createDefault()
            val wasteMaterial = it.admin.wasteMaterials.create(wasteCategoryId = wasteCategory.id!!)

            val createdWaste = it.userA.wastes.create(
                surveyId = surveyA.id!!,
                wasteMaterialId = wasteMaterial.id!!,
                usageId = usage.id!!
            )
            val createdWasteB = it.userA.wastes.create(
                surveyId = surveyB.id!!,
                wasteMaterialId = wasteMaterial.id,
                usageId = usage.id
            )

            it.userB.wastes.assertDeleteFailStatus(403, surveyB.id, createdWaste.id!!)
            it.userA.wastes.assertDeleteFailStatus(404, UUID.randomUUID(), createdWaste.id)

            it.userA.wastes.delete(surveyA.id, createdWaste.id)
            assertEquals(0, it.userA.wastes.list(surveyA.id).size)
            it.admin.surveys.delete(surveyB)
            it.admin.wastes.markAsDeleted(createdWasteB)
            it.userB.wastes.assertListFailStatus(404, surveyB.id)
        }
    }
}