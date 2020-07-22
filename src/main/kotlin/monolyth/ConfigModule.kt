package monolyth

import monolyth.util.Configuration
import org.koin.core.module.Module
import org.koin.dsl.module

fun configModule(): Module {
    // Load config with the boot options
    val configuration = Configuration()

    return module {
        single { configuration }
    }
}
