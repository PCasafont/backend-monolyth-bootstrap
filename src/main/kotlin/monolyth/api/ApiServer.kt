package monolyth.api

import monolyth.api.controller.ApiController
import monolyth.api.controller.statusPages
import monolyth.util.createLogger
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.schema.namer.DefaultSchemaNamer
import com.papsign.ktor.openapigen.schema.namer.SchemaNamer
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlin.reflect.KType

private val logger = createLogger {}

const val API_VERSION = "0.1"

class ApiServer(
    private val config: ApiConfig,
    private val controllers: List<ApiController>
) {
    private var server: ApplicationEngine? = null

    fun start() {
        if (server != null) {
            return
        }

        logger.info { "Starting HTTP API on port ${config.port}" }

        server = embeddedServer(Netty, port = config.port) {
            install(DefaultHeaders)
            install(CallLogging)
            install(CORS) {
                anyHost()
                allowNonSimpleContentTypes = true
            }

            install(OpenAPIGen) {
                info {
                    version = API_VERSION
                    title = "Monolyth API"
                    description = "This is the Monolyth API"
                    contact {
                        name = "Monolyth"
                        email = "https://monolythme.com"
                    }
                }
                replaceModule(DefaultSchemaNamer, object: SchemaNamer {
                    val regex = Regex("[A-Za-z0-9_.]+")
                    override fun get(type: KType): String {
                        return type.toString().replace(regex) { it.value.split(".").last() }.replace(Regex(">|<|, "), "_")
                    }
                })
            }

            statusPages()

            install(ContentNegotiation) {
                jackson {
                    enable(
                        DeserializationFeature.WRAP_EXCEPTIONS,
                        DeserializationFeature.USE_BIG_INTEGER_FOR_INTS,
                        DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS
                    )
                    enable(SerializationFeature.WRAP_EXCEPTIONS, SerializationFeature.INDENT_OUTPUT)
                    setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                        indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                        indentObjectsWith(DefaultIndenter("  ", "\n"))
                    })
                    registerModule(JavaTimeModule())
                }
            }

            val metricsRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
            install(MicrometerMetrics) {
                registry = metricsRegistry
                meterBinders = listOf(
                    UptimeMetrics(),
                    ProcessorMetrics(),
                    LogbackMetrics(),
                    JvmThreadMetrics(),
                    JvmMemoryMetrics(),
                    JvmGcMetrics(),
                    ClassLoaderMetrics()
                )
            }

            install(Locations)
            routing {
                get("openapi.json") {
                    val application = application
                    call.respond(application.openAPIGen.api)
                }
                get("/") {
                    call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
                }
                get("metrics") {
                    call.respond(metricsRegistry.scrape())
                }
            }

            apiRouting {
                for (controller in controllers) {
                    with(controller) {
                        registerApi()
                    }
                }
            }
        }.start()
    }

    fun stop() {
        server?.stop(300, 1000)
        server = null
    }
}
