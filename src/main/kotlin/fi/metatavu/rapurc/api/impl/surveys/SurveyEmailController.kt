package fi.metatavu.rapurc.api.impl.surveys

import fi.metatavu.rapurc.api.impl.KeycloakController
import fi.metatavu.rapurc.api.impl.buildings.BuildingController
import fi.metatavu.rapurc.api.impl.email.EmailController
import fi.metatavu.rapurc.api.impl.email.Templates
import fi.metatavu.rapurc.api.impl.groups.GroupJoinController
import fi.metatavu.rapurc.api.model.BuildingDemolitionEmailTemplate
import fi.metatavu.rapurc.api.model.EmailType
import fi.metatavu.rapurc.api.persistence.model.Survey
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for sending user's email concerning survey data
 */
@ApplicationScoped
class SurveyEmailController {

    @Inject
    @ConfigProperty(name = "rapurc.ui.url")
    lateinit var url: String

    @Inject
    lateinit var buildingController: BuildingController

    @Inject
    lateinit var groupJoinController: GroupJoinController

    @Inject
    lateinit var emailController: EmailController

    @Inject
    lateinit var keycloakController: KeycloakController

    /**
     * Sends survey email, if necessary invites the user to the group if it needs to access the resource
     *
     * @param survey the survey that info is sent about
     * @param emailAddress receiver email address
     * @param emailType type of email message
     * @param userId user id
     * @param emailData email data if any
     */
    fun sendSurveyEmail(
        survey: Survey,
        emailAddress: String,
        emailType: EmailType,
        userId: UUID,
        emailData: Any?
    ): String? {
        val requiresInvite = emailType == EmailType.BUILDING_DEMOLITION_CONTACT_UPDATE
        val groupId = survey.keycloakGroupId!!

        when (emailType) {
            EmailType.BUILDING_DEMOLITION_CONTACT_UPDATE -> {
                if (emailData == null) return "Email data must not be null"
                val data = DataMapperUtils.getData<BuildingDemolitionEmailTemplate>(emailData)
                val building = buildingController.find(data.buildingId) ?: return "Building not found"
                if (building.survey?.id != survey.id) return "Building not found"
                emailController.sendEmail(
                    to = emailAddress,
                    subject = Templates.buildingDemolitionContactUpdateSubject(building.propertyName ?: "").render(),
                    content = Templates.buildingDemolitionContactUpdateBody("${url}surveys/${survey.id}/reusables").render()
                )
            }
        }

        if (requiresInvite) {
            var alreadyInGroup = false
            val invitedUser = keycloakController.findUserByEmail(emailAddress)
            if (invitedUser != null) {
                alreadyInGroup = keycloakController
                    .getUserGroups(UUID.fromString(invitedUser.id))
                    .map { it.id }
                    .contains(groupId.toString())
            }
            if (alreadyInGroup) return null

            val targetGroup = keycloakController.findGroupById(groupId) ?: return "Failed to find survey's group"
            val targetGroupAdmin = keycloakController.findGroupAdmin(targetGroup) ?: return "Filed to find group's admin"
            groupJoinController.createGroupInvite(
                joiningUserEmail = emailAddress,
                groupId = groupId,
                groupName = targetGroup.name!!,
                groupAdmin = targetGroupAdmin,
                userId = userId
            )
        }

        return null
    }
}