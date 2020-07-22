package monolyth.persistence.repository

import monolyth.persistence.model.MyEntityInsertRecord
import monolyth.persistence.model.EntityRecord
import monolyth.persistence.model.MyEntityTable
import monolyth.persistence.model.setRecord
import monolyth.persistence.model.toEntityRecord
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class MyEntityRepository(
    private val database: Database
) {
    fun getById(id: Long): EntityRecord? = transaction(database) {
        MyEntityTable.select {
            MyEntityTable.id eq id
        }.firstOrNull()?.toEntityRecord()
    }

    fun getByUniqueProperty(uniqueProperty: String): EntityRecord? = transaction(database) {
        MyEntityTable.select {
            MyEntityTable.uniqueProperty eq uniqueProperty
        }.firstOrNull()?.toEntityRecord()
    }

    fun create(record: MyEntityInsertRecord) = transaction(database) {
        MyEntityTable.insertAndGetId {
            it.setRecord(record)
        }.value
    }

    fun setData2(id: Long, x: Long) = transaction(database) {
        MyEntityTable.update({
            MyEntityTable.id eq id
        }) {
            it[data2] = x
        } > 0
    }
}
