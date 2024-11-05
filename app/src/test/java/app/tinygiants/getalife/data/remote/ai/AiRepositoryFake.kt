package app.tinygiants.getalife.data.remote.ai

import app.tinygiants.getalife.domain.repository.AiRepository

class AiRepositoryFake: AiRepository {

    var hasFailedToRetrieveEmoji = false

    override suspend fun generateEmojiBy(tag: String): Result<String?> {

        if (hasFailedToRetrieveEmoji) return Result.failure(exception = Exception("Could not retrieve emoji successfully"))

        return Result.success(value = "✅")
    }
}