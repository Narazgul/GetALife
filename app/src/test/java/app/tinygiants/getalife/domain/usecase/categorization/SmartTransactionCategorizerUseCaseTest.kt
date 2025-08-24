package app.tinygiants.getalife.domain.usecase.categorization

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TargetType
import app.tinygiants.getalife.domain.model.categorization.CategoryMatch
import app.tinygiants.getalife.domain.repository.AiRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.SmartCategorizationConfig
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.FeatureFlagUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Instant

class SmartTransactionCategorizerUseCaseTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var groupRepository: GroupRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var aiRepository: AiRepository
    private lateinit var featureFlagUseCase: FeatureFlagUseCase
    private lateinit var transactionSimilarityCalculator: TransactionSimilarityCalculator

    private lateinit var useCase: SmartTransactionCategorizerUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        categoryRepository = mockk()
        groupRepository = mockk()
        transactionRepository = mockk()
        aiRepository = mockk()
        featureFlagUseCase = mockk()
        transactionSimilarityCalculator = mockk()

        useCase = SmartTransactionCategorizerUseCase(
            categoryRepository = categoryRepository,
            groupRepository = groupRepository,
            transactionRepository = transactionRepository,
            aiRepository = aiRepository,
            featureFlagUseCase = featureFlagUseCase,
            transactionSimilarityCalculator = transactionSimilarityCalculator,
            dispatcher = testDispatcher
        )
    }

    @Test
    fun `returns empty result when feature is disabled`() = runTest(testDispatcher) {
        // Given
        val config = SmartCategorizationConfig(
            isEnabled = false,
            confidenceThreshold = 0.7f,
            maxSuggestions = 3,
            enableLearning = true,
            enableBulkCategorization = false
        )
        every { featureFlagUseCase.getSmartCategorizationConfig() } returns flowOf(config)

        // When
        val result = useCase("REWE", "Groceries shopping", Money(50.0))

        // Then
        assert(result.existingCategoryMatch == null)
        assert(result.newCategorySuggestion == null)

        coVerify(exactly = 0) { categoryRepository.getAllCategories() }
    }

    @Test
    fun `finds matching category with high confidence`() = runTest(testDispatcher) {
        // Given
        val config = SmartCategorizationConfig(
            isEnabled = true,
            confidenceThreshold = 0.7f,
            maxSuggestions = 3,
            enableLearning = true,
            enableBulkCategorization = false
        )

        val categories = listOf(
            createTestCategory(1L, "🛒", "Groceries"),
            createTestCategory(2L, "⛽", "Fuel")
        )

        val groups = listOf(
            createTestGroup(1L, "Living Expenses")
        )

        every { featureFlagUseCase.getSmartCategorizationConfig() } returns flowOf(config)
        coEvery { categoryRepository.getAllCategories() } returns categories
        coEvery { groupRepository.getAllGroups() } returns groups
        coEvery { transactionSimilarityCalculator.calculateSimilarity(any(), any(), "Groceries") } returns 0.9f
        coEvery { transactionSimilarityCalculator.calculateSimilarity(any(), any(), "Fuel") } returns 0.2f

        // When
        val result = useCase("REWE", "Weekly groceries", Money(45.0))

        // Then
        assert(result.existingCategoryMatch != null)
        assert(result.existingCategoryMatch?.categoryId == 1L)
        assert(result.existingCategoryMatch?.confidence == 0.9f)
        assert(result.newCategorySuggestion == null) // High confidence, no new suggestion needed
    }

    @Test
    fun `suggests new category when no good match found`() = runTest(testDispatcher) {
        // Given
        val config = SmartCategorizationConfig(
            isEnabled = true,
            confidenceThreshold = 0.7f,
            maxSuggestions = 3,
            enableLearning = true,
            enableBulkCategorization = false
        )

        val categories = listOf(
            createTestCategory(1L, "🛒", "Groceries")
        )

        val groups = listOf(
            createTestGroup(1L, "Living Expenses")
        )

        val aiResponse = """
        {
          "shouldCreateNew": true,
          "categoryName": "Gas Station",
          "emoji": "⛽",
          "groupName": "Living Expenses",
          "reasoning": "For fuel purchases"
        }
        """.trimIndent()

        every { featureFlagUseCase.getSmartCategorizationConfig() } returns flowOf(config)
        coEvery { categoryRepository.getAllCategories() } returns categories
        coEvery { groupRepository.getAllGroups() } returns groups
        coEvery { transactionSimilarityCalculator.calculateSimilarity(any(), any(), "Groceries") } returns 0.3f
        coEvery { aiRepository.generateCategorySuggestion(any(), any(), any(), any(), any()) } returns Result.success(aiResponse)

        // When
        val result = useCase("Shell", "Fuel purchase", Money(60.0))

        // Then
        assert(result.existingCategoryMatch?.confidence == 0.3f) // Low confidence match is returned
        assert(result.newCategorySuggestion != null)
        assert(result.newCategorySuggestion?.categoryName == "Gas Station")
        assert(result.newCategorySuggestion?.emoji == "⛽")
    }

    @Test
    fun `handles AI failure gracefully`() = runTest(testDispatcher) {
        // Given
        val config = SmartCategorizationConfig(
            isEnabled = true,
            confidenceThreshold = 0.7f,
            maxSuggestions = 3,
            enableLearning = true,
            enableBulkCategorization = false
        )

        val categories = listOf(createTestCategory(1L, "🛒", "Groceries"))
        val groups = listOf(createTestGroup(1L, "Living Expenses"))

        every { featureFlagUseCase.getSmartCategorizationConfig() } returns flowOf(config)
        coEvery { categoryRepository.getAllCategories() } returns categories
        coEvery { groupRepository.getAllGroups() } returns groups
        coEvery { transactionSimilarityCalculator.calculateSimilarity(any(), any(), any()) } returns 0.3f
        coEvery {
            aiRepository.generateCategorySuggestion(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Result.failure(Exception("AI failed"))

        // When
        val result = useCase("Unknown Merchant", "Some purchase", Money(25.0))

        // Then
        assert(result.existingCategoryMatch != null) // Low confidence match still returned
        assert(result.newCategorySuggestion == null) // AI failed, no suggestion
    }

    private fun createTestCategory(
        id: Long,
        emoji: String,
        name: String
    ) = Category(
        id = id,
        groupId = 1L,
        emoji = emoji,
        name = name,
        budgetTarget = Money(100.0),
        monthlyTargetAmount = null,
        targetMonthsRemaining = null,
        targetType = TargetType.NEEDED_FOR_SPENDING,
        targetAmount = null,
        targetDate = null,
        isRepeating = false,
        listPosition = 0,
        isInitialCategory = false,
        linkedAccountId = null,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST
    )

    private fun createTestGroup(id: Long, name: String) = Group(
        id = id,
        name = name,
        sumOfAvailableMoney = Money(500.0),
        listPosition = 0,
        isExpanded = true
    )
}