package fi.metatavu.rapurc.api.impl.email

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance

/**
 * Class for email checked templates
 */
@CheckedTemplate
object Templates {

    /**
     * Returns email template for join request
     *
     * @param userFullName user full name
     * @param groupName group name
     * @return email template
     */
    @JvmStatic
    external fun joinRequestEmail(userFullName: String, groupName: String): TemplateInstance

    /**
     * Returns email template for join request subject
     *
     * @param userFullName user full name
     * @param groupName group name
     * @return email template
     */
    @JvmStatic
    external fun joinRequestEmailSubject(userFullName: String, groupName: String): TemplateInstance

    /**
     * Returns email template for join request update
     *
     * @param newStatus new status
     * @param groupName group name
     * @return email template
     */
    @JvmStatic
    external fun joinRequestUpdateEmail(newStatus: String, groupName: String): TemplateInstance

    /**
     * Returns email template for join request update subject
     *
     * @param newStatus new status
     * @param groupName group name
     * @return email template
     */
    @JvmStatic
    external fun joinRequestUpdateEmailSubject(newStatus: String, groupName: String): TemplateInstance

    /**
     * Returns email template for invite
     *
     * @param groupName group name
     * @return email template
     */
    @JvmStatic
    external fun userInviteEmail(groupName: String): TemplateInstance

    /**
     * Returns email template for invite subject
     *
     * @param groupName group name
     * @return email template
     */
    @JvmStatic
    external fun userInviteEmailSubject(groupName: String): TemplateInstance

    /**
     * Returns email template for invite update
     *
     * @param invitedUserFullName invited user full name
     * @param newStatus new status
     * @param groupName group name
     * @return email template
     */
    @JvmStatic
    external fun userInviteUpdateEmail(invitedUserFullName: String, newStatus: String, groupName: String): TemplateInstance

    /**
     * Returns email template for invite update subject
     *
     * @param invitedUserFullName invited user full name
     * @param newStatus new status
     * @param groupName group name
     * @return email template
     */
    @JvmStatic
    external fun userInviteUpdateEmailSubject(invitedUserFullName: String, newStatus: String, groupName: String): TemplateInstance

}