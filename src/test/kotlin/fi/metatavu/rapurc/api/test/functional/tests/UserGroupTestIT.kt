package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.GroupJoinInvite
import fi.metatavu.rapurc.api.client.models.JoinRequestStatus
import fi.metatavu.rapurc.api.client.models.UserGroup
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
 * Tests for User Groups API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class UserGroupTestIT {

    /**
     * Tests creating user group
     */
    @Test
    fun create() {
        TestBuilder().use { testBuilder ->
            val created = testBuilder.userA.userGroups.create()
            assertNotNull(created.id)
            assertEquals("Test group", created.name)

            testBuilder.userA.userGroups.assertCreateFailStatus(400)
        }
    }

    /**
     * Tests listing user groups
     */
    @Test
    fun list() {
        TestBuilder().use { testBuilder ->
            testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            testBuilder.userA.userGroups.create(UserGroup(name = "group 2"))
            testBuilder.userB.userGroups.create(UserGroup(name = "group 3"))

            val list = testBuilder.userA.userGroups.list()
            assertEquals(5, list.size)  // 3 created by test, 2 already existing by keycloak
            val listByAdmin = testBuilder.userA.userGroups.list(adminEmail = "usera@example.com")
            assertEquals(2, listByAdmin.size)

            testBuilder.userB.userGroups.assertListFailStatus(403, adminEmail = "usera@example.com")
            testBuilder.userB.userGroups.assertListFailStatus(404, adminEmail = "randomemail")
        }
    }

    /**
     * Tests finding user group
     */
    @Test
    fun find() {
        TestBuilder().use { testBuilder ->
            val created = testBuilder.userA.userGroups.create()
            val found = testBuilder.userA.userGroups.find(created.id!!)
            assertEquals(created.id, found.id)
            assertEquals(created.name, found.name)

            testBuilder.userA.userGroups.assertFindFailStatus(404, UUID.randomUUID())
        }
    }

    /**
     * Tests updating user group
     */
    @Test
    fun update() {
        TestBuilder().use { testBuilder ->
            val created1 = testBuilder.userA.userGroups.create(UserGroup(name = "group 1"))
            testBuilder.userA.userGroups.create(UserGroup(name = "group 2"))

            val updateData = created1.copy(name = "Updated name")
            val updated = testBuilder.userA.userGroups.update(created1.id!!, updateData)

            assertEquals(created1.id, updated.id)
            assertEquals("Updated name", updated.name)

            // cannot update name to existing name
            testBuilder.userA.userGroups.assertUpdateFailStatus(400, created1.id, updateData.copy(name = "group 2"))
            testBuilder.userA.userGroups.assertUpdateFailStatus(404, UUID.randomUUID(), updateData)
            testBuilder.userB.userGroups.assertUpdateFailStatus(403, created1.id, updateData)
        }
    }

    /**
     * Tests deleting user group
     */
    @Test
    fun delete() {
        TestBuilder().use { testBuilder ->
            val created = testBuilder.userA.userGroups.create()

            testBuilder.userA.userGroups.assertDeleteFailStatus(404, UUID.randomUUID())
            testBuilder.userB.userGroups.assertDeleteFailStatus(403, created.id!!)

            testBuilder.userA.userGroups.delete(created.id)
            assertEquals(0, testBuilder.userA.userGroups.list("usera@example.com").size)
        }
    }

    /**
     * Tests listing group members and removing them
     */
    @Test
    fun groupUsersListing() {
        TestBuilder().use { tb ->
            val group1 = tb.userA.userGroups.create()
            // Check that group admin is the only member
            val groupMembers = tb.userA.userGroups.listGroupMembers(group1.id!!)
            assertEquals(1, groupMembers.size)
            assertEquals("first name", groupMembers[0].firstName)
            assertEquals("last name", groupMembers[0].lastName)
            assertEquals("usera@example.com", groupMembers[0].email)
            assertEquals("4d5c5296-587a-4387-91c5-b9f19d7d10d6", groupMembers[0].id.toString())
            assertEquals("user_a", groupMembers[0].username)

            // not admins cannot list the members
            tb.userB.userGroups.assertListGroupMembersFailStatus(403, group1.id)
            tb.userB.userGroups.assertListGroupMembersFailStatus(404, UUID.randomUUID())

            // Invite user to the group and accept the invitation
            val createdInvite = tb.userA.groupJoinInvites.create(group1.id,
                GroupJoinInvite(
                    email = "userb@example.com",
                    groupId = group1.id,
                    status = JoinRequestStatus.pENDING
                )
            )
            tb.userB.groupJoinInvites.update(group1.id, createdInvite.id!!, createdInvite.copy(status = JoinRequestStatus.aCCEPTED))

            // Verify that new user is in the group
            val groupMembersAfterInvite = tb.userA.userGroups.listGroupMembers(group1.id)
            assertEquals(2, groupMembersAfterInvite.size)
            val userB = groupMembersAfterInvite.find { it.id.toString() == "77fc8188-ba66-4843-8fbf-72945e68805a" }
            val userA = groupMembersAfterInvite.find { it.id.toString() == "4d5c5296-587a-4387-91c5-b9f19d7d10d6" }
            assertNotNull(userA)
            assertNotNull(userB)

            // Cannot remove unknown user
            tb.userA.userGroups.assertDeleteGroupUserFailStatus(404, group1.id, UUID.randomUUID())
            tb.userA.userGroups.assertDeleteGroupUserFailStatus(404, UUID.randomUUID(), userB.id!!)
            tb.userB.userGroups.assertDeleteGroupUserFailStatus(403, group1.id, userB.id)

            // Manually remove user B from the group and verify that it is removed
            tb.userA.userGroups.deleteGroupUser(group1.id, userB!!.id!!)
            val groupMembersAfterRemoval = tb.userA.userGroups.listGroupMembers(group1.id)
            assertEquals(1, groupMembersAfterRemoval.size)

            //Cannot remove group admin
            tb.userA.userGroups.assertDeleteGroupUserFailStatus(400, group1.id, groupMembersAfterInvite[0].id!!)
        }
    }
}