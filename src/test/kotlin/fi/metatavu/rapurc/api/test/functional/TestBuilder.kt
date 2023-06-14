package fi.metatavu.rapurc.api.test.functional

import fi.metatavu.rapurc.api.client.infrastructure.ApiClient
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider
import fi.metatavu.rapurc.api.test.functional.auth.TestBuilderAuthentication
import fi.metatavu.rapurc.api.test.functional.settings.ApiTestSettings
import org.eclipse.microprofile.config.ConfigProvider

/**
 * Abstract test builder class
 *
 * @author Jari Nykänen
 */
class TestBuilder: AbstractTestBuilder<ApiClient>() {

    val settings = ApiTestSettings()

    val admin = createTestBuilderAuthentication(username = "admin", password = "adminPassword")
    val userA = createTestBuilderAuthentication(username = "user_a", password = "passworda")
    val userB = createTestBuilderAuthentication(username = "user_b", password = "passwordb")
    val userC = createTestBuilderAuthentication(username = "user_c", password = "passwordc")

    override fun createTestBuilderAuthentication(
        testBuilder: AbstractTestBuilder<ApiClient>,
        accessTokenProvider: AccessTokenProvider
    ): AuthorizedTestBuilderAuthentication<ApiClient> {
        return TestBuilderAuthentication(this, accessTokenProvider)
    }

    /**
     * Creates test builder authenticatior for given user
     *
     * @param username username
     * @param password password
     * @return test builder authenticatior for given user
     */
    private fun createTestBuilderAuthentication(username: String, password: String): TestBuilderAuthentication {
        val authServerUrl: String = ConfigProvider.getConfig().getValue("keycloak.url", String::class.java)
        val realm: String = ConfigProvider.getConfig().getValue("keycloak.realm", String::class.java)
        val clientId = "test"
        return TestBuilderAuthentication(this, KeycloakAccessTokenProvider(authServerUrl, realm, clientId, username, password, null))
    }
}