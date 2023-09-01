package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.GroupJoinRequest
import fi.metatavu.rapurc.api.client.models.JoinRequestStatus
import fi.metatavu.rapurc.api.client.models.SurveyStatus
import fi.metatavu.rapurc.api.client.models.UserGroup
import fi.metatavu.rapurc.api.impl.email.Templates
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
import fi.metatavu.rapurc.api.test.functional.settings.ApiTestSettings
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for Group Join Requests
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class GroupJoinRequestTestIT: AbstractTestIT() {

    @Test
    fun create() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))
            val wireMock = getMailgunMocker()
            // user a asks to join user's b group
            val createdRequest = testBuilder.userA.groupJoinRequests.create(group2.id!!,
                GroupJoinRequest(
                    email = "usera@example.com",
                    groupId = group2.id
                )
            )

            val joinSubjectExpected = Templates.joinRequestEmailSubject("first name last name", "group 2").render()
            val joinBodyExpected = Templates.joinRequestEmail("first name last name", "group 2").render()
            wireMock.verifyTextMessageSent(
                fromEmail = ApiTestSettings.mailgunSenderEmail,
                to = "userb@example.com",
                subject = joinSubjectExpected,
                content = joinBodyExpected
            )
            try {
                Assertions.assertNotNull(createdRequest.id)
                Assertions.assertEquals(group2.id, createdRequest.groupId)
                Assertions.assertEquals("usera@example.com", createdRequest.email)
                Assertions.assertEquals(JoinRequestStatus.pENDING, createdRequest.status)
                Assertions.assertEquals("first name", createdRequest.firstName)
                Assertions.assertEquals("last name", createdRequest.lastName)
                Assertions.assertEquals("user_a", createdRequest.username)
                Assertions.assertNotNull(createdRequest.metadata?.creatorId)
                Assertions.assertNotNull(createdRequest.metadata?.createdAt)

                // cannot use another's email to send join request
                testBuilder.userA.groupJoinRequests.assertCreateFailStatus(group2.id, createdRequest.copy(email = "userb@example.com"), 403)
                testBuilder.userB.groupJoinRequests.assertCreateFailStatus(group2.id, createdRequest.copy(email = "invalidemail"), 404)
                testBuilder.userB.groupJoinRequests.assertCreateFailStatus(group1.id!!, createdRequest.copy(groupId = group2.id), 400)
                testBuilder.userB.groupJoinRequests.assertCreateFailStatus(group2.id, createdRequest.copy(groupId = UUID.randomUUID()), 400)
                testBuilder.userB.groupJoinRequests.assertCreateFailStatus(UUID.randomUUID(), createdRequest, 400)

                val randomId = UUID.randomUUID()
                testBuilder.userB.groupJoinRequests.assertCreateFailStatus(randomId, createdRequest.copy(groupId = randomId), 404)
            } finally {
                testBuilder.userB.groupJoinRequests.delete(group2.id, createdRequest.id!!)
            }
        }
    }

    @Test
    fun find() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))

            // user a asks to join user's b group
            val createdRequest = testBuilder.userA.groupJoinRequests.create(group2.id!!,
                GroupJoinRequest(
                    email = "usera@example.com",
                    groupId = group2.id
                )
            )
            try {
                val foundRequest = testBuilder.userB.groupJoinRequests.find(group2.id, createdRequest.id!!)
                Assertions.assertEquals(createdRequest.groupId, foundRequest.groupId)
                Assertions.assertEquals(createdRequest.email, foundRequest.email)
                Assertions.assertEquals(createdRequest.status, foundRequest.status)
                Assertions.assertNotNull(createdRequest.metadata?.creatorId)
                Assertions.assertNotNull(createdRequest.metadata?.createdAt)

                testBuilder.userA.groupJoinRequests.assertFindFailStatus(group2.id, createdRequest.id, 403)
                testBuilder.userB.groupJoinRequests.assertFindFailStatus(group2.id, UUID.randomUUID(), 404)
                testBuilder.userB.groupJoinRequests.assertFindFailStatus(UUID.randomUUID(), createdRequest.id, 404)
            } finally {
                testBuilder.userB.groupJoinRequests.delete(group2.id, createdRequest.id!!)
            }
        }
    }

    @Test
    fun list() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))

            val joinGroup2Request = testBuilder.userA.groupJoinRequests.create(group2.id!!,
                GroupJoinRequest(
                    email = "usera@example.com",
                    groupId = group2.id
                )
            )

            try {
                val group1Requests = testBuilder.userA.groupJoinRequests.list(group1.id!!, null)
                Assertions.assertEquals(0, group1Requests.size)

                val group2Requests = testBuilder.userB.groupJoinRequests.list(group2.id, null)
                Assertions.assertEquals(1, group2Requests.size)

                val pendingGroup2Requests = testBuilder.userB.groupJoinRequests.list(group2.id, JoinRequestStatus.pENDING)
                Assertions.assertEquals(1, pendingGroup2Requests.size)

                val acceptedGroup2Requests = testBuilder.userB.groupJoinRequests.list(group2.id, JoinRequestStatus.aCCEPTED)
                Assertions.assertEquals(0, acceptedGroup2Requests.size)

                // check that admin role can also see invites
                Assertions.assertEquals(1, testBuilder.admin.groupJoinRequests.list(group2.id, null).size)

                testBuilder.userA.groupJoinRequests.assertListFailStatus(group2.id, 403)
                testBuilder.userA.groupJoinRequests.assertListFailStatus(UUID.randomUUID(), 404)
            } finally {
                testBuilder.userB.groupJoinRequests.delete(group2.id, joinGroup2Request.id!!)
            }

        }
    }

    @Test
    fun update() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))
            testBuilder.userB.surveys.create(group2.id!!, SurveyStatus.dONE)
            testBuilder.userB.surveys.create(group2.id!!, SurveyStatus.dONE)
            testBuilder.userA.surveys.create(group1.id!!, SurveyStatus.dONE)
            val wireMock = getMailgunMocker()

            // A asks to join B's group
            val joinGroup2Request = testBuilder.userA.groupJoinRequests.create(group2.id!!,
                GroupJoinRequest(
                    email = "usera@example.com",
                    groupId = group2.id
                )
            )
            val joinSubjectExpected = Templates.joinRequestEmailSubject("first name last name", "group 2").render()
            val joinBodyExpected = Templates.joinRequestEmail("first name last name", "group 2").render()
            wireMock.verifyTextMessageSent(
                fromEmail = ApiTestSettings.mailgunSenderEmail,
                to = "userb@example.com",
                subject = joinSubjectExpected,
                content = joinBodyExpected
            )
            try {
                // before A could see 1 survey (own)
                val userAGroupsBeforeJoin = testBuilder.userA.userGroups.list(member = true)
                Assertions.assertEquals(1, testBuilder.userA.surveys.listSurveys().size)
                val accepted = testBuilder.userB.groupJoinRequests.update(group2.id, joinGroup2Request.id!!, joinGroup2Request.copy(status = JoinRequestStatus.aCCEPTED))
                Assertions.assertEquals(JoinRequestStatus.aCCEPTED, accepted.status)
                // now A has access to 2 B's surveys and belongs to its group
                Assertions.assertEquals(3, testBuilder.userA.surveys.listSurveys().size)
                Assertions.assertEquals(userAGroupsBeforeJoin.size + 1, testBuilder.userA.userGroups.list(member = true).size)

                val joinSubjectUpdateExpected = Templates.joinRequestAcceptedEmailSubject("group 2").render()
                val joinBodyUpdateExpected = Templates.joinRequestAcceptedEmail("group 2").render()
                wireMock.verifyTextMessageSent(
                    fromEmail = ApiTestSettings.mailgunSenderEmail,
                    to = "usera@example.com",
                    subject = joinSubjectUpdateExpected,
                    content = joinBodyUpdateExpected
                )

                // user a cannot update user b's group join requests
                testBuilder.userA.groupJoinRequests.assertUpdateFailStatus(group2.id, joinGroup2Request.id, joinGroup2Request.copy(status = JoinRequestStatus.aCCEPTED), 403)
                testBuilder.userB.groupJoinRequests.assertUpdateFailStatus(UUID.randomUUID(), joinGroup2Request.id, joinGroup2Request, 404)
                testBuilder.userB.groupJoinRequests.assertUpdateFailStatus(group2.id, UUID.randomUUID(), joinGroup2Request, 404)
                testBuilder.userB.groupJoinRequests.assertUpdateFailStatus(group2.id, joinGroup2Request.id, joinGroup2Request.copy(groupId = UUID.randomUUID()), 400)
            } finally {
                testBuilder.userB.groupJoinRequests.delete(group2.id, joinGroup2Request.id!!)
            }
        }
    }

    @Test
    fun delete() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))

            val joinGroup2Request = testBuilder.userA.groupJoinRequests.create(group2.id!!,
                GroupJoinRequest(
                    email = "usera@example.com",
                    groupId = group2.id
                )
            )

            testBuilder.userA.groupJoinRequests.assertDeleteFailStatus(group2.id, joinGroup2Request.id!!, 403)

            testBuilder.userB.groupJoinRequests.delete(group2.id, joinGroup2Request.id)

            val afterDeletion = testBuilder.userB.groupJoinRequests.list(group2.id, null)
            Assertions.assertEquals(0, afterDeletion.size)
        }
    }
}