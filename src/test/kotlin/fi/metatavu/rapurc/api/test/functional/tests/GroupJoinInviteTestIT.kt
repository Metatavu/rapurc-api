package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.GroupJoinInvite
import fi.metatavu.rapurc.api.client.models.JoinRequestStatus
import fi.metatavu.rapurc.api.client.models.SurveyStatus
import fi.metatavu.rapurc.api.client.models.UserGroup
import fi.metatavu.rapurc.api.impl.email.Templates
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MailgunMocker
import fi.metatavu.rapurc.api.test.functional.resources.MailgunResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for Group Join Invites
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class),
    QuarkusTestResource(MailgunResource::class)
)
class GroupJoinInviteTestIT : AbstractTestIT() {
    private val userAEmail = "usera@example.com"
    private val userBEmail = "userb@example.com"

    /**
     * Test creating a group, inviting a user and checking that it
     * received an email and can see the invite
     */
    @Test
    fun create() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))

            val mailgunMocker: MailgunMocker = getMailgunMocker()
            // user a offers b to join its group
            val createdInvite = testBuilder.userA.groupJoinInvites.create(group1.id!!,
                GroupJoinInvite(
                    email = "userb@example.com",
                    groupId = group1.id,
                    status = JoinRequestStatus.pENDING
                )
            )

            assertNotNull(createdInvite.id)
            assertEquals(group1.id, createdInvite.groupId)
            assertEquals("userb@example.com", createdInvite.email)
            assertEquals(JoinRequestStatus.pENDING, createdInvite.status)
            assertNotNull(createdInvite.metadata?.creatorId)
            assertNotNull(createdInvite.metadata?.createdAt)

            val subjectExpected = Templates.userInviteEmailSubject("group 1").render()
            val bodyExpected = Templates.userInviteEmail("group 1").render()
            mailgunMocker.verifyTextMessageSent(
                fromEmail = userAEmail,
                to = userBEmail,
                subject = subjectExpected,
                content = bodyExpected
            )

            testBuilder.userA.groupJoinInvites.create(group1.id, createdInvite.copy(email = "notregisteredemail@exmaple.com"))
            testBuilder.userA.groupJoinInvites.assertCreateFailStatus(group1.id, createdInvite.copy(groupId = group2.id!!), 400)
            testBuilder.userA.groupJoinInvites.assertCreateFailStatus(group2.id, createdInvite.copy(groupId = UUID.randomUUID()), 403)
            testBuilder.userA.groupJoinInvites.assertCreateFailStatus(UUID.randomUUID(), createdInvite, 404)
            testBuilder.userA.groupJoinInvites.assertCreateFailStatus(group2.id, createdInvite.copy(groupId = group2.id), 403)

            val randomId = UUID.randomUUID()
            testBuilder.userA.groupJoinInvites.assertCreateFailStatus(randomId, createdInvite.copy(groupId = randomId), 404)

            testBuilder.userA.groupJoinInvites.resendEmail(group1.id, createdInvite.id!!)
            mailgunMocker.verifyTextMessageSent(
                fromEmail = userAEmail,
                to = userBEmail,
                subject = subjectExpected,
                content = bodyExpected
            )

        }
    }

    /**
     * User A creates invite for user B to group 1 and checks that user b can view it.
     */
    @Test
    fun find() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))
            val group3 = testBuilder.admin.userGroups.create(UserGroup(name = "group 3"))

            // user a invites user b to join its group
            val createdInvite = testBuilder.userA.groupJoinInvites.create(group1.id!!,
                GroupJoinInvite(
                    email = "userb@example.com",
                    groupId = group1.id,
                    status = JoinRequestStatus.pENDING
                )
            )

            val foundByInvitedUser = testBuilder.userB.groupJoinInvites.find(group1.id, createdInvite.id!!)
            val foundByGroupAdmin = testBuilder.userA.groupJoinInvites.find(group1.id, createdInvite.id)

            assertEquals(createdInvite.id, foundByInvitedUser.id)
            assertEquals(createdInvite.groupId, foundByGroupAdmin.groupId)
            assertEquals(createdInvite.email, foundByGroupAdmin.email)
            assertEquals(createdInvite.status, foundByGroupAdmin.status)
            assertEquals(createdInvite.metadata?.creatorId, foundByGroupAdmin.metadata?.creatorId)
            assertEquals(createdInvite.metadata?.createdAt, foundByGroupAdmin.metadata?.createdAt)

            // No user of another group can see the invite
            testBuilder.admin.groupJoinInvites.assertFindFailStatus(group1.id, createdInvite.id, 403)
            testBuilder.userB.groupJoinInvites.assertFindFailStatus(group2.id!!, createdInvite.id, 404)
        }
    }

    /**
     * User A creates invites for 2 users and checks that it can see all invites while invited users
     * can see only own invites
     */
    @Test
    fun list() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))
            val group3 = testBuilder.userC.userGroups.create(UserGroup(name = "group 3"))

            // user a invites user b to join its group
            testBuilder.userA.groupJoinInvites.create(group1.id!!,
                GroupJoinInvite(
                    email = "userb@example.com",
                    groupId = group1.id,
                    status = JoinRequestStatus.pENDING
                )
            )

            // user a invites user c to join its group
            testBuilder.userA.groupJoinInvites.create(group1.id,
                GroupJoinInvite(
                    email = "userc@example.com",
                    groupId = group1.id,
                    status = JoinRequestStatus.pENDING
                )
            )

            assertEquals(2, testBuilder.userA.groupJoinInvites.list(group1.id).size)
            assertEquals(1, testBuilder.userB.groupJoinInvites.list(group1.id).size)
            assertEquals(1, testBuilder.userC.groupJoinInvites.list(group1.id).size)

            //cannot list another group's invites
            assertEquals(0, testBuilder.admin.groupJoinInvites.list(group1.id).size)
        }
    }

    /**
     * Checks that when user A invites a user to the group, user can accept it, successfully join the group
     *  and send/receive correct emails
     */
    @Test
    fun update() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))
            val group3 = testBuilder.userC.userGroups.create(UserGroup(name = "group 3"))

            val mailgunMocker = getMailgunMocker()

            // user a invites user b to join its group
            val createdInvite = testBuilder.userA.groupJoinInvites.create(group1.id!!,
                GroupJoinInvite(
                    email = "userb@example.com",
                    groupId = group1.id,
                    status = JoinRequestStatus.pENDING
                )
            )

            val goroup1Survey = testBuilder.userA.surveys.create(SurveyStatus.dONE)
            testBuilder.userB.surveys.assertFindFailStatus(403, goroup1Survey.id!!)

            // user b accepts the invite
            testBuilder.userB.groupJoinInvites.update(group1.id, createdInvite.id!!, createdInvite.copy(status = JoinRequestStatus.aCCEPTED))

            //check that user has access to group 1 resouces now but other users still do not
            assertNotNull(testBuilder.userB.surveys.findSurvey(goroup1Survey.id))
            testBuilder.userC.surveys.assertFindFailStatus(403, goroup1Survey.id)

            mailgunMocker.verifyTextMessageSent(
                fromEmail = userAEmail,
                to = userBEmail,
                subject = Templates.userInviteEmailSubject("group 1").render(),
                content = Templates.userInviteEmail("group 1").render()
            )
            mailgunMocker.verifyTextMessageSent(
                fromEmail = userBEmail,
                to = userAEmail,
                subject = Templates.userInviteAcceptedEmailSubject("user_b", "group 1").render(),
                content = Templates.userInviteAcceptedEmail("user_b", "group 1").render()
            )
        }
    }

    /**
     * Checks that group admin can create and remove invites
     */
    @Test
    fun delete() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))

            val createdInvite = testBuilder.userA.groupJoinInvites.create(group1.id!!,
                GroupJoinInvite(
                    email = "userb@example.com",
                    groupId = group1.id,
                    status = JoinRequestStatus.pENDING
                )
            )

            assertEquals(1, testBuilder.userB.groupJoinInvites.list(group1.id).size)
            testBuilder.userA.groupJoinInvites.delete(group1.id, createdInvite.id!!)

            assertEquals(0, testBuilder.userB.groupJoinInvites.list(group1.id).size)
        }
    }
}