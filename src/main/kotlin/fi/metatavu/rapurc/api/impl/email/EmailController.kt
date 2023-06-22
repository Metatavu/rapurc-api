package fi.metatavu.rapurc.api.impl.email

import io.vertx.core.MultiMap
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.client.WebClient
import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Mailgun controller
 */
@ApplicationScoped
class EmailController {

    @Inject
    @ConfigProperty(name = "rapurc.mailgun.apikey")
    lateinit var apiKey: String

    @Inject
    @ConfigProperty(name = "rapurc.mailgun.domain")
    lateinit var domain: String
    @Inject
    @ConfigProperty(name = "rapurc.mailgun.apiurl")
    lateinit var mailgunUrl: String

    @Inject
    lateinit var vertx: io.vertx.core.Vertx

    /**
     * Sends an email
     *
     * @param to email which will receive this email
     * @param subject email subject
     * @param content email content
     */
    fun sendEmail(to: String, from: String, subject: String, content: String) {
        val client: WebClient = WebClient.create(vertx)
        client.requestAbs(
            HttpMethod.POST,
            "$mailgunUrl/$domain/messages"
        ).basicAuthentication("api", apiKey)
            .sendForm(
                MultiMap.caseInsensitiveMultiMap()
                    .add("to", to)
                    .add("subject", subject)
                    .add("text", content)
                    .add("from", from)
            )
    }
}