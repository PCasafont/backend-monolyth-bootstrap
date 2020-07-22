@file:JvmName("MonolythServer")

package monolyth

import monolyth.util.createLogger
import org.koin.core.context.startKoin
import monolyth.api.ApiServer
import monolyth.api.apiModule
import monolyth.persistence.persistenceModule
import monolyth.service.serviceModule
import monolyth.util.debugWarn
import kotlin.system.exitProcess

private val logger = createLogger {}

fun main() {
    val koin = startKoin {
        modules(
            listOf(
                configModule(),
                serviceModule,
                persistenceModule,
                apiModule
            )
        )
    }.koin

    val apiServer: ApiServer = koin.get()

    try {
        apiServer.start()
    } catch (e: Exception) {
        logger.debugWarn(e) { "There was an exception during startup" }
        logger.info { "Exiting..." }
        exitProcess(1)
    }
}
