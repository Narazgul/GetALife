package app.tinygiants.getalife.domain.usecase.user

import app.tinygiants.getalife.data.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LinkAnonymousUserUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(newUserId: String) {
        val currentUser = firebaseAuth.currentUser ?: return

        // Get the original anonymous user ID before linking
        val anonymousUserId = currentUser.uid.takeIf { currentUser.isAnonymous } ?: return

        if (anonymousUserId != newUserId) {
            // User has just signed in, link their anonymous budget
            val userName = currentUser.displayName ?: "My Budget"
            budgetRepository.linkAnonymousAccount(
                anonymousUserId = anonymousUserId,
                authenticatedUserId = newUserId,
                userName = userName
            )
        }
    }
}