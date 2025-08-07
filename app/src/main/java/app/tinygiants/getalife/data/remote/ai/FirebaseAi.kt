package app.tinygiants.getalife.data.remote.ai

import app.tinygiants.getalife.domain.repository.AiRepository
import com.google.firebase.ai.GenerativeModel
import javax.inject.Inject

class FirebaseAi @Inject constructor(private val generativeModel: GenerativeModel): AiRepository {

    override suspend fun generateEmojiBy(tag: String): Result<String?> {
        val prompt = "Return only one emoji (no text, no explanation) that best represents: $tag"

        return runCatching {
            val response = generativeModel.generateContent(prompt)
            response.text?.trim()
        }.recoverCatching { exception ->
            // Handle specific Firebase AI and App Check errors
            when {
                exception.message?.contains("App Check token", ignoreCase = true) == true -> {
                    // Log App Check token issue but continue with fallback
                    println("Firebase AI: App Check token error - continuing with placeholder token")
                    // Retry the request (Firebase will use placeholder token)
                    val response = generativeModel.generateContent(prompt)
                    response.text?.trim()
                }

                exception.message?.contains("firebaseappcheck.googleapis.com", ignoreCase = true) == true -> {
                    // Handle network connectivity issue
                    println("Firebase AI: Network connectivity issue with App Check service")
                    // Retry the request (Firebase will use placeholder token)
                    val response = generativeModel.generateContent(prompt)
                    response.text?.trim()
                }

                else -> throw exception // Re-throw other exceptions
            }
        }
    }
}