package fi.metatavu.rapurc.api.test.functional.resources

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.rapurc.api.client.apis.SurveysApi
import fi.metatavu.rapurc.api.client.infrastructure.ApiClient
import fi.metatavu.rapurc.api.client.infrastructure.ClientException
import fi.metatavu.rapurc.api.client.models.Metadata
import fi.metatavu.rapurc.api.client.models.Survey
import fi.metatavu.rapurc.api.client.models.SurveyStatus
import fi.metatavu.rapurc.api.client.models.SurveyType
import fi.metatavu.rapurc.api.test.functional.TestBuilder
import fi.metatavu.rapurc.api.test.functional.auth.TestBuilderAuthentication
import fi.metatavu.rapurc.api.test.functional.impl.ApiTestBuilderResource
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import java.util.*

/**
 * Test builder resource for surveys
 *
 * @author Antti Leppä
 * @author Jari Nykänen
 */
class SurveyTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<Survey, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): SurveysApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return SurveysApi(testBuilder.settings.apiBasePath)
    }

    /**
     * Creates new survey with default values
     *
     * @param status survey status
     * @return created survey
     */
    fun create(groupId: UUID, status: SurveyStatus = SurveyStatus.dRAFT): Survey {
        val survey = Survey(status = status, type = SurveyType.rENOVATION, groupId = groupId, metadata = Metadata())
        val result = api.createSurvey(survey)
        return addClosable(result)
    }

    /**
     * Creates survey for user's group
     *
     * @param user user creating the survey
     * @return created survey
     */
    fun create(user: TestBuilderAuthentication): Survey {
        return addClosable(
            api.createSurvey(
                Survey(
                    status = SurveyStatus.dRAFT,
                    type = SurveyType.rENOVATION,
                    groupId = user.userGroups.list(member = true).first().id!!,
                    metadata = Metadata()
                )
            )
        )
    }

    /**
     * Creates new survey
     *
     * @param survey survey
     * @return created survey
     */
    fun create(survey: Survey): Survey {
        val result = api.createSurvey(survey)
        return addClosable(result)
    }

    /**
     * Finds a survey
     *
     * @param surveyId survey id
     * @return found survey
     */
    fun findSurvey(surveyId: UUID?): Survey {
        return api.findSurvey(surveyId!!)
    }

    /**
     * Lists surveys
     *
     * @param firstResult first result
     * @param maxResult max results
     * @param address filter by address
     * @param status filter by status
     * @param type filter by type
     * @param dateUnknown filter by date unknown
     * @param startDate filter by start date
     * @param endDate filter by end date
     * @return found surveys
     */
    fun listSurveys(
        firstResult: Int? = null,
        maxResult: Int? = null,
        address: String? = null,
        status: SurveyStatus? = null,
        type: SurveyType? = null,
        dateUnknown: Boolean? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Array<Survey> {
        return api.listSurveys(
            firstResult = firstResult,
            maxResults = maxResult,
            address = address,
            status = status,
            type = type,
            dateUnknown = dateUnknown,
            startDate = startDate,
            endDate = endDate
        )
    }

    /**
     * Updates a survey into the API
     *
     * @param body body payload
     * @return updated survey
     */
    fun updateSurvey(body: Survey): Survey {
        return api.updateSurvey(body.id!!, body)
    }

    /**
     * Deletes a survey from the API
     *
     * @param survey survey to be deleted
     */
    fun delete(survey: Survey) {
        api.deleteSurvey(survey.id!!)
        removeCloseable { closable: Any? ->
            if (closable !is Survey) {
                return@removeCloseable false
            }
            closable.id == survey.id
        }
    }

    /**
     * Asserts survey count within the system
     *
     * @param firstResult first result
     * @param maxResult max results
     * @param address filter by address
     * @param status filter by status
     * @param type filter by type
     * @param startDate filter by start date
     * @param endDate filter by end date
     * @param expected expected count
     */
    fun assertCount(
        expected: Int,
        firstResult: Int?,
        maxResult: Int?,
        address: String?,
        status: SurveyStatus?,
        type: SurveyType?,
        dateUnknown: Boolean?,
        startDate: String?,
        endDate: String?
    ) {
        assertEquals(
            expected,
            api.listSurveys(
                firstResult = firstResult,
                maxResults = maxResult,
                address = address,
                status = status,
                type = type,
                dateUnknown = dateUnknown,
                startDate = startDate,
                endDate = endDate
            ).size
        )
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param surveyId survey id
     */
    fun assertFindFailStatus(expectedStatus: Int, surveyId: UUID?) {
        try {
            api.findSurvey(surveyId!!)
            fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts create status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param status status
     */
    fun assertCreateFailStatus(expectedStatus: Int, status: SurveyStatus, groupId: UUID) {
        try {
            create(status = status, groupId = groupId)
            fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts update status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param survey survey
     */
    fun assertUpdateFailStatus(expectedStatus: Int, survey: Survey) {
        try {
            updateSurvey(survey)
            fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts delete status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param survey survey
     */
    fun assertDeleteFailStatus(expectedStatus: Int, survey: Survey) {
        try {
            api.deleteSurvey(survey.id!!)
            fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts list status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param firstResult first result
     * @param maxResult max results
     * @param address filter by address
     * @param status filter by status
     * @param type filter by type
     * @param startDate filter by start date
     * @param endDate filter by end date
     */
    fun assertListFailStatus(
        expectedStatus: Int,
        firstResult: Int?,
        maxResult: Int?,
        address: String?,
        status: SurveyStatus?,
        type: SurveyType?,
        dateUnknown: Boolean?,
        startDate: String?,
        endDate: String?
    ) {
        try {
            api.listSurveys(
                firstResult = firstResult,
                maxResults = maxResult,
                address = address,
                status = status,
                type = type,
                dateUnknown = dateUnknown,
                startDate = startDate,
                endDate = endDate
            )
            fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    override fun clean(survey: Survey) {
        api.deleteSurvey(survey.id!!)
    }

}