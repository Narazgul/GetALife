package app.tinygiants.getalife.domain.usecase.categorization

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TargetType
import app.tinygiants.getalife.domain.model.categorization.BulkCategorizationResult
import app.tinygiants.getalife.domain.model.categorization.TransactionGroup
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.FeatureFlagUseCase
import app.tinygiants.getalife.domain.usecase.budget.GetCurrentBudgetUseCase
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import javax.inject.Inject
import kotlin.time.Instant

/**
 * Use case for bulk categorization of transactions.
 *
 * Features:
 * - Groups similar uncategorized transactions
 * - Applies smart categorization suggestions to entire groups
 * - Supports batch operations for efficiency
 * - Provides impact analysis before applying changes
 */
class BulkCategorizationUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val getCurrentBudgetUseCase: GetCurrentBudgetUseCase,
    private val smartCategorizerUseCase: SmartTransactionCategorizerUseCase,
    private val similarityCalculator: TransactionSimilarityCalculator,
    private val featureFlagUseCase: FeatureFlagUseCase
) {

    /**
     * Groups uncategorized transactions by similarity and provides categorization suggestions
     *
     * @param minGroupSize Minimum number of transactions required to form a group
     * @param similarityThreshold Minimum similarity score (0.0-1.0) for grouping
     * @return List of transaction groups with categorization suggestions
     */
    suspend fun getUncategorizedTransactionGroups(
        minGroupSize: Int = 2,
        similarityThreshold: Float = 0.75f
    ): List<TransactionGroup> {
        val config = featureFlagUseCase.getSmartCategorizationConfig().first()
        if (!config.isEnabled) {
            return emptyList()
        }

        val uncategorizedTransactions = transactionRepository.getUncategorizedTransactions()

        if (uncategorizedTransactions.isEmpty()) {
            return emptyList()
        }

        return groupTransactionsBySimilarity(
            transactions = uncategorizedTransactions,
            minGroupSize = minGroupSize,
            similarityThreshold = similarityThreshold
        )
    }

    /**
     * Applies categorization to a group of transactions
     *
     * @param group The transaction group to categorize
     * @param categoryId The category ID to apply, or null to create a new category
     * @param newCategoryName Name for new category if categoryId is null
     * @param newCategoryGroupId Group ID for new category if creating one
     * @return Result of the bulk categorization operation
     */
    suspend fun categorizeBulkTransactions(
        group: TransactionGroup,
        categoryId: Long? = null,
        newCategoryName: String? = null,
        newCategoryGroupId: Long? = null
    ): BulkCategorizationResult {
        val config = featureFlagUseCase.getSmartCategorizationConfig().first()
        if (!config.isEnabled) {
            return BulkCategorizationResult.Error("Smart categorization is disabled")
        }

        return try {
            val category = if (categoryId != null) {
                // Use existing category
                categoryRepository.getAllCategories()
                    .find { it.id == categoryId }
                    ?: return BulkCategorizationResult.Error("Category not found")
            } else if (newCategoryName != null && newCategoryGroupId != null) {
                // Create new category
                val newCategory = Category(
                    id = 0, // Will be assigned by database
                    groupId = newCategoryGroupId,
                    emoji = group.suggestedCategory?.emoji ?: "🏷️",
                    name = newCategoryName,
                    budgetTarget = Money(0.0),
                    monthlyTargetAmount = null,
                    targetMonthsRemaining = null,
                    targetType = TargetType.NONE,
                    targetAmount = null,
                    targetDate = null,
                    isRepeating = false,
                    listPosition = 0,
                    isInitialCategory = false,
                    linkedAccountId = null,
                    updatedAt = Clock.System.now(),
                    createdAt = Clock.System.now()
                )
                categoryRepository.addCategory(newCategory)

                // Get the newly created category with its ID
                categoryRepository.getAllCategories()
                    .find { it.name == newCategoryName && it.groupId == newCategoryGroupId }
                    ?: return BulkCategorizationResult.Error("Failed to create new category")
            } else {
                return BulkCategorizationResult.Error("Either categoryId or new category details must be provided")
            }

            // Apply categorization to all transactions in the group
            var successCount = 0
            var failureCount = 0
            val errors = mutableListOf<String>()

            for (transaction in group.transactions) {
                try {
                    val updatedTransaction = transaction.copy(category = category)
                    transactionRepository.updateTransaction(updatedTransaction)
                    successCount++
                } catch (e: Exception) {
                    failureCount++
                    errors.add("Transaction ${transaction.id}: ${e.message}")
                }
            }

            if (successCount > 0) {
                BulkCategorizationResult.Success(
                    processedCount = successCount,
                    category = category,
                    totalAmount = group.totalAmount,
                    errors = if (errors.isNotEmpty()) errors else null
                )
            } else {
                BulkCategorizationResult.Error("Failed to categorize any transactions: ${errors.joinToString(", ")}")
            }

        } catch (e: Exception) {
            BulkCategorizationResult.Error("Bulk categorization failed: ${e.message}")
        }
    }

    /**
     * Groups transactions by similarity using clustering algorithm
     */
    private suspend fun groupTransactionsBySimilarity(
        transactions: List<Transaction>,
        minGroupSize: Int,
        similarityThreshold: Float
    ): List<TransactionGroup> {
        val groups = mutableListOf<TransactionGroup>()
        val processedTransactions = mutableSetOf<Long>()

        // Simple clustering: for each unprocessed transaction, find all similar ones
        for (transaction in transactions) {
            if (transaction.id in processedTransactions) continue

            val similarTransactions = mutableListOf<Transaction>()
            similarTransactions.add(transaction)
            processedTransactions.add(transaction.id)

            // Find similar transactions
            for (otherTransaction in transactions) {
                if (otherTransaction.id in processedTransactions) continue

                val similarity = similarityCalculator.calculateSimilarity(transaction, otherTransaction)
                if (similarity >= similarityThreshold) {
                    similarTransactions.add(otherTransaction)
                    processedTransactions.add(otherTransaction.id)
                }
            }

            // Only create group if it meets minimum size requirement
            if (similarTransactions.size >= minGroupSize) {
                val group = createTransactionGroup(similarTransactions)
                groups.add(group)
            }
        }

        // Sort groups by total amount (descending) for better UX
        return groups.sortedByDescending { it.totalAmount.asDouble() }
    }

    /**
     * Creates a TransactionGroup from a list of similar transactions
     */
    private suspend fun createTransactionGroup(transactions: List<Transaction>): TransactionGroup {
        // Get a representative transaction (usually the first one)
        val representative = transactions.first()

        // Calculate total amount
        val totalAmount = Money(transactions.sumOf { it.amount.asDouble() })

        // Generate smart categorization suggestion based on the representative transaction
        val suggestion = try {
            smartCategorizerUseCase(
                transactionPartner = representative.transactionPartner,
                description = representative.description,
                amount = representative.amount
            )
        } catch (e: Exception) {
            null
        }

        return TransactionGroup(
            id = "${representative.transactionPartner}_${transactions.size}".hashCode().toLong(),
            transactions = transactions,
            groupName = generateGroupName(transactions),
            totalAmount = totalAmount,
            transactionCount = transactions.size,
            suggestedCategory = suggestion?.existingCategoryMatch?.let { match ->
                // Convert CategoryMatch to Category if needed
                categoryRepository.getAllCategories().find { it.id == match.categoryId }
            },
            confidence = suggestion?.existingCategoryMatch?.confidence ?: 0.0f,
            dateRange = transactions.minOfOrNull { it.dateOfTransaction } to transactions.maxOfOrNull { it.dateOfTransaction }
        )
    }

    /**
     * Generates a descriptive name for a transaction group
     */
    private fun generateGroupName(transactions: List<Transaction>): String {
        val representative = transactions.first()
        val count = transactions.size

        return when {
            representative.transactionPartner.isNotBlank() ->
                "${representative.transactionPartner} ($count Transaktionen)"

            representative.description.isNotBlank() ->
                "${representative.description} ($count Transaktionen)"

            else -> "Ähnliche Transaktionen ($count)"
        }
    }
}