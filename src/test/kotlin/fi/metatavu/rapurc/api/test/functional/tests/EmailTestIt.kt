package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.*
import fi.metatavu.rapurc.api.impl.email.Templates
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MailgunResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
import fi.metatavu.rapurc.api.test.functional.settings.ApiTestSettings
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for email notifications
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class),
    QuarkusTestResource(MailgunResource::class)
)
class EmailTestIt : AbstractTestIT() {

    /**
     * Checks that user can send email to another registered use that belongs to the same group
     */
    @Test
    fun testBuildingDemolitionEmailSameGroup() {
        TestBuilder().use {
            val wireMock = getMailgunMocker()

            val group1 = it.userA.userGroups.create(UserGroup(name = "group 1"))
            val (createdSurveyA, building) = createSurveyWithBuilding(it, group1.id!!)

            // Inform registered user about the survey
            val emailTemplate = EmailTemplate(
                emailType = EmailType.bUILDINGDEMOLITIONCONTACTUPDATE,
                emailAddress = "usera@example.com",
                emailData = BuildingDemolitionEmailTemplate(
                    buildingId = building!!.id!!,
                )
            )

            it.admin.emails.sendSurveyEmail(
                surveyId = createdSurveyA.id!!,
                emailTemplate = emailTemplate
            )

            //check that user A received 1 email
            val informEmailSubject = Templates.buildingDemolitionContactUpdateSubject(building.propertyName!!).render()
            val informEmailObject = Templates.buildingDemolitionContactUpdateBody("https://purkukartoitus.fi/surveys/${createdSurveyA.id}/reusables").render()
            wireMock.verifyTextMessageSent(
                fromEmail = ApiTestSettings.mailgunSenderEmail,
                to = "usera@example.com",
                subject = informEmailSubject,
                content = informEmailObject
            )

            // Errors checking
            it.admin.emails.assertSendSurveyEmailFailStatus(
                404,
                surveyId = UUID.randomUUID(),
                emailTemplate = emailTemplate
            )
            it.userB.emails.assertSendSurveyEmailFailStatus(
                403,
                surveyId = createdSurveyA.id,
                emailTemplate = emailTemplate
            )
            it.admin.emails.assertSendSurveyEmailFailStatus(
                400,
                surveyId = createdSurveyA.id,
                emailTemplate = emailTemplate.copy(emailData = BuildingDemolitionEmailTemplate(buildingId = UUID.randomUUID()))
            )
        }
    }

    /**
     * Checks that user can send email to another users: registered but from another group or unregistered
     * and that those users receive the group invitations
     */
    @Test
    fun testBuildingDemolitionEmailDifferentGrouporUnregistered() {
        TestBuilder().use {
            val wireMock = getMailgunMocker()
            val group1 = it.userA.userGroups.create(UserGroup(name = "group 1"))
            val (survey, building) = createSurveyWithBuilding(it, group1.id!!)

            val emailTemplate = EmailTemplate(
                emailType = EmailType.bUILDINGDEMOLITIONCONTACTUPDATE,
                emailAddress = "userc@example.com",
                emailData = BuildingDemolitionEmailTemplate(buildingId = building!!.id!!)
            )

            val informEmailSubject = Templates.buildingDemolitionContactUpdateSubject(building.propertyName!!).render()
            val informEmailObject = Templates.buildingDemolitionContactUpdateBody("https://purkukartoitus.fi/surveys/${survey.id}/reusables").render()
            val subjectExpected = Templates.userInviteEmailSubject("group 1").render()
            val bodyExpected = Templates.userInviteEmail("group 1").render()

            // Inform user C from another group and verify that it received both the notification and invite
            it.userA.emails.sendSurveyEmail(
                surveyId = survey.id!!,
                emailTemplate = emailTemplate.copy(emailAddress = "userc@example.com")
            )
            wireMock.verifyTextMessageSent(
                fromEmail = ApiTestSettings.mailgunSenderEmail,
                to = "userc@example.com",
                subject = informEmailSubject,
                content = informEmailObject
            )
            wireMock.verifyTextMessageSent(
                fromEmail = ApiTestSettings.mailgunSenderEmail,
                to = "userc@example.com",
                subject = subjectExpected,
                content = bodyExpected
            )

            // Inform unregistered user and verify that it received both the notification and invite
            it.userA.emails.sendSurveyEmail(
                surveyId = survey.id,
                emailTemplate = emailTemplate.copy(emailAddress = "newEmail@example.com")
            )
            wireMock.verifyTextMessageSent(
                fromEmail = ApiTestSettings.mailgunSenderEmail,
                to = "newEmail@example.com",
                subject = informEmailSubject,
                content = informEmailObject
            )
            wireMock.verifyTextMessageSent(
                fromEmail = ApiTestSettings.mailgunSenderEmail,
                to = "newEmail@example.com",
                subject = subjectExpected,
                content = bodyExpected
            )
        }
    }

    /**
     * creates survey for group and a building
     *
     * @param tb test builder
     * @param groupId group id
     * @return created survey and building
     */
    private fun createSurveyWithBuilding(
        tb: TestBuilder,
        groupId: UUID,
    ): Pair<Survey, Building?> {
        val createdSurveyA = tb.userA.surveys.create(groupId, SurveyStatus.dONE)
        val buildingType = tb.admin.buildingTypes.create()
        val building = tb.userA.buildings.create(
            surveyId = createdSurveyA.id!!,
            building = Building(
                surveyId = createdSurveyA.id,
                buildingTypeId = buildingType!!.id,
                propertyName = "Test building",
                metadata = Metadata()
            )
        )

        return Pair(createdSurveyA, building)
    }
}
