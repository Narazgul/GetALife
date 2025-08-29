package app.tinygiants.getalife.domain.repository

interface AiRepository {

    suspend fun generateEmojiBy(tag: String): Result<String?>

    /**
     * Generate financial insights based on spending patterns
     */
    suspend fun generateFinancialInsights(
        spendingPattern: String,
        userContext: String
    ): Result<String>
}