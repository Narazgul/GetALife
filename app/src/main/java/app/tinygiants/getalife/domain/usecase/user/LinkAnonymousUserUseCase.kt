package app.tinygiants.getalife.domain.usecase.user

import android.util.Log
import app.tinygiants.getalife.data.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LinkAnonymousUserUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(anonymousUserId: String, newUserId: String) {
        val currentUser = firebaseAuth.currentUser ?: return

        Log.d("LinkAnonymousUser", "Linking account: anonymous=$anonymousUserId, new=$newUserId")

        // Skip if the anonymous user ID and new user ID are the same
        if (anonymousUserId == newUserId) {
            Log.d("LinkAnonymousUser", "User IDs are the same, skipping link")
            return
        }

        val userName = currentUser.displayName ?: "My Budget"

        Log.d("LinkAnonymousUser", "Starting budget linking process for user: $userName")

        budgetRepository.linkAnonymousAccount(
            anonymousUserId = anonymousUserId,
            authenticatedUserId = newUserId,
            userName = userName
        )

        Log.d("LinkAnonymousUser", "Budget linking completed")
    }

    // Legacy method for backward compatibility - deprecated
    @Deprecated("Use the overload that accepts anonymousUserId parameter")
    suspend operator fun invoke(newUserId: String) {
        val currentUser = firebaseAuth.currentUser ?: return
        val anonymousUserId = currentUser.uid.takeIf { currentUser.isAnonymous } ?: return

        if (anonymousUserId != newUserId) {
            val userName = currentUser.displayName ?: "My Budget"
            budgetRepository.linkAnonymousAccount(
                anonymousUserId = anonymousUserId,
                authenticatedUserId = newUserId,
                userName = userName
            )
        }
    }
}