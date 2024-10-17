package app.tinygiants.getalife.data.remote.firebase.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class BudgetDto(
    val name: String? = null,
    val categories: Map<String, GroupDto>? = null
)

data class GroupDto(
    val id: String? = null,
    val name: String? = null,
    @field:JvmField val isExpanded: Boolean? = null,
    val position: Int? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val items: Map<Long, CategoryDto>? = null
)

data class CategoryDto(
    val id: String? = null,
    val name: String? = null,
    val budgetTarget: Double? = null,
    val availableMoney: Double? = null,
    val position: Int? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)