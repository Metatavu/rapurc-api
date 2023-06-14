package fi.metatavu.rapurc.api.test.functional.tests

import fi.metatavu.rapurc.api.test.functional.settings.ApiTestSettings
import fi.metatavu.rapurc.api.test.functional.resources.MailgunMocker

abstract class AbstractTestIT {

    /**
     * Creates a mailgun mocker
     *
     * @return mailgun mocker
     */
    protected fun getMailgunMocker(): MailgunMocker {
        return MailgunMocker(String.format("/%s", ApiTestSettings.mailgunApiUrlEnding), ApiTestSettings.mailgunDomain)
    }

}