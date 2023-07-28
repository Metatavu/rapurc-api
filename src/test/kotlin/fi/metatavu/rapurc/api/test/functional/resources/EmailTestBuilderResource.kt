package fi.metatavu.rapurc.api.test.functional.resources

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.rapurc.api.client.apis.EmailsApi
import fi.metatavu.rapurc.api.client.infrastructure.ApiClient
import fi.metatavu.rapurc.api.client.infrastructure.ClientException
import fi.metatavu.rapurc.api.client.models.EmailTemplate
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.impl.ApiTestBuilderResource
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Emails API
 */
class EmailTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<EmailTemplate, ApiClient?>(testBuilder, apiClient) {


    override fun getApi(): EmailsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return EmailsApi(testBuilder.settings.apiBasePath)
    }

    /**
     * Sends email
     *
     * @param surveyId survey id
     * @param emailTemplate email Template
     */
    fun sendSurveyEmail(surveyId: UUID, emailTemplate: EmailTemplate) {
        return api.sendSurveyEmail(surveyId, emailTemplate)
    }

    /**
     * Asserts sending email failed
     *
     * @param expectedStatus expected status code
     * @param surveyId survey id
     * @param emailTemplate emailTemplate
     */
    fun assertSendSurveyEmailFailStatus(expectedStatus: Int, surveyId: UUID, emailTemplate: EmailTemplate) {
        try {
            sendSurveyEmail(surveyId, emailTemplate)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    override fun clean(emailTemplate: EmailTemplate) {
        // Nothing is persisted
    }
}