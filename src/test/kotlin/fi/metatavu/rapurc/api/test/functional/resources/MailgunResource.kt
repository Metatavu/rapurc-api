package fi.metatavu.rapurc.api.test.functional.resources

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import fi.metatavu.rapurc.api.test.functional.settings.ApiTestSettings
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.apache.commons.codec.binary.Base64

/**
 * Resource for wiremock container
 */
class MailgunResource : QuarkusTestResourceLifecycleManager {
    private lateinit var wireMockServer: WireMockServer

    private val domain = ApiTestSettings.mailgunDomain
    private val path = ApiTestSettings.mailgunApiUrlEnding
    private val key = ApiTestSettings.mailgunApiKey

    override fun start(): Map<String, String> {
        val config: MutableMap<String, String> = HashMap()
        wireMockServer = WireMockServer(WireMockConfiguration().extensions(ResponseTemplateTransformer(true)))
        wireMockServer.start()
        mailgun()
        config["rapurc.mailgun.apiurl"] = wireMockServer.baseUrl() + '/' + path
        config["rapurc.mailgun.domain"] = domain
        config["rapurc.mailgun.apikey"] = key
        return config
    }

    override fun stop() {
        wireMockServer.stop()
    }

    /**
     * Sets up mailgun stubs
     */
    private fun mailgun() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/$path/$domain/messages"))
            .withHeader("Authorization", WireMock.equalTo(String.format("Basic %s", Base64.encodeBase64String(String.format("api:%s", key).toByteArray()))))
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(WireMock.aResponse().withStatus(200)))
    }

}