package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.Metadata
import fi.metatavu.rapurc.api.client.models.Survey
import fi.metatavu.rapurc.api.client.models.SurveyStatus
import fi.metatavu.rapurc.api.client.models.SurveyType
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

/**
 * Functional test for surveys
 *
 * @author Antti Leppä
 * @author Jari Nykänen
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class SurveyTestIT {

    /**
     * Tests survey creation
     */
    @Test
    fun create() {
        TestBuilder().use {
            val createdSurvey = it.userA.surveys.create()
            assertNotNull(createdSurvey)
            assertEquals("first name last name", createdSurvey.creatorName)

            it.admin.surveys.assertCreateFailStatus(403, SurveyStatus.dRAFT)
        }
    }

    /**
     * Tests survey listing
     */
    @Test
    fun list() {
        TestBuilder().use {
            val emptyList = it.admin.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = null,
                type = null,
                dateUnknown = null,
                startDate = null,
                endDate = null
            )

            assertEquals(0, emptyList.size)

            it.userA.surveys.create(
                Survey(
                    status = SurveyStatus.dRAFT,
                    dateUnknown = false,
                    startDate = LocalDate.of(2021, 1, 10).toString(),
                    endDate = LocalDate.of(2021, 5, 1).toString(),
                    type = SurveyType.dEMOLITION,
                    metadata = Metadata()
                )
            )
            it.userA.surveys.create(
                Survey(
                    status = SurveyStatus.dONE,
                    type = SurveyType.rENOVATION,
                    dateUnknown = false,
                    startDate = LocalDate.of(2020, 1, 1).toString(),
                    endDate = LocalDate.of(2020, 12, 1).toString(),
                    metadata = Metadata()
                )
            )
            it.userB.surveys.create(
                Survey(
                    status = SurveyStatus.dRAFT,
                    type = SurveyType.pARTIALDEMOLITION,
                    metadata = Metadata()
                )
            )

            val listWithThreeItems = it.admin.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = null,
                type = null,
                dateUnknown = null,
                startDate = null,
                endDate = null
            )

            assertEquals(3, listWithThreeItems.size)
            assertEquals(SurveyType.pARTIALDEMOLITION, listWithThreeItems[0].type)

            val listWithDraftStatus = it.admin.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = SurveyStatus.dRAFT,
                type = null,
                dateUnknown = null,
                startDate = null,
                endDate = null
            )

            assertEquals(2, listWithDraftStatus.size)

            val listWithDoneStatus = it.admin.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = SurveyStatus.dONE,
                type = null,
                dateUnknown = null,
                startDate = null,
                endDate = null
            )
            assertEquals(1, listWithDoneStatus.size)

            val listByType = it.admin.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = null,
                type = SurveyType.rENOVATION,
                dateUnknown = null,
                startDate = null,
                endDate = null
            )
            assertEquals(1, listByType.size)
            
            val listByDateUnknown = it.admin.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = null,
                type = null,
                dateUnknown = false,
                startDate = null,
                endDate = null
            )
            assertEquals(2, listByDateUnknown.size)

            val listFilteredByDate = it.admin.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = null,
                type = null,
                dateUnknown = null,
                startDate = LocalDate.of(2021, 1, 1).toString(),
                endDate = LocalDate.of(2021, 5, 1).toString()
            )
            assertEquals(1, listFilteredByDate.size)

            val listByGroupA = it.userA.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = null,
                type = null,
                dateUnknown = null,
                startDate = null,
                endDate = null
            )
            assertEquals(2, listByGroupA.size)

            val listByGroupB = it.userB.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = null,
                type = null,
                dateUnknown = null,
                startDate = null,
                endDate = null
            )
            assertEquals(1, listByGroupB.size)
        }
    }

    /**
     * Tests finding survey
     */
    @Test
    fun find() {
        TestBuilder().use {
            val survey = it.userA.surveys.create()
            val foundSurvey = it.userA.surveys.findSurvey(surveyId = survey.id!!)

            assertEquals(survey.id, foundSurvey.id)
            assertEquals(survey.status, foundSurvey.status)

            it.admin.surveys.assertFindFailStatus(expectedStatus = 404, surveyId = UUID.randomUUID())
            it.userB.surveys.assertFindFailStatus(expectedStatus = 403, surveyId = survey.id)
        }
    }

    /**
     * Tests survey updating
     */
    @Test
    fun update() {
        TestBuilder().use {
            val survey = it.userA.surveys.create()
            val updateData = survey.copy(status = SurveyStatus.dONE, additionalInformation = "test")

            var updatedSurvey = it.userA.surveys.updateSurvey(body = updateData)
            assertEquals(survey.id, updatedSurvey.id)
            assertNotEquals(survey.status, updatedSurvey.status)
            assertEquals(SurveyStatus.dONE, updatedSurvey.status)
            assertEquals(updateData.additionalInformation, updatedSurvey.additionalInformation)
            assertNotNull(updatedSurvey.markedAsDone)
            val markedAsDone1 = OffsetDateTime.parse(updatedSurvey.markedAsDone)

            updatedSurvey = it.userA.surveys.updateSurvey(body = survey.copy(status = SurveyStatus.dRAFT))
            assertNull(updatedSurvey.markedAsDone)

            updatedSurvey = it.userA.surveys.updateSurvey(body = survey.copy(status = SurveyStatus.dONE))
            assertNotNull(updatedSurvey.markedAsDone)
            val markedAsDone2 = OffsetDateTime.parse(updatedSurvey.markedAsDone)

            assertTrue(markedAsDone2.isAfter(markedAsDone1))

            it.userB.surveys.assertUpdateFailStatus(expectedStatus = 403, updateData)
            it.admin.surveys.assertUpdateFailStatus(expectedStatus = 404, survey.copy(id = UUID.randomUUID()))
        }
    }

    /**
     * Tests survey deletion
     */
    @Test
    fun delete() {
        TestBuilder().use {
            val emptyList = it.admin.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = null,
                type = null,
                dateUnknown = null,
                startDate = null,
                endDate = null
            )

            assertEquals(0, emptyList.size)

            val survey = it.userA.surveys.create()
            val anotherSurvey = it.userB.surveys.create()

            val listWithTwoItems = it.admin.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = null,
                type = null,
                dateUnknown = null,
                startDate = null,
                endDate = null
            )

            assertEquals(2, listWithTwoItems.size)

            it.admin.surveys.assertDeleteFailStatus(expectedStatus = 404, survey.copy(id = UUID.randomUUID()))
            it.userB.surveys.assertDeleteFailStatus(expectedStatus = 403, survey)

            it.admin.surveys.delete(survey = survey)
            it.userB.surveys.delete(survey = anotherSurvey)

            it.admin.surveys.assertDeleteFailStatus(expectedStatus = 404, survey)

            val finalEmptyList = it.admin.surveys.listSurveys(
                firstResult = null,
                maxResult = null,
                address = null,
                status = null,
                type = null,
                dateUnknown = null,
                startDate = null,
                endDate = null
            )

            assertEquals(0, finalEmptyList.size)
        }
    }
}