package monolyth.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import monolyth.persistence.model.MyEntityTable
import monolyth.persistence.repository.MyEntityRepository
import monolyth.util.Configuration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.koin.dsl.module
import java.sql.Connection
import javax.sql.DataSource

data class PersistenceConfig(
    val url: String,
    val driver: String,
    val username: String = "",
    val password: String = ""
)

val persistenceModule = module {
    single {
        val configuration: Configuration = get()
        val dataDir = System.getenv("DATA_DIR")
            ?: configuration.getString("dataDir")
            ?: "./"

        configuration.extract("database") ?: PersistenceConfig(
            url = "jdbc:sqlite:$dataDir/monolyth.db",
            driver = "org.sqlite.JDBC"
        )
    }

    single<DataSource>(createdAtStart = true) {
        // Configure data source
        val config: PersistenceConfig = get()
        val hikariConfig = HikariConfig().apply {
            driverClassName = config.driver
            jdbcUrl = config.url
            username = config.username
            password = config.password
        }
        val dataSource = HikariDataSource(hikariConfig)

        dataSource
    }

    single {
        Database.connect(get<DataSource>()).apply {
            transactionManager.defaultIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED
            transaction(this) {
                SchemaUtils.createMissingTablesAndColumns(MyEntityTable)
            }
        }
    }

    single { MyEntityRepository(get()) }
}
