package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.ContactPerson
import fi.metatavu.rapurc.api.client.models.Metadata
import fi.metatavu.rapurc.api.client.models.OwnerInformation
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
 * Tests for Owners API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class OwnerInformationTestIT {

    val ownerInformation = OwnerInformation(
        surveyId = UUID.randomUUID(),
        ownerName = "Timo",
        businessId = "id",
        contactPerson = ContactPerson(
            firstName = "Timo",
            email = "example@example.com",
            profession = "construction worker"
        ),
        metadata = Metadata()
    )

    /**
     * Tests owner information creation
     */
    @Test
    fun create() {
        TestBuilder().use {
            val createdSurveyA1 = it.userA.surveys.create(it.userA)
            val createdSurveyA2 = it.userA.surveys.create(it.userA)

            it.userB.owners.assertCreateFailStatus(403, createdSurveyA1.id, ownerInformation)
            it.userA.owners.assertCreateFailStatus(
                403, createdSurveyA1.id, ownerInformation.copy(
                    surveyId = createdSurveyA2.id!!
                )
            )

            val createdOwnerInfo = it.userA.owners.create(
                createdSurveyA1.id!!, ownerInformation.copy(
                    surveyId = createdSurveyA1.id
                )
            )

            assertNotNull(createdOwnerInfo)
            assertEquals(ownerInformation.businessId, createdOwnerInfo!!.businessId)
            assertEquals(ownerInformation.ownerName, createdOwnerInfo.ownerName)
            assertEquals(ownerInformation.contactPerson!!.firstName, createdOwnerInfo.contactPerson!!.firstName)
            assertEquals(ownerInformation.contactPerson!!.email, createdOwnerInfo.contactPerson.email)
            assertEquals(ownerInformation.contactPerson!!.profession, createdOwnerInfo.contactPerson.profession)
        }
    }

    /**
     * Tests listing owner information
     */
    @Test
    fun list() {
        TestBuilder().use {
            val createdSurveyA1 = it.userA.surveys.create(it.userA)
            val createdSurveyA2 = it.userA.surveys.create(it.userA)
            val createdSurveyB1 = it.userB.surveys.create(it.userB)
            it.userA.owners.create(
                createdSurveyA1.id!!, ownerInformation.copy(
                    surveyId = createdSurveyA1.id
                )
            )
            it.userA.owners.create(
                createdSurveyA2.id!!, ownerInformation.copy(
                    surveyId = createdSurveyA2.id
                )
            )
            it.userB.owners.create(
                createdSurveyB1.id!!, ownerInformation.copy(
                    surveyId = createdSurveyB1.id
                )
            )

            it.admin.owners.assertCount(1, createdSurveyA1.id)
            it.admin.owners.assertCount(1, createdSurveyA2.id)
            it.admin.owners.assertCount(1, createdSurveyB1.id)
            it.userA.owners.assertCount(1, createdSurveyA1.id)
            it.userA.owners.assertCount(1, createdSurveyA2.id)

            it.userA.owners.assertListFailStatus(404, UUID.randomUUID())
            it.userB.owners.assertListFailStatus(403, createdSurveyA2.id)
        }
    }

    /**
     * Tests finding owner information
     */
    @Test
    fun find() {
        TestBuilder().use {
            val createdSurveyA1 = it.userA.surveys.create(it.userA)
            val createdSurveyB1 = it.userB.surveys.create(it.userB)
            val ownerInformation1 = it.userA.owners.create(
                createdSurveyA1.id!!, ownerInformation.copy(
                    surveyId = createdSurveyA1.id
                )
            )
            val ownerInformation2 = it.userB.owners.create(
                createdSurveyB1.id!!, ownerInformation.copy(
                    surveyId = createdSurveyB1.id
                )
            )

            val foundOwnerInfo = it.userA.owners.findOwner(createdSurveyA1.id, ownerInformation1!!.id!!)
            assertNotNull(foundOwnerInfo)
            assertEquals(ownerInformation.businessId, foundOwnerInfo.businessId)
            assertEquals(ownerInformation.ownerName, foundOwnerInfo.ownerName)
            assertEquals(ownerInformation.contactPerson!!.firstName, foundOwnerInfo.contactPerson!!.firstName)
            assertEquals(ownerInformation.contactPerson!!.email, foundOwnerInfo.contactPerson.email)
            assertEquals(ownerInformation.contactPerson!!.profession, foundOwnerInfo.contactPerson.profession)

            it.userA.owners.assertFindFailStatus(403, createdSurveyA1.id, ownerInformation2!!.id!!)
            it.userA.owners.assertFindFailStatus(404, createdSurveyA1.id, UUID.randomUUID())
            it.userB.owners.assertFindFailStatus(403, createdSurveyA1.id, ownerInformation1.id!!)
        }
    }

    /**
     * Tests owner information updates
     */
    @Test
    fun update() {
        TestBuilder().use {
            val createdSurveyA1 = it.userA.surveys.create(it.userA)
            val createdSurveyB1 = it.userB.surveys.create(it.userB)
            val ownerInformation1 = it.userA.owners.create(
                createdSurveyA1.id!!, ownerInformation.copy(
                    surveyId = createdSurveyA1.id
                )
            )
            val ownerInformation2 = it.userB.owners.create(
                createdSurveyB1.id!!, ownerInformation.copy(
                    surveyId = createdSurveyB1.id
                )
            )

            val newOwnerInfo = ownerInformation1!!.copy(
                ownerName = "Mikko"
            )
            val updatedOwnerInfo = it.userA.owners.update(createdSurveyA1.id, ownerInformation1.id!!, newOwnerInfo)

            assertNotNull(updatedOwnerInfo)
            assertEquals(newOwnerInfo.businessId, updatedOwnerInfo.businessId)
            assertEquals(newOwnerInfo.ownerName, updatedOwnerInfo.ownerName)
            assertEquals(newOwnerInfo.contactPerson!!.firstName, updatedOwnerInfo.contactPerson!!.firstName)
            assertEquals(newOwnerInfo.contactPerson.email, updatedOwnerInfo.contactPerson.email)
            assertEquals(newOwnerInfo.contactPerson.profession, updatedOwnerInfo.contactPerson.profession)

            it.userA.owners.assertUpdateFailStatus(
                403, createdSurveyA1.id, ownerInformation2!!.id!!, ownerInformation1.copy(
                    surveyId = createdSurveyB1.id
                )
            )
            it.userA.owners.assertUpdateFailStatus(404, createdSurveyB1.id, UUID.randomUUID(), ownerInformation1)
        }
    }

    /**
     * Tests owner information deletion
     */
    @Test
    fun delete() {
        TestBuilder().use {
            val createdSurveyA1 = it.userA.surveys.create(it.userA)
            val createdSurveyB1 = it.userB.surveys.create(it.userB)
            val ownerInformation1 = it.userA.owners.create(
                createdSurveyA1.id!!, ownerInformation.copy(
                    surveyId = createdSurveyA1.id
                )
            )
            val ownerInformation2 = it.userB.owners.create(
                createdSurveyB1.id!!, ownerInformation.copy(
                    surveyId = createdSurveyB1.id
                )
            )
            val ownerInformation3 = it.userB.owners.create(
                createdSurveyB1.id, ownerInformation.copy(
                    surveyId = createdSurveyB1.id
                )
            )

            it.userA.owners.assertDeleteFailStatus(403, ownerInformation2!!)
            it.userA.owners.delete(ownerInformation1!!)
            it.userB.owners.delete(ownerInformation2)
            it.admin.owners.assertCount(0, createdSurveyA1.id)
            it.admin.owners.assertCount(1, createdSurveyB1.id)

            it.admin.surveys.delete(createdSurveyB1)
            it.admin.owners.markAsDeleted(ownerInformation3!!)
            it.admin.owners.assertListFailStatus(404, createdSurveyB1.id)
        }
    }

}