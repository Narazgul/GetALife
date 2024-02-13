package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.data.local.entities.HeaderWithCategories
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Header
import kotlinx.coroutines.flow.Flow

@Dao
interface HeaderDao {

    @Transaction
    @Query("SELECT * FROM headers")
    fun getBudget(): Flow<List<HeaderWithCategories>>

    @Insert
    suspend fun addHeader(headerEntity: HeaderEntity)

    @Update
    suspend fun updateHeader(headerEntity: HeaderEntity)

    @Update
    suspend fun toggleIsExpanded(headerEntity: HeaderEntity)

    @Delete
    suspend fun deleteHeader(headerEntity: HeaderEntity)

}

fun mapToGroups(headersWithCategories: List<HeaderWithCategories>) =
    headersWithCategories.map { headerWithCategory ->

        val headerEntity = headerWithCategory.header
        val categoryEntities = headerWithCategory.categories

        val header = Header(
            id = headerEntity.id,
            name = headerEntity.name,
            isExpanded = headerEntity.isExpanded
        )
        val categories = categoryEntities.map { category ->
            Category(
                id = category.id,
                headerId = header.id,
                name = category.name,
                budgetTarget = category.budgetTarget,
                availableMoney = category.availableMoney,
                optionalText = category.optionalText
            )
        }

        Group(
            header = header,
            categories = categories
        )
    }

