package app.tinygiants.getalife.data.remote.firebase.dto

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

const val firestoreBudgetId: String = "FfxNsKxEDHTuUw9z193n"

const val pathBudgets: String = "budgets"

const val firestoreBudgetName = "budgetName"
const val firestoreCategories = "categories"
const val firestoreName = "name"
const val firestoreItems = "items"
const val firestoreIsExpanded = "isExpanded"

class FirebaseRemoteDataSource @Inject constructor() {

    private val reference = Firebase.firestore
        .collection(pathBudgets)
        .document(firestoreBudgetId)

    fun getCategories(): Flow<Result<BudgetDto?>> = callbackFlow {
        val subscription = reference.addSnapshotListener { snapshot, error ->

            error?.let {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }

            snapshot?.let { document ->
                val budgetDto = document.toObject(BudgetDto::class.java)
                trySend(Result.success(budgetDto))
            }
        }

        awaitClose { subscription.remove() }
    }

    fun toggleIsExpanded(headerId: String, isExpanded: Boolean) {
        reference.update("$firestoreCategories.$headerId.$firestoreIsExpanded", isExpanded)
    }

    fun updateBudgetName(name: String) {
        reference.update(firestoreBudgetName, name)
    }

    fun addCategoryGroup(budgetList: Map<Group, List<Category>>) {
        var headerPosition = 0

        val categoryGroup = budgetList.map { (header, items) ->
            val categoryItems = items.mapIndexed { index, item ->
                item.id to CategoryDto(
                    id = item.id.toString(),
                    name = item.name,
                    budgetTarget = item.budgetTarget?.value,
                    availableMoney = item.availableMoney.value,
                    position = index
                )
            }.toMap()

            header.id to GroupDto(
                id = header.id.toString(),
                name = header.name,
                isExpanded = false,
                position = headerPosition++,
                items = categoryItems
            )
        }.toMap()

        reference.update(firestoreCategories, categoryGroup)
    }

    fun updateCategoryGroupName(headerId: String, name: String) {
        reference.update("$firestoreCategories.$headerId.$firestoreName", name)
    }

    fun addCategory(budgetList: Map<Group, List<Category>>) {
        var headerPosition = 0

        val categoryGroup = budgetList.map { (header, items) ->
            val categoryItems = items.mapIndexed { index, item ->
                item.id to CategoryDto(
                    id = item.id.toString(),
                    name = item.name,
                    budgetTarget = item.budgetTarget?.value,
                    availableMoney = item.availableMoney.value,
                    position = index
                )
            }.toMap()

            header.id to GroupDto(
                id = header.id.toString(),
                name = header.name,
                isExpanded = false,
                position = headerPosition++,
                items = categoryItems
            )
        }.toMap()

        reference.update(firestoreCategories, categoryGroup)
    }
}