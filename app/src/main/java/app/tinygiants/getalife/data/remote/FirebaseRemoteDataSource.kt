package app.tinygiants.getalife.data.remote

import app.tinygiants.getalife.data.remote.dto.BudgetDto
import app.tinygiants.getalife.di.Io
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class FirebaseRemoteDataSource @Inject constructor(
    @Io private val ioDispatcher: CoroutineDispatcher,
    db: FirebaseFirestore
) {

    fun getCategories(): Flow<BudgetDto?> = callbackFlow {
        val subscription = budgetCategoriesRef.addSnapshotListener { snapshot, _ ->
            if (snapshot!!.exists()) {
                trySend(snapshot.toObject(BudgetDto::class.java))
            }
        }

        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    private val budgetCategoriesRef = db.collection("budgets")
        .document("Us2rTV7fVkzM5N1iGStG")
}