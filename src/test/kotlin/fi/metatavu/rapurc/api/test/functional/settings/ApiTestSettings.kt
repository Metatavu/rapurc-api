package fi.metatavu.rapurc.api.test.functional.settings

/**
 * Settings implementation for test builder
 *
 * @author Jari Nykänen
 */
class ApiTestSettings {

    /**
     * Returns API service base path
     */
    val apiBasePath: String
        get() = "http://localhost:8081"


    companion object {
        // Mailgun settings

        const val mailgunDomain = "example.com"
        const val mailgunApiKey = "apiKey"
        const val mailgunApiUrlEnding = "mgapi"
        const val mailgunSenderEmail = "noreply@example.com"
    }

}
