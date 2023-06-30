package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.Surveyor
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Functional test for surveyors
 *
 * @author Jari Nykänen
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class SurveyorTestIT {

    /**
     * Tests surveyor creation
     */
    @Test
    fun create() {
        TestBuilder().use {
            val useAGroup = it.userA.userGroups.list(member = true).first()
            val surveyId = it.userA.surveys.create(useAGroup.id!!).id!!
            val createdSurveyor = it.userA.surveyors.createWithDefaultValues(surveyId = surveyId)
            assertNotNull(createdSurveyor)

            val anotherSurveyor = Surveyor(
                firstName = "First",
                lastName = "Last",
                company = "Company",
                phone = "044123456789"
            )

            it.userB.surveyors.assertCreateFailStatus(
                expectedStatus = 403,
                surveyId = surveyId,
                surveyor = anotherSurveyor
            )
        }
    }

    /**
     * Tests surveyor listing
     */
    @Test
    fun list() {
        TestBuilder().use {
            val useAGroup = it.userA.userGroups.list(member = true).first()

            val surveyId = it.userA.surveys.create(useAGroup.id!!).id!!

            val emptyList = it.admin.surveyors.list(surveyId = surveyId)
            assertEquals(0, emptyList.size)

            it.userA.surveyors.createWithDefaultValues(surveyId = surveyId)
            it.userA.surveyors.createWithDefaultValues(surveyId = surveyId)
            it.userA.surveyors.createWithDefaultValues(surveyId = surveyId)

            val listWithThreeItems = it.admin.surveyors.list(surveyId = surveyId)

            assertEquals(3, listWithThreeItems.size)
        }
    }

    /**
     * Tests finding surveyor
     */
    @Test
    fun find() {
        TestBuilder().use {
            val useAGroup = it.userA.userGroups.list(member = true).first()

            val surveyId = it.userA.surveys.create(useAGroup.id!!).id!!

            val surveyor = it.userA.surveyors.createWithDefaultValues(surveyId = surveyId)
            val foundSurveyor = it.userA.surveyors.findSurveyor(surveyId = surveyId, surveyorId = surveyor!!.id!!)

            assertEquals(surveyor.id, foundSurveyor.id)
            assertEquals(surveyor.firstName, foundSurveyor.firstName)
            assertEquals(surveyor.lastName, foundSurveyor.lastName)
            assertEquals(surveyor.company, foundSurveyor.company)
            assertEquals(surveyor.phone, foundSurveyor.phone)
            assertEquals(surveyor.email, foundSurveyor.email)

            it.admin.surveyors.assertFindFailStatus(expectedStatus = 404, surveyId = surveyId, surveyorId = UUID.randomUUID())
            it.userB.surveyors.assertFindFailStatus(expectedStatus = 403, surveyId = surveyId, surveyorId = surveyor.id)
        }
    }

    /**
     * Tests surveyor updating
     */
    @Test
    fun update() {
        TestBuilder().use {
            val useAGroup = it.userA.userGroups.list(member = true).first()

            val surveyId = it.userA.surveys.create(useAGroup.id!!).id!!
            val surveyor = it.userA.surveyors.createWithDefaultValues(surveyId = surveyId)
            val updateData = surveyor?.copy(
                firstName = "Updated first name",
                lastName = "Updated last name",
                company = "Updated company",
                phone = "044987654321",
                email = "updated@example.com",
                role = "Updated role"
            )

            val updatedSurveyor = it.userA.surveyors.update(
                surveyId = surveyId,
                surveyorId = updateData?.id!!,
                surveyor = updateData
            )

            assertEquals(surveyor.id, updatedSurveyor.id)
            assertEquals("Updated first name", updatedSurveyor.firstName)
            assertEquals("Updated last name", updatedSurveyor.lastName)
            assertEquals("Updated company", updatedSurveyor.company)
            assertEquals("044987654321", updatedSurveyor.phone)
            assertEquals("updated@example.com", updatedSurveyor.email)
            assertEquals("Updated role", updatedSurveyor.role)


            it.userB.surveyors.assertUpdateFailStatus(expectedStatus = 403, surveyor = updateData)
            it.admin.surveyors.assertUpdateFailStatus(expectedStatus = 404, surveyor.copy(id = UUID.randomUUID()))
        }
    }

    /**
     * Tests surveyor deletion
     */
    @Test
    fun delete() {
        TestBuilder().use {
            val useAGroup = it.userA.userGroups.list(member = true).first()
            val useBGroup = it.userB.userGroups.list(member = true).first()

            val surveyId = it.userA.surveys.create(useAGroup.id!!).id!!
            val anotherSurveyId = it.userB.surveys.create(useBGroup.id!!).id!!

            val emptyList = it.admin.surveyors.list(surveyId = surveyId)
            assertEquals(0, emptyList.size)

            val surveyor = it.userA.surveyors.createWithDefaultValues(surveyId = surveyId)
            val anotherSurveyor = it.userB.surveyors.createWithDefaultValues(surveyId = anotherSurveyId)

            val listWithTwoItems = it.admin.surveyors.list(surveyId = surveyId)

            assertEquals(1, listWithTwoItems.size)

            it.admin.surveyors.assertDeleteFailStatus(expectedStatus = 404, surveyor!!.copy(id = UUID.randomUUID()))
            it.userB.surveyors.assertDeleteFailStatus(expectedStatus = 403, surveyor)

            it.admin.surveyors.delete(surveyor = surveyor)
            it.userB.surveyors.delete(surveyor = anotherSurveyor!!)

            it.admin.surveyors.assertDeleteFailStatus(expectedStatus = 404, surveyor)

            assertEquals(0, it.admin.surveyors.list(surveyId = surveyId).size)
            assertEquals(0, it.admin.surveyors.list(surveyId = anotherSurveyId).size)
        }
    }
}