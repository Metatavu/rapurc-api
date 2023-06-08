package fi.metatavu.rapurc.api.test.functional.tests

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

            testBuilder.userA.userGroups.assertDeleteFailStatus(403, UUID.randomUUID())
            testBuilder.userB.userGroups.assertDeleteFailStatus(403, created.id!!)

            testBuilder.userA.userGroups.delete(created.id)
            assertEquals(0, testBuilder.userA.userGroups.list("usera@example.com").size)
        }
    }
}