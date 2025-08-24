package app.tinygiants.getalife.domain.usecase.user

import android.util.Log
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.data.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case responsible for linking anonymous Firebase accounts to authenticated accounts.
 * Ensures proper data migration and user account continuity.
 */
class LinkAnonymousUserUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val firebaseAuth: FirebaseAuth,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    /**
     * Links an anonymous user account to an authenticated account.
     * Migrates budget data and ensures user continuity.
     */
    suspend operator fun invoke(
        authenticatedUserId: String,
        previousAnonymousUserId: String? = null
    ): Unit = withContext(defaultDispatcher) {

        val anonymousUserId = previousAnonymousUserId ?: getCurrentFirebaseUserId()
        val userName = firebaseAuth.currentUser?.displayName ?: "Mein Budget"

        Log.d("LinkAnonymousUserUseCase", "Linking anonymous user $anonymousUserId to authenticated user $authenticatedUserId")

        // Only link if we have a valid anonymous user ID and it's different from authenticated ID
        if (anonymousUserId != authenticatedUserId && anonymousUserId != "anonymous") {
            Log.d("LinkAnonymousUserUseCase", "Proceeding with account linking")

            // Link anonymous account data using the repository method
            budgetRepository.linkAnonymousAccount(
                anonymousUserId = anonymousUserId,
                authenticatedUserId = authenticatedUserId,
                userName = userName
            )

            Log.d("LinkAnonymousUserUseCase", "Successfully linked anonymous account data")
        } else {
            Log.d("LinkAnonymousUserUseCase", "No linking needed - user IDs are same or invalid")
        }
    }

    private fun getCurrentFirebaseUserId(): String {
        return firebaseAuth.currentUser?.uid ?: "anonymous"
    }
}