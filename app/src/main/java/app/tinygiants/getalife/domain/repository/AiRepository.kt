package app.tinygiants.getalife.domain.repository

interface AiRepository {

    suspend fun generateEmojiBy(tag: String): Result<String?>

    /**
     * Generate a suggestion for a new category based on transaction details
     */
    suspend fun generateCategorySuggestion(
        transactionPartner: String,
        description: String,
        amount: String,
        existingCategories: List<String>,
        existingGroups: List<String>
    ): Result<String>

    /**
     * Calculate semantic similarity between two text strings
     * Returns a value between 0.0 and 1.0
     */
    suspend fun calculateSemanticSimilarity(
        text1: String,
        text2: String
    ): Result<Float>

    /**
     * Generate financial insights based on spending patterns
     */
    suspend fun generateFinancialInsights(
        spendingPattern: String,
        userContext: String
    ): Result<String>
}