package app.tinygiants.getalife.domain.usecase.emoji

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.domain.repository.AiRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class AddEmojiToCategoryNameUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val repository: CategoryRepository) {

    suspend operator fun invoke(categoryEntity: CategoryEntity) {

        val emojiResponse = aiRepository.generateEmojiBy(tag = categoryEntity.name)

        emojiResponse.onFailure { exception -> Firebase.crashlytics.recordException(exception) }
        emojiResponse.onSuccess { successfulResult ->
            val emojis = successfulResult.text
            if (emojis.isNullOrBlank()) return@onSuccess

            repository.updateCategory(categoryEntity.copy(emoji = emojis))
        }
    }
}