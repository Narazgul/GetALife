package app.tinygiants.getalife.data.remote.ai

import app.tinygiants.getalife.domain.repository.AiRepository
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class FirebaseAi @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val crashlytics: FirebaseCrashlytics
) : AiRepository {

    override suspend fun generateEmojiBy(tag: String): Result<String?> {
        val prompt = "Return only one emoji (no text, no explanation) that best represents: $tag"

        return runCatching {
            crashlytics.log("FirebaseAI: Generating emoji for tag: $tag")
            val response = generativeModel.generateContent(prompt)
            val result = response.text?.trim()
            crashlytics.log("FirebaseAI: Successfully generated emoji for tag: $tag")
            result
        }.recoverCatching { exception ->
            // Log error details to Crashlytics
            crashlytics.log("FirebaseAI: Error occurred for tag: $tag")
            crashlytics.setCustomKey("ai_tag", tag)
            crashlytics.setCustomKey("ai_prompt", prompt)
            crashlytics.setCustomKey("error_message", exception.message ?: "Unknown error")

            // Handle specific Firebase AI and App Check errors
            when {
                exception.message?.contains("App Check token", ignoreCase = true) == true -> {
                    crashlytics.log("FirebaseAI: App Check token error - retrying with placeholder token")
                    crashlytics.recordException(
                        Exception("Firebase AI App Check Token Error for tag: $tag", exception)
                    )

                    // Retry the request (Firebase will use placeholder token)
                    val response = generativeModel.generateContent(prompt)
                    val result = response.text?.trim()
                    crashlytics.log("FirebaseAI: Retry successful after App Check error")
                    result
                }

                exception.message?.contains("firebaseappcheck.googleapis.com", ignoreCase = true) == true -> {
                    crashlytics.log("FirebaseAI: Network connectivity issue with App Check service - retrying")
                    crashlytics.recordException(
                        Exception("Firebase AI Network Connectivity Error for tag: $tag", exception)
                    )

                    // Retry the request (Firebase will use placeholder token)
                    val response = generativeModel.generateContent(prompt)
                    val result = response.text?.trim()
                    crashlytics.log("FirebaseAI: Retry successful after network error")
                    result
                }

                else -> {
                    // Log all other exceptions to Crashlytics
                    crashlytics.log("FirebaseAI: Unhandled error - reporting to Crashlytics")
                    crashlytics.recordException(
                        Exception("Firebase AI Unhandled Error for tag: $tag", exception)
                    )
                    throw exception // Re-throw other exceptions
                }
            }
        }
    }
}