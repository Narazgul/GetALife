package app.tinygiants.getalife.domain.usecase.emoji

import android.util.Log
import app.tinygiants.getalife.di.Vertex
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.AiRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class AddEmojiToCategoryNameUseCase @Inject constructor(
    @Vertex private val aiRepository: AiRepository,
    private val repository: CategoryRepository) {

    suspend operator fun invoke(category: Category) {

        val emojiResponse = aiRepository.generateEmojiBy(tag = category.name)

        emojiResponse.onFailure { exception ->
            Firebase.crashlytics.recordException(exception)
            Log.e("Emoji", "Emoji: ${exception.localizedMessage} ${exception.cause}")

            repository.updateCategory(category.copy(emoji = "💸"))
        }

        emojiResponse.onSuccess { emojis ->
            if (emojis.isNullOrBlank()) return@onSuccess

            repository.updateCategory(category.copy(emoji = emojis))
        }
    }
}