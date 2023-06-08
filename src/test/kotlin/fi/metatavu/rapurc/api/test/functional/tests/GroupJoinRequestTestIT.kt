package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.GroupJoinRequest
import fi.metatavu.rapurc.api.client.models.JoinRequestStatus
import fi.metatavu.rapurc.api.client.models.UserGroup
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
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
class GroupJoinRequestTestIT {

    @Test
    fun create() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))

            // user a asks to join user's b group
            val createdRequest = testBuilder.userA.groupJounRequests.create(group2.id!!,
                GroupJoinRequest(
                    email = "usera@example.com",
                    groupId = group2.id
                )
            )

            try {
                Assertions.assertNotNull(createdRequest.id)
                Assertions.assertEquals(group2.id, createdRequest.groupId)
                Assertions.assertEquals("usera@example.com", createdRequest.email)
                Assertions.assertEquals(JoinRequestStatus.pENDING, createdRequest.status)
                Assertions.assertNotNull(createdRequest.metadata?.creatorId)
                Assertions.assertNotNull(createdRequest.metadata?.createdAt)

                // cannot use another's email to send join request
                testBuilder.userA.groupJounRequests.assertCreateFailStatus(group2.id, createdRequest.copy(email = "userb@example.com"), 403)
                testBuilder.userB.groupJounRequests.assertCreateFailStatus(group2.id, createdRequest.copy(email = "invalidemail"), 404)
                testBuilder.userB.groupJounRequests.assertCreateFailStatus(group1.id!!, createdRequest.copy(groupId = group2.id), 400)
                testBuilder.userB.groupJounRequests.assertCreateFailStatus(group2.id, createdRequest.copy(groupId = UUID.randomUUID()), 400)
                testBuilder.userB.groupJounRequests.assertCreateFailStatus(UUID.randomUUID(), createdRequest, 400)

                val randomId = UUID.randomUUID()
                testBuilder.userB.groupJounRequests.assertCreateFailStatus(randomId, createdRequest.copy(groupId = randomId), 404)
            } finally {
                testBuilder.userB.groupJounRequests.delete(group2.id, createdRequest.id!!)
            }
        }
    }

    @Test
    fun find() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))

            // user a asks to join user's b group
            val createdRequest = testBuilder.userA.groupJounRequests.create(group2.id!!,
                GroupJoinRequest(
                    email = "usera@example.com",
                    groupId = group2.id
                )
            )
            try {
                val foundRequest = testBuilder.userB.groupJounRequests.find(group2.id, createdRequest.id!!)
                Assertions.assertEquals(createdRequest.groupId, foundRequest.groupId)
                Assertions.assertEquals(createdRequest.email, foundRequest.email)
                Assertions.assertEquals(createdRequest.status, foundRequest.status)
                Assertions.assertNotNull(createdRequest.metadata?.creatorId)
                Assertions.assertNotNull(createdRequest.metadata?.createdAt)

                testBuilder.userA.groupJounRequests.assertFindFailStatus(group2.id, createdRequest.id, 403)
                testBuilder.userB.groupJounRequests.assertFindFailStatus(group2.id, UUID.randomUUID(), 404)
                testBuilder.userB.groupJounRequests.assertFindFailStatus(UUID.randomUUID(), createdRequest.id, 404)
            } finally {
                testBuilder.userB.groupJounRequests.delete(group2.id, createdRequest.id!!)
            }
        }
    }

    @Test
    fun list() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))

            val joinGroup2Request = testBuilder.userA.groupJounRequests.create(group2.id!!,
                GroupJoinRequest(
                    email = "usera@example.com",
                    groupId = group2.id
                )
            )

            try {
                val group1Requests = testBuilder.userA.groupJounRequests.list(group1.id!!, null)
                Assertions.assertEquals(0, group1Requests.size)

                val group2Requests = testBuilder.userB.groupJounRequests.list(group2.id, null)
                Assertions.assertEquals(1, group2Requests.size)

                val pendingGroup2Requests = testBuilder.userB.groupJounRequests.list(group2.id, JoinRequestStatus.pENDING)
                Assertions.assertEquals(1, pendingGroup2Requests.size)

                val acceptedGroup2Requests = testBuilder.userB.groupJounRequests.list(group2.id, JoinRequestStatus.aCCEPTED)
                Assertions.assertEquals(0, acceptedGroup2Requests.size)

                testBuilder.userA.groupJounRequests.assertListFailStatus(group2.id, 403)
                testBuilder.userA.groupJounRequests.assertListFailStatus(UUID.randomUUID(), 404)
            } finally {
                testBuilder.userB.groupJounRequests.delete(group2.id, joinGroup2Request.id!!)
            }

        }
    }

    @Test
    fun update() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))

            val joinGroup2Request = testBuilder.userA.groupJounRequests.create(group2.id!!,
                GroupJoinRequest(
                    email = "usera@example.com",
                    groupId = group2.id
                )
            )
            try {
                val accepted = testBuilder.userB.groupJounRequests.update(group2.id, joinGroup2Request.id!!, joinGroup2Request.copy(status = JoinRequestStatus.aCCEPTED))
                Assertions.assertEquals(JoinRequestStatus.aCCEPTED, accepted.status)

                // user a cannot update user b's group join requests
                testBuilder.userA.groupJounRequests.assertUpdateFailStatus(group2.id, joinGroup2Request.id, joinGroup2Request.copy(status = JoinRequestStatus.aCCEPTED), 403)
                testBuilder.userB.groupJounRequests.assertUpdateFailStatus(UUID.randomUUID(), joinGroup2Request.id, joinGroup2Request, 404)
                testBuilder.userB.groupJounRequests.assertUpdateFailStatus(group2.id, UUID.randomUUID(), joinGroup2Request, 404)
                testBuilder.userB.groupJounRequests.assertUpdateFailStatus(group2.id, joinGroup2Request.id, joinGroup2Request.copy(groupId = UUID.randomUUID()), 400)
            } finally {
                testBuilder.userB.groupJounRequests.delete(group2.id, joinGroup2Request.id!!)
            }
        }
    }

    @Test
    fun delete() {
        TestBuilder().use { testBuilder ->
            val group1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            val group2 = testBuilder.userB.userGroups.create(UserGroup(name = "group 2"))

            val joinGroup2Request = testBuilder.userA.groupJounRequests.create(group2.id!!,
                GroupJoinRequest(
                    email = "usera@example.com",
                    groupId = group2.id
                )
            )

            testBuilder.userA.groupJounRequests.assertDeleteFailStatus(group2.id, joinGroup2Request.id!!, 403)

            testBuilder.userB.groupJounRequests.delete(group2.id, joinGroup2Request.id)

            val afterDeletion = testBuilder.userB.groupJounRequests.list(group2.id, null)
            Assertions.assertEquals(0, afterDeletion.size)
        }
    }
}