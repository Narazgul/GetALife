package app.tinygiants.getalife.data.remote.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

const val USERS_COLLECTION = "users"

const val IS_CHAT_ENABLED_ATTRIBUTE = "isChatEnabled"
const val FIREBASE_MESSAGING_TOKEN = "firebaseMessagingToken"

class RemoteFirebaseUsers @Inject constructor() {

    private val currentUser = Firebase.auth
    private val userDocument = currentUser.uid?.let { firebaseUserUid ->
        Firebase.firestore
            .collection(USERS_COLLECTION)
            .document(firebaseUserUid)
    }

    fun isChatEnabled(): Flow<Boolean> = callbackFlow {
        val registration = userDocument?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Firebase.crashlytics.recordException(error)
                trySend(false)
            }

            if (snapshot != null && snapshot.exists()) {
                val isChatEnabled = snapshot.data?.getValue(IS_CHAT_ENABLED_ATTRIBUTE) as? Boolean
                if (isChatEnabled != null) trySend(isChatEnabled)
            }
        }

        awaitClose { registration?.remove() }
    }

    fun updateNotificationToken(firebaseMessagingToken: String) {
        userDocument
            ?.update(FIREBASE_MESSAGING_TOKEN, firebaseMessagingToken)
            ?.addOnFailureListener { exception -> Firebase.crashlytics.recordException(exception) }
    }
}