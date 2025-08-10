package app.tinygiants.getalife.domain.usecase.emoji

import app.tinygiants.getalife.di.ChatGPT
import app.tinygiants.getalife.di.FirebaseGemini
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.AiRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class AddEmojiToCategoryNameUseCase @Inject constructor(
    @FirebaseGemini private val firebaseAi: AiRepository,
    @ChatGPT private val chatGptAi: AiRepository,
    private val repository: CategoryRepository
) {

    suspend operator fun invoke(category: Category) {
        // Try Firebase AI first
        val emojiResponse = withTimeoutOrNull(timeout = 5.seconds) {
            try {
                firebaseAi.generateEmojiBy(tag = category.name)
            } catch (e: Exception) {
                // If Firebase AI fails, try ChatGPT
                try {
                    chatGptAi.generateEmojiBy(tag = category.name)
                } catch (chatGptException: Exception) {
                    Firebase.crashlytics.recordException(e) // Log original Firebase error
                    Firebase.crashlytics.recordException(chatGptException) // Log ChatGPT error
                    Result.failure(chatGptException)
                }
            }
        }

        if (emojiResponse == null) {
            setDefaultEmoji(category = category)
            return
        }

        emojiResponse.onFailure { exception ->
            Firebase.crashlytics.recordException(exception)
            setDefaultEmoji(category = category)
        }

        emojiResponse.onSuccess { emojis ->
            if (emojis.isNullOrBlank()) {
                setDefaultEmoji(category = category)
                return@onSuccess
            }

            repository.updateCategory(category.copy(emoji = emojis.trim()))
        }
    }

    private suspend fun setDefaultEmoji(category: Category) {
        repository.updateCategory(category.copy(emoji = "💸"))
    }
}