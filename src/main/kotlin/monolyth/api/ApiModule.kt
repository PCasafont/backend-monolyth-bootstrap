package monolyth.api

import monolyth.api.controller.MyEntityController
import monolyth.util.Configuration
import org.koin.dsl.module

class ApiConfig(
    val port: Int = 8080
)

val apiModule = module {
    single {
        val config: Configuration = get()
        config.extract<ApiConfig>("api") ?: ApiConfig()
    }

    single { MyEntityController(get()) }

    single {
        ApiServer(
            get(),
            listOf(
                get<MyEntityController>()
            )
        )
    }
}
