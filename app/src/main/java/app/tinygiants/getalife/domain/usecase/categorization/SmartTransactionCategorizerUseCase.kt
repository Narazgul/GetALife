package app.tinygiants.getalife.domain.usecase.categorization

import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.categorization.CategorizationResult
import app.tinygiants.getalife.domain.model.categorization.CategoryMatch
import app.tinygiants.getalife.domain.model.categorization.NewCategorySuggestion
import app.tinygiants.getalife.domain.repository.AiRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.SmartCategorizationConfig
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.FeatureFlagUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Main use case for smart transaction categorization.
 * Combines existing category matching with AI-powered new category suggestions.
 */
class SmartTransactionCategorizerUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val groupRepository: GroupRepository,
    private val transactionRepository: TransactionRepository,
    private val aiRepository: AiRepository,
    private val featureFlagUseCase: FeatureFlagUseCase,
    private val transactionSimilarityCalculator: TransactionSimilarityCalculator,
    private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        transactionPartner: String,
        description: String,
        amount: Money
    ): CategorizationResult {
        return withContext(dispatcher) {
            // Check if feature is enabled
            val config = featureFlagUseCase.getSmartCategorizationConfig().first()
            if (!config.isEnabled) {
                return@withContext CategorizationResult(null, null)
            }

            val userCategories = categoryRepository.getAllCategories()
            val userGroups = groupRepository.getAllGroups()

            // 1. Find best matching existing category
            val existingMatch = findBestMatchingCategory(
                transactionPartner,
                description,
                userCategories,
                config
            )

            // 2. Generate AI suggestion for new category if no good match found
            val aiSuggestion = if (existingMatch.confidence < config.confidenceThreshold) {
                generateNewCategorySuggestion(
                    transactionPartner,
                    description,
                    amount,
                    userCategories,
                    userGroups
                )
            } else null

            CategorizationResult(
                existingCategoryMatch = existingMatch.takeIf { it.confidence > config.confidenceThreshold * 0.5f },
                newCategorySuggestion = aiSuggestion
            )
        }
    }

    private suspend fun findBestMatchingCategory(
        partner: String,
        description: String,
        userCategories: List<app.tinygiants.getalife.domain.model.Category>,
        config: SmartCategorizationConfig
    ): CategoryMatch {
        if (userCategories.isEmpty()) {
            return CategoryMatch.noMatch()
        }

        // Calculate similarity for each category
        val matches = userCategories.map { category ->
            val confidence = transactionSimilarityCalculator.calculateSimilarity(
                partner = partner,
                description = description,
                categoryName = category.name
            )

            CategoryMatch(
                categoryId = category.id,
                categoryName = category.name,
                categoryEmoji = category.emoji,
                confidence = confidence,
                reasoning = "Based on transaction pattern analysis"
            )
        }.sortedByDescending { it.confidence }

        return matches.first()
    }

    private suspend fun generateNewCategorySuggestion(
        partner: String,
        description: String,
        amount: Money,
        existingCategories: List<app.tinygiants.getalife.domain.model.Category>,
        existingGroups: List<app.tinygiants.getalife.domain.model.Group>
    ): NewCategorySuggestion? {
        return try {
            val prompt = buildNewCategoryPrompt(partner, description, amount, existingCategories, existingGroups)
            val aiResponse = aiRepository.generateCategorySuggestion(
                transactionPartner = partner,
                description = description,
                amount = amount.formattedMoney,
                existingCategories = existingCategories.map { "${it.emoji} ${it.name}" },
                existingGroups = existingGroups.map { it.name }
            ).getOrNull()

            aiResponse?.let { parseNewCategoryResponse(it, existingGroups) }
        } catch (e: Exception) {
            null // Graceful fallback if AI fails
        }
    }

    private fun buildNewCategoryPrompt(
        partner: String,
        description: String,
        amount: Money,
        existingCategories: List<app.tinygiants.getalife.domain.model.Category>,
        existingGroups: List<app.tinygiants.getalife.domain.model.Group>
    ): String {
        return """
        Analysiere diese Transaktion und schlage eine neue Kategorie vor:
        
        Transaktion:
        - Partner: $partner
        - Beschreibung: $description
        - Betrag: ${amount.formattedMoney}
        
        Bestehende Kategorien des Nutzers:
        ${existingCategories.joinToString("\n") { "- ${it.emoji} ${it.name}" }}
        
        Verfügbare Gruppen:
        ${existingGroups.joinToString("\n") { "- ${it.name}" }}
        
        Falls keine bestehende Kategorie gut passt, schlage eine neue vor.
        
        Antwortformat (JSON):
        {
          "shouldCreateNew": true/false,
          "categoryName": "Vorgeschlagener Name",
          "emoji": "🛒",
          "groupName": "Passende Gruppe",
          "reasoning": "Warum diese Kategorie sinnvoll ist"
        }
        """.trimIndent()
    }

    private fun parseNewCategoryResponse(
        aiResponse: String,
        existingGroups: List<app.tinygiants.getalife.domain.model.Group>
    ): NewCategorySuggestion? {
        // Simple JSON parsing - in production, use a proper JSON library
        return try {
            // Extract values from JSON response (simplified)
            val categoryName = extractJsonValue(aiResponse, "categoryName") ?: return null
            val emoji = extractJsonValue(aiResponse, "emoji") ?: "📝"
            val groupName = extractJsonValue(aiResponse, "groupName") ?: return null
            val reasoning = extractJsonValue(aiResponse, "reasoning") ?: "AI suggestion"

            val group = existingGroups.find { it.name.equals(groupName, ignoreCase = true) }
                ?: existingGroups.firstOrNull()
                ?: return null

            NewCategorySuggestion(
                categoryName = categoryName,
                emoji = emoji,
                groupId = group.id,
                groupName = group.name,
                reasoning = reasoning
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun extractJsonValue(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }
}