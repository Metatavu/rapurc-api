package fi.metatavu.rapurc.api.impl.surveys

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * Static class for mapping data object to classes
 */
class DataMapperUtils {
    companion object {

        /**
         * Get event data
         *
         * @param data data
         * @return event data
         */
        inline fun <reified T> getData(data: Any): T {
            val objectMapper = jacksonObjectMapper()
            return objectMapper.readValue(objectMapper.writeValueAsBytes(data))
        }
    }
}