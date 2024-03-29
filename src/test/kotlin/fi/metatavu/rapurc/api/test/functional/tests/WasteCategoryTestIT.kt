package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.client.models.LocalizedValue
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.resources.KeycloakTestResource
import fi.metatavu.rapurc.api.test.functional.resources.MysqlTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for Waste Category API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class WasteCategoryTestIT {

    /**
     * Tests waste category creation and access rights
     */
    @Test
    fun create() {
        TestBuilder().use { testBuilder ->
            testBuilder.userA.wasteCategories.assertCreateFailStatus(403)
            val created = testBuilder.admin.wasteCategories.createDefault()
            assertEquals(testBuilder.admin.wasteCategories.wasteCategory.localizedNames[0].value, created.localizedNames.find { it.language == "en" }!!.value)
            assertEquals(testBuilder.admin.wasteCategories.wasteCategory.ewcCode, created.ewcCode)
            assertNotNull(created.metadata.createdAt)
            assertNotNull(created.metadata.creatorId)
            assertNotNull(created.metadata.modifiedAt)
            assertNotNull(created.metadata.lastModifierId)
        }
    }

    /**
     * Tests waste category listing
     */
    @Test
    fun list() {
        TestBuilder().use { testBuilder ->
            testBuilder.admin.wasteCategories.createDefault()
            testBuilder.admin.wasteCategories.createDefault()
            testBuilder.userA.wasteCategories.assertCount(2)
        }
    }

    /**
     * Tests waste category search
     */
    @Test
    fun find() {
        TestBuilder().use { testBuilder ->
            val created = testBuilder.admin.wasteCategories.createDefault()
            testBuilder.admin.wasteCategories.assertFindFailStatus(404, UUID.randomUUID())
            val found = testBuilder.admin.wasteCategories.find(created.id!!)
            assertEquals(created.id, found.id)
        }
    }

    /**
     * Tests waste category updates and access rights
     */
    @Test
    fun update() {
        TestBuilder().use { testBuilder ->
            val created = testBuilder.admin.wasteCategories.createDefault()
            val updateData = created.copy(
                ewcCode = "122",
                localizedNames = arrayOf(
                    LocalizedValue("en", "new material name en"),
                    LocalizedValue("fr", "new material name fr")
                )
            )
            testBuilder.admin.wasteCategories.assertUpdateFailStatus(404, UUID.randomUUID(), updateData)
            testBuilder.userA.wasteCategories.assertUpdateFailStatus(403, created.id!!, updateData)
            testBuilder.admin.wasteCategories.assertUpdateFailStatus(400, created.id!!, updateData.copy(localizedNames = emptyArray()))

            val updated = testBuilder.admin.wasteCategories.update(created.id, updateData)
            assertEquals(created.id, updated.id)
            assertEquals(updateData.ewcCode, updated.ewcCode)

            val sorted = updated.localizedNames.sortedBy { it.language }
            assertEquals("new material name en", sorted[0].value)
            assertEquals("en", sorted[0].language)

            assertEquals("new material name fr", sorted[1].value)
            assertEquals("fr", sorted[1].language)
        }
    }

    /**
     * Tests waste category deletion and access rights
     */
    @Test
    fun delete() {
        TestBuilder().use { testBuilder ->
            val created = testBuilder.admin.wasteCategories.createDefault()
            testBuilder.userA.wasteCategories.assertDeleteFailStatus(403, created.id!!)
            testBuilder.admin.wasteCategories.assertDeleteFailStatus(404, UUID.randomUUID())
            testBuilder.admin.wasteCategories.delete(created.id)
            testBuilder.userA.wasteCategories.assertCount(0)
        }
    }
}