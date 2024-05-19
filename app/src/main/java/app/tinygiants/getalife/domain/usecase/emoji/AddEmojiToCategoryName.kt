package app.tinygiants.getalife.domain.usecase.emoji

import android.util.Log
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Gemini
import app.tinygiants.getalife.domain.repository.AiRepository
import app.tinygiants.getalife.domain.repository.BudgetRepository
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class AddEmojiToCategoryNameUseCase @Inject constructor(
    @Gemini private val aiRepository: AiRepository,
    private val repository: BudgetRepository) {

    suspend operator fun invoke(categoryEntity: CategoryEntity) {

        val emojiResponse = aiRepository.generateEmojiBy(tag = categoryEntity.name)

        emojiResponse.onFailure { exception ->
            Firebase.crashlytics.recordException(exception)
            Log.e("Emoji", "Failure: ${exception.message}")

            repository.updateCategory(categoryEntity.copy(emoji = "💸"))
        }

        emojiResponse.onSuccess { emojis ->
            if (emojis.isNullOrBlank()) return@onSuccess

            repository.updateCategory(categoryEntity.copy(emoji = emojis))
        }
    }
}