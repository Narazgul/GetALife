package app.tinygiants.getalife.domain.usecase.emoji

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
    @FirebaseGemini private val aiRepository: AiRepository,
    private val repository: CategoryRepository) {

    suspend operator fun invoke(category: Category) {
        val emojiResponse = withTimeoutOrNull(timeout = 10.seconds) {
            try {
                aiRepository.generateEmojiBy(tag = category.name)
            } catch (e: Exception) {
                Result.failure(e)
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