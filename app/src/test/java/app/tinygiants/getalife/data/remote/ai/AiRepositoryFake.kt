package app.tinygiants.getalife.data.remote.ai

import app.tinygiants.getalife.domain.repository.AiRepository

class AiRepositoryFake: AiRepository {

    override suspend fun generateEmojiBy(tag: String): Result<String?> {
        return Result.success("🏷️")
    }

    override suspend fun generateFinancialInsights(
        spendingPattern: String,
        userContext: String
    ): Result<String> {
        return Result.success("Test financial insights")
    }
}