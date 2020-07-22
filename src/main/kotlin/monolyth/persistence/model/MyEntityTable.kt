package monolyth.persistence.model

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime

object MyEntityTable : LongIdTable("my_entity") {
    val uniqueProperty = varchar("unique_property", 50).uniqueIndex()
    val data1 = varchar("data_1", 20)
    val data2 = long("data_2")
    val data3 = varchar("data_3", 250)
    val createdAt = datetime("created_at")
}

data class EntityRecord(
    val id: Long,
    val uniqueProperty: String,
    val data1: String,
    val data2: Long,
    val data3: String,
    val createdAt: LocalDateTime
)

data class MyEntityInsertRecord(
    val uniqueProperty: String,
    val data1: String,
    val data2: Long,
    val data3: String
)

fun ResultRow.toEntityRecord() = EntityRecord(
    this[MyEntityTable.id].value,
    this[MyEntityTable.uniqueProperty],
    this[MyEntityTable.data1],
    this[MyEntityTable.data2],
    this[MyEntityTable.data3],
    this[MyEntityTable.createdAt]
)

fun InsertStatement<EntityID<Long>>.setRecord(
    record: MyEntityInsertRecord
) {
    this[MyEntityTable.uniqueProperty] = record.uniqueProperty
    this[MyEntityTable.data1] = record.data1
    this[MyEntityTable.data2] = record.data2
    this[MyEntityTable.data3] = record.data3
    this[MyEntityTable.createdAt] = LocalDateTime.now()
}
