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
            val response = generativeModel.generateContent(prompt)
            val result = response.text?.trim()
            result
        }.recoverCatching { exception ->
            crashlytics.recordException(exception)
            null
        }
    }
}