package monolyth.service

import monolyth.persistence.model.MyEntityInsertRecord
import monolyth.persistence.repository.MyEntityRepository
import monolyth.util.createLogger
import java.time.LocalDateTime

private val logger = createLogger {}

class MyEntityService(
    private val myEntityRepository: MyEntityRepository
) {
    fun create(uniqueProperty: String, data1: String, data2: Long, data3: String): MyEntity {
        logger.info { "Creating entity $uniqueProperty" }
        val id = myEntityRepository.create(MyEntityInsertRecord(uniqueProperty, data1, data2, data3))
        return MyEntity(id, uniqueProperty, data1, data2, data3, LocalDateTime.now())
    }

    fun getById(id: Long) = myEntityRepository.getById(id)?.let {
        MyEntity(it.id, it.uniqueProperty, it.data1, it.data2, it.data3, it.createdAt)
    }

    fun getByUniqueProperty(uniqueProperty: String) = myEntityRepository.getByUniqueProperty(uniqueProperty)?.let {
        MyEntity(it.id, it.uniqueProperty, it.data1, it.data2, it.data3, it.createdAt)
    }

    fun increaseSavings(myEntity: MyEntity, x: Long) {
        myEntity.data2 += x
        myEntityRepository.setData2(myEntity.id, myEntity.data2)
    }
}

data class MyEntity(
    val id: Long,
    val uniqueProperty: String,
    val data1: String,
    var data2: Long,
    val data3: String,
    val createdAt: LocalDateTime
)
