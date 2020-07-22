
package monolyth.api.controller

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BadRequestException(override val message: String) : Exception()
class NotFoundException(override val message: String) : Exception()
class InternalErrorException(override val message: String) : Exception()

fun Application.statusPages() {
    install(StatusPages) {
        exception<BadRequestException> {
            call.respond(HttpStatusCode.BadRequest, ApiError(it.message))
        }
        exception<JsonParseException> {
            call.respond(HttpStatusCode.BadRequest, ApiError("Invalid JSON"))
        }
        exception<JsonMappingException> {
            call.respond(HttpStatusCode.BadRequest, ApiError(it.message ?: "Unable to parse JSON body"))
        }
        exception<MissingKotlinParameterException> {
            call.respond(HttpStatusCode.BadRequest, ApiError("Missing body parameter: ${it.parameter.name}"))
        }
        exception<NotFoundException> {
            call.respond(HttpStatusCode.NotFound, ApiError(it.message))
        }
        exception<InternalErrorException> {
            logger.info { "Internal error response returned: ${it.message}" }
            call.respond(HttpStatusCode.InternalServerError, ApiError(it.message))
        }
        exception<Exception> {
            logger.warn("Unhandled exception", it)
            // FIXME don't expose that to the consumer
            call.respond(HttpStatusCode.InternalServerError, ApiError("Unhandled exception [${it::class.simpleName}]: ${it.message}"))
            //call.respond(HttpStatusCode.InternalServerError, ApiError("Internal Error! Please check the server logs"))
        }
    }
}

data class ApiError(
    val message: String
)
