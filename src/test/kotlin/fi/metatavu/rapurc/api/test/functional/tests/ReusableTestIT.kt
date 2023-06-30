package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.*
import fi.metatavu.rapurc.api.client.models.Unit
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for Survey Reusables API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class ReusableTestIT {

    val reusable = Reusable(
        componentName = "brick house",
        reusableMaterialId = UUID.randomUUID(),
        usability = Usability.gOOD,
        amountAsWaste = 99923.3,
        amount = 1000.0,
        unit = Unit.kG,
        images = arrayOf("http://photo1.png", "http://photo2.png"),
        metadata = Metadata()
    )

    /**
     * Tests survey reusable creation and access rights
     */
    @Test
    fun create() {
        TestBuilder().use { testBuilder ->
            val userAGroup = testBuilder.userA.userGroups.list(member = true).first()
            val userBGroup = testBuilder.userB.userGroups.list(member = true).first()

            val survey1 = testBuilder.userA.surveys.create(userAGroup.id!!, SurveyStatus.dRAFT)
            val survey2 = testBuilder.userB.surveys.create(userBGroup.id!!, SurveyStatus.dRAFT)

            val material = testBuilder.admin.materials.createDefault()

            testBuilder.userA.reusables.assertCreateFailStatus(403, survey2.id!!, reusable)
            testBuilder.userA.reusables.assertCreateFailStatus(404, survey1.id!!, reusable)
            val createdReusable = testBuilder.userA.reusables.create(survey1.id, reusable.copy(reusableMaterialId = material.id!!))
            assertNotNull(createdReusable!!.id)
            assertNotNull(createdReusable.metadata.createdAt)
            assertEquals(reusable.componentName, createdReusable.componentName)
            assertEquals(reusable.usability, createdReusable.usability)
            assertEquals(reusable.amount, createdReusable.amount)
            assertEquals(reusable.unit, createdReusable.unit)
            assertEquals(reusable.amountAsWaste, createdReusable.amountAsWaste)
            assertEquals(2, createdReusable.images!!.size)
        }
    }

    /**
     * Tests survey reusables listing and access rights
     */
    @Test
    fun list() {
        TestBuilder().use { testBuilder ->
            val userAGroup = testBuilder.userA.userGroups.list(member = true).first()
            val userBGroup = testBuilder.userB.userGroups.list(member = true).first()

            val survey1 = testBuilder.userA.surveys.create(userAGroup.id!!, SurveyStatus.dRAFT)
            val survey2 = testBuilder.userB.surveys.create(userBGroup.id!!, SurveyStatus.dRAFT)

            val material = testBuilder.admin.materials.createDefault()
            testBuilder.userA.reusables.create(survey1.id!!, reusable.copy(reusableMaterialId = material.id!!))
            testBuilder.userB.reusables.create(survey2.id!!, reusable.copy(reusableMaterialId = material.id))
            testBuilder.userB.reusables.create(survey2.id, reusable.copy(reusableMaterialId = material.id))

            val listSurveys = testBuilder.admin.surveys.listSurveys(null, null, null, null, null, null, null, null)
            assertEquals(survey2.id, listSurveys[0].id)
            testBuilder.userA.reusables.assertListFailStatus(403, survey2.id)
            testBuilder.userA.reusables.assertCount(1, survey1.id)
            testBuilder.userB.reusables.assertCount(2, survey2.id)
            testBuilder.admin.reusables.assertCount(2, survey2.id)
        }
    }

    /**
     * Tests survey reusables search and access rights
     */
    @Test
    fun find() {
        TestBuilder().use { testBuilder ->
            val userAGroup = testBuilder.userA.userGroups.list(member = true).first()
            val userBGroup = testBuilder.userB.userGroups.list(member = true).first()

            val survey1 = testBuilder.userA.surveys.create(userAGroup.id!!, SurveyStatus.dRAFT)
            val survey2 = testBuilder.userB.surveys.create(userBGroup.id!!, SurveyStatus.dRAFT)

            val material = testBuilder.admin.materials.createDefault()

            val created1 = testBuilder.userA.reusables.create(survey1.id!!, reusable.copy(reusableMaterialId = material.id!!))
            val created2 = testBuilder.userB.reusables.create(survey2.id!!, reusable.copy(reusableMaterialId = material.id))

            testBuilder.userA.reusables.assertFindFailStatus(403, survey2.id, created1!!.id)
            testBuilder.admin.reusables.assertFindFailStatus(403, survey1.id, created2!!.id)
            testBuilder.admin.reusables.assertFindFailStatus(404, UUID.randomUUID(), created1.id)
            testBuilder.admin.reusables.assertFindFailStatus(404, survey1.id, UUID.randomUUID())
            val found1 = testBuilder.userA.reusables.find(survey1.id, created1.id!!)

            assertEquals(created1.id, found1.id)
            assertEquals(created1.unit, found1.unit)
            assertEquals(created1.reusableMaterialId, found1.reusableMaterialId)
            assertEquals(2, found1.images!!.size)
        }
    }

    /**
     * Tests survey reusables updates and access rights
     */
    @Test
    fun update() {
        TestBuilder().use { testBuilder ->
            val userAGroup = testBuilder.userA.userGroups.list(member = true).first()
            val userBGroup = testBuilder.userB.userGroups.list(member = true).first()

            val survey1 = testBuilder.userA.surveys.create(userAGroup.id!!, SurveyStatus.dRAFT)
            val survey2 = testBuilder.userB.surveys.create(userBGroup.id!!, SurveyStatus.dRAFT)

            val material = testBuilder.admin.materials.createDefault()

            val created1 = testBuilder.userA.reusables.create(survey1.id!!, reusable.copy(reusableMaterialId = material.id!!))
            val created2 = testBuilder.userB.reusables.create(survey2.id!!, reusable.copy(reusableMaterialId = material.id))

            val updateData = created1!!.copy(
                componentName = "wooden house",
                usability = Usability.pOOR,
                images = null
            )

            testBuilder.userA.reusables.assertUpdateFailStatus(403, survey2.id, created1.id!!, updateData)
            testBuilder.userA.reusables.assertUpdateFailStatus(403, survey1.id, created2!!.id!!, updateData)
            testBuilder.userA.reusables.assertUpdateFailStatus(404, UUID.randomUUID(), created1.id, updateData)
            testBuilder.userA.reusables.assertUpdateFailStatus(404, survey1.id, UUID.randomUUID(), updateData)
                testBuilder.userA.reusables.assertUpdateFailStatus(404, survey1.id, created1.id, updateData.copy(
                reusableMaterialId = UUID.randomUUID()
            ))
            val updated = testBuilder.userA.reusables.update(survey1.id, created1.id, updateData)

            assertEquals(updateData.id, updated.id)
            assertEquals(updateData.unit, updated.unit)
            assertEquals(updateData.reusableMaterialId, updated.reusableMaterialId)
            assertEquals(updateData.usability, updated.usability)
            assertEquals(updateData.componentName, updated.componentName)
            assertEquals(0, updated.images!!.size)
        }
    }

    /**
     * Tests survey reusables deletion and access rights
     */
    @Test
    fun delete() {
        TestBuilder().use { testBuilder ->
            val userAGroup = testBuilder.userA.userGroups.list(member = true).first()
            val userBGroup = testBuilder.userB.userGroups.list(member = true).first()

            val survey1 = testBuilder.userA.surveys.create(userAGroup.id!!, SurveyStatus.dRAFT)
            val survey2 = testBuilder.userB.surveys.create(userBGroup.id!!, SurveyStatus.dRAFT)

            val material = testBuilder.admin.materials.createDefault()

            val created1 = testBuilder.userA.reusables.create(survey1.id!!, reusable.copy(reusableMaterialId = material.id!!))
            val created2 = testBuilder.userB.reusables.create(survey2.id!!, reusable.copy(reusableMaterialId = material.id))
            val created3 = testBuilder.userB.reusables.create(survey2.id, reusable.copy(reusableMaterialId = material.id))

            testBuilder.userA.reusables.assertDeleteFailStatus(403, survey2.id, created1!!.id!!)
            testBuilder.userA.reusables.assertDeleteFailStatus(403, survey1.id, created2!!.id!!)
            testBuilder.userA.reusables.assertDeleteFailStatus(404, UUID.randomUUID(), created1.id!!)
            testBuilder.userA.reusables.assertDeleteFailStatus(404, survey1.id, UUID.randomUUID())

            testBuilder.userA.reusables.delete(survey1.id, created1.id)
            testBuilder.admin.reusables.delete(survey2.id, created2.id!!)

            testBuilder.admin.reusables.assertCount(0, survey1.id)
            testBuilder.admin.reusables.assertCount(1, survey2.id)
            testBuilder.admin.surveys.delete(survey2)
            testBuilder.admin.reusables.markAsDeleted(created3!!)
            testBuilder.admin.reusables.assertListFailStatus(404, survey2.id)
        }

    }

}