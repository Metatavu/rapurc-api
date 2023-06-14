package fi.metatavu.rapurc.api.test.functional.resources

import com.github.tomakehurst.wiremock.client.WireMock
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair

/**
 * Mocker for Mailgun API
 *
 *
 * Inspired by https://github.com/sargue/mailgun/blob/master/src/test/java/net/sargue/mailgun/test/BasicTests.java
 *
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
class MailgunMocker(private val basePath: String, private val domain: String) {

    /**
     * Verifies that HTML email has been sent
     *
     * @param fromEmail from email
     * @param to        to email
     * @param subject   subject
     * @param content   content
     */
    fun verifyTextMessageSent(fromEmail: String, to: String, subject: String, content: String) {
        verifyMessageSent(createParameterList(fromEmail, to, subject, content))
    }

    /**
     * Verifies that HTML email has been sent n-times
     *
     * @param count     count
     * @param fromEmail from email
     * @param to        to email
     * @param subject   subject
     * @param content   content
     */
    fun verifyTextMessageSent(count: Int, fromEmail: String, to: String, subject: String, content: String) {
        verifyMessageSent(count, createParameterList(fromEmail, to, subject, content))
    }

    /**
     * Creates parameter list
     *
     * @param fromEmail from email
     * @param to        to email
     * @param subject   subject
     * @param content   content
     */
    private fun createParameterList(fromEmail: String, to: String, subject: String, content: String): List<NameValuePair> {
        return listOf<NameValuePair>(
            BasicNameValuePair("to", to),
            BasicNameValuePair("subject", subject),
            BasicNameValuePair("text", content),
            BasicNameValuePair("from", fromEmail)
        )
    }

    private fun createParameterList(fromEmail: String, to: String, subject: String): List<NameValuePair> {
        return listOf<NameValuePair>(
            BasicNameValuePair("to", to),
            BasicNameValuePair("subject", subject),
            BasicNameValuePair("from", fromEmail)
        )
    }

    /**
     * Verifies that email with parameters has been sent
     *
     * @param parametersList parameters
     */
    private fun verifyMessageSent(parametersList: List<NameValuePair>) {
        val parameters: List<NameValuePair> = ArrayList(parametersList)
        val form = URLEncodedUtils.format(parameters, "UTF-8")
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(apiUrl)).withRequestBody(WireMock.equalTo(form)))
    }

    /**
     * Verifies that email with parameters has been sent n-times
     *
     * @param count count
     * @param parametersList parameters
     */
    private fun verifyMessageSent(count: Int, parametersList: List<NameValuePair>) {
        val parameters: List<NameValuePair> = ArrayList(parametersList)
        val form = URLEncodedUtils.format(parameters, "UTF-8")
        WireMock.verify(count, WireMock.postRequestedFor(WireMock.urlEqualTo(apiUrl)).withRequestBody(WireMock.equalTo(form)))
    }

    /**
     * Returns API URL
     *
     * @return API URL
     */
    private val apiUrl: String
        get() = String.format("%s/%s/messages", basePath, domain)
}