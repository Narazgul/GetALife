package app.tinygiants.getalife.data.remote.dto

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

//fun BudgetDto?.toBudgetListElement(): Result<List<Group>> {
//    return try {
//        val budgetDto = this ?: return Result.success(emptyList())
//
//        val sortedCategoryGroups = budgetDto.categories
//            ?.toList()
//            ?.sortedBy { it.second.position ?: Int.MIN_VALUE }
//            ?.toMap()
//
//        val budgetList = mutableListOf<Group>()
//
//        sortedCategoryGroups?.forEach { (_, groupDto) ->
//
//            val header = groupDto.toHeader()
//            val items = groupDto.items?.values
//                ?.toList()
//                ?.sortedBy { categoryDto -> categoryDto.position }
//                ?.map { categoryDto ->  categoryDto.toCategory() }
//                .orEmpty()
//
//            val group = Group(
//                id = 0,
//                header = header,
//                items = items,
//            )
//
//            budgetList.add(element = group)
//        }
//
//        Result.success(budgetList)
//    } catch (exception: Exception) {
//        Result.failure(exception)
//    }
//}