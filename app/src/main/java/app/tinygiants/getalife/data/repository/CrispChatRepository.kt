package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.domain.repository.SupportChatRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CrispChatRepository @Inject constructor() : SupportChatRepository {

    override fun isSupportChatEnabled(): Flow<Boolean> = flow {
        isChatForUserEnabled()
            .collect { isChatEnabled ->
                emit(isChatEnabled)
            }
    }

    private fun isChatForUserEnabled(): Flow<Boolean> = callbackFlow {

        val userDocumentReference = Firebase.auth.currentUser?.let { firebaseUser ->
            Firebase.firestore
                .collection("test")
                .document(firebaseUser.uid)
        }

        val registration = userDocumentReference?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Firebase.crashlytics.recordException(error)
                trySend(false)
            }

            if (snapshot != null && snapshot.exists()) {
                val isChatEnabled = snapshot.data?.getValue("isChatEnabled") as? Boolean
                if (isChatEnabled != null) trySend(isChatEnabled)
            }
        }

        awaitClose { registration?.remove() }
    }
}