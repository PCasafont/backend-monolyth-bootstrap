package monolyth.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.Json
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.takeFrom
import kotlinx.coroutines.runBlocking

fun createHttpClient(
    authConfig: HttpAuthConfig? = null,
    contentTypes: List<ContentType>? = null,
    connectionTimeout: Int = 5_000
) = HttpClient(Apache) {
    Json {
        contentTypes?.forEach {
            accept(it)
        }
    }
    if (authConfig != null) {
        Auth {
            basic {
                username = authConfig.username
                password = authConfig.password
                sendWithoutRequest = true
            }
        }
    }
    engine {
        socketTimeout = connectionTimeout
        connectTimeout = connectionTimeout
        connectionRequestTimeout = connectionTimeout * 2
    }
}

data class HttpAuthConfig(
    val username: String,
    val password: String
)

suspend inline fun <reified T> HttpClient.get(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T = try {
    get {
        url.takeFrom(urlString)
        block()
    }
} catch (e: Exception) {
    throw ApiCallException("Error calling $urlString: ${e.message}", e)
}

class ApiCallException(
    override val message: String,
    override val cause: Throwable
) : IllegalStateException()
