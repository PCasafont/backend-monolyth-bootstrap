package monolyth.service

import org.koin.dsl.module

val serviceModule = module {
    single { MyEntityService(get()) }
}
