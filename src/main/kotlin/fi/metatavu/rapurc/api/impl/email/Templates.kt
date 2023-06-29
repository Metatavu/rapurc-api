package fi.metatavu.rapurc.api.impl.email

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance

/**
 * Class for email checked templates
 */
@CheckedTemplate
object Templates {

    @JvmStatic
    external fun joinRequestEmail(userFullName: String, groupName: String): TemplateInstance

    @JvmStatic
    external fun joinRequestEmailSubject(userFullName: String, groupName: String): TemplateInstance

    @JvmStatic
    external fun joinRequestAcceptedEmail(groupName: String): TemplateInstance

    @JvmStatic
    external fun joinRequestRejectedEmail(groupName: String): TemplateInstance

    @JvmStatic
    external fun joinRequestAcceptedEmailSubject(groupName: String): TemplateInstance

    @JvmStatic
    external fun joinRequestRejectedEmailSubject(groupName: String): TemplateInstance

    @JvmStatic
    external fun userInviteEmail(groupName: String): TemplateInstance

    @JvmStatic
    external fun userInviteEmailSubject(groupName: String): TemplateInstance

    @JvmStatic
    external fun userInviteAcceptedEmail(invitedUserFullName: String, groupName: String): TemplateInstance

    @JvmStatic
    external fun userInviteAcceptedEmailSubject(invitedUserFullName: String, groupName: String): TemplateInstance

    @JvmStatic
    external fun userInviteRejectedEmail(invitedUserFullName: String, groupName: String): TemplateInstance

    @JvmStatic
    external fun userInviteRejectedEmailSubject(invitedUserFullName: String, groupName: String): TemplateInstance

}