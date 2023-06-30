package fi.metatavu.rapurc.api.test.functional.tests

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
 * Tests for Attachments API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlTestResource::class)
)
class AttachmentTestIT {

    /**
     * Tests attachment creation
     */
    @Test
    fun create() {
        TestBuilder().use {
            val userAGroup = it.userA.userGroups.list(member = true).first()
            val userBGroup = it.userB.userGroups.list(member = true).first()
            val survey1Id = it.userA.surveys.create(userAGroup.id!!).id!!
            val survey2Id = it.userB.surveys.create(userBGroup.id!!).id!!

            it.userA.attachments.assertCreateFailStatus(
                404,
                UUID.randomUUID(),
                it.userA.attachments.attachment
            )
            it.userA.attachments.assertCreateFailStatus(
                403,
                survey2Id,
                it.userA.attachments.attachment
            )
            val attachment = it.userA.attachments.create(survey1Id, it.userA.attachments.attachment)
            assertEquals(it.userA.attachments.attachment.name, attachment.name)
            assertEquals(it.userA.attachments.attachment.url, attachment.url)
            assertEquals(it.userA.attachments.attachment.description, attachment.description)
            assertNotNull(attachment.id)
        }
    }

    /**
     * Tests listing attachments
     */
    @Test
    fun list() {
        TestBuilder().use {
            val userAGroup = it.userA.userGroups.list(member = true).first()
            val survey1Id = it.userA.surveys.create(userAGroup.id!!).id!!
            val survey2Id = it.userA.surveys.create(userAGroup.id).id!!

            it.userA.attachments.create(survey1Id, it.userA.attachments.attachment)
            it.userA.attachments.create(survey2Id, it.userA.attachments.attachment)
            it.userA.attachments.create(survey2Id, it.userA.attachments.attachment)
            assertEquals(1, it.userA.attachments.list(survey1Id).size)
            assertEquals(2, it.userA.attachments.list(survey2Id).size)
            it.userB.attachments.assertListFailStatus(403, survey1Id)
            it.userA.attachments.assertListFailStatus(404, UUID.randomUUID())
        }
    }

    /**
     * Tests finding attachments
     */
    @Test
    fun find() {
        TestBuilder().use {
            val userAGroup = it.userA.userGroups.list(member = true).first()
            val userBGroup = it.userB.userGroups.list(member = true).first()

            val survey1Id = it.userA.surveys.create(userAGroup.id!!).id!!
            val survey2Id = it.userB.surveys.create(userBGroup.id!!).id!!
            val createdAttachment =
                it.userA.attachments.create(survey1Id, it.userA.attachments.attachment)

            it.userB.attachments.assertFindFailStatus(403, survey1Id, createdAttachment.id)
            it.userB.attachments.assertFindFailStatus(403, survey2Id, createdAttachment.id)
            it.userA.attachments.assertFindFailStatus(404, UUID.randomUUID(), createdAttachment.id)
            it.userA.attachments.assertFindFailStatus(404, survey1Id, UUID.randomUUID())

            val foundAttachment =
                it.userA.attachments.findAttachment(survey1Id, createdAttachment.id)
            assertEquals(createdAttachment.name, foundAttachment.name)
            assertEquals(createdAttachment.url, foundAttachment.url)
            assertEquals(createdAttachment.description, foundAttachment.description)
            assertEquals(createdAttachment.id, foundAttachment.id)
        }
    }

    /**
     * Tests attachment updates
     */
    @Test
    fun update() {
        TestBuilder().use {
            val userAGroup = it.userA.userGroups.list(member = true).first()
            val userBGroup = it.userB.userGroups.list(member = true).first()

            val survey1Id = it.userA.surveys.create(userAGroup.id!!).id!!
            val survey2Id = it.userB.surveys.create(userBGroup.id!!).id!!

            val attachment = it.userA.attachments.create(survey1Id, it.userA.attachments.attachment)
            val updateData = attachment.copy(
                url = "http://newurl.png",
                name = "new name"
            )
            it.userB.attachments.assertUpdateFailStatus(403, survey1Id, attachment.id, updateData)
            it.userB.attachments.assertUpdateFailStatus(403, survey2Id, attachment.id, updateData)
            it.userA.attachments.assertUpdateFailStatus(
                404,
                UUID.randomUUID(),
                attachment.id,
                updateData
            )
            it.userA.attachments.assertUpdateFailStatus(
                404,
                survey1Id,
                UUID.randomUUID(),
                updateData
            )
            val updatedAttachment =
                it.userA.attachments.update(survey1Id, attachment.id, updateData)
            assertEquals(updateData.name, updatedAttachment.name)
            assertEquals(updateData.url, updatedAttachment.url)
            assertEquals(updateData.description, updatedAttachment.description)
            assertEquals(updateData.id, updatedAttachment.id)

        }
    }

    /**
     * Tests attachment deletion
     */
    @Test
    fun delete() {
        TestBuilder().use {
            val userAGroup = it.userA.userGroups.list(member = true).first()
            val userBGroup = it.userB.userGroups.list(member = true).first()

            val survey1Id = it.userA.surveys.create(userAGroup.id!!).id!!
            val survey2Id = it.userB.surveys.create(userBGroup.id!!).id!!

            val attachment = it.userA.attachments.create(survey1Id, it.userA.attachments.attachment)

            it.userB.attachments.assertDeleteFailStatus(403, survey1Id, attachment.id)
            it.userB.attachments.assertDeleteFailStatus(403, survey2Id, attachment.id)
            it.userA.attachments.assertDeleteFailStatus(404, UUID.randomUUID(), attachment.id)
            it.userA.attachments.assertDeleteFailStatus(404, survey1Id, UUID.randomUUID())

            it.userA.attachments.delete(attachment)
            it.userA.attachments.assertFindFailStatus(404, survey1Id, attachment.id)
        }
    }
}