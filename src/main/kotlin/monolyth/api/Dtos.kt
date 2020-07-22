package monolyth.api

import monolyth.service.MyEntity
import com.papsign.ktor.openapigen.annotations.Request
import com.papsign.ktor.openapigen.annotations.Response
import java.time.format.DateTimeFormatter

@Response("Basic entity information")
data class MyEntityResponse(
    val id: Long,
    val uniqueProperty: String,
    val data1: String,
    val data2: Long,
    val data3: String,
    val createdAt: String
)

fun MyEntity.toResponse(): MyEntityResponse = MyEntityResponse(
    id,
    uniqueProperty,
    data1,
    data2,
    data3,
    createdAt.format(DateTimeFormatter.ISO_DATE_TIME)
)

@Request("Request to create a new entity")
data class NewEntityRequest(
    val uniqueProperty: String,
    val data1: String,
    val data2: String,
    val data3: String
)
