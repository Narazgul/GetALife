package app.tinygiants.getalife.domain.usecase.onboarding

import app.tinygiants.getalife.di.ApplicationScope
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.di.Io
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.onboarding.OnboardingAnswers
import app.tinygiants.getalife.domain.model.onboarding.Pet
import app.tinygiants.getalife.domain.model.onboarding.onbaordinganswers.Debt
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.usecase.OnboardingPrefsUseCase
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Clock

class CreateBudgetFromOnboardingAnswersUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
    private val categoryRepository: CategoryRepository,
    private val addEmojiToCategoryName: AddEmojiToCategoryNameUseCase,
    private val onboardingPrefsUseCase: OnboardingPrefsUseCase,
    @Io private val ioDispatcher: CoroutineDispatcher,
    @Default private val defaultDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    suspend operator fun invoke(answers: OnboardingAnswers) = withContext(ioDispatcher) {
        var groupPosition = 0
        var categoryPosition = 0
        val createdCategories = mutableListOf<Category>()

        suspend fun createGroupWithCategories(groupName: String, categoryNames: List<String>) {
            if (categoryNames.isEmpty()) return

            val group = withContext(defaultDispatcher) {
                Group(
                    id = kotlin.math.abs(Random.nextLong()),
                    name = groupName,
                    sumOfAvailableMoney = EmptyMoney(),
                    listPosition = groupPosition++,
                    isExpanded = true
                )
            }
            groupRepository.addGroup(group)

            categoryNames.forEach { categoryName ->
                val category = withContext(defaultDispatcher) {
                    val now = Clock.System.now()
                    Category(
                        id = kotlin.math.abs(Random.nextLong()),
                        groupId = group.id,
                        emoji = "",
                        name = categoryName,
                        budgetTarget = EmptyMoney(),
                        monthlyTargetAmount = null,
                        targetMonthsRemaining = null,
                        listPosition = categoryPosition++,
                        isInitialCategory = false,
                        updatedAt = now,
                        createdAt = now
                    )
                }
                categoryRepository.addCategory(category)
                createdCategories.add(category)
            }
        }

        // Create groups with categories first (fast operation)
        createHousingGroup(answers, ::createGroupWithCategories)
        createMobilityGroup(answers, ::createGroupWithCategories)
        createCommitmentsGroup(answers, ::createGroupWithCategories)
        createDailyExpensesGroup(answers, ::createGroupWithCategories)
        createLeisureGroup(answers, ::createGroupWithCategories)
        createSavingsGoalsGroup(answers, ::createGroupWithCategories)

        // Mark onboarding as completed immediately
        onboardingPrefsUseCase.markOnboardingCompleted()

        // Generate emojis in parallel in background scope (non-blocking)
        createdCategories.forEach { category ->
            applicationScope.async {
                addEmojiToCategoryName(category)
            }
        }
    }

    private suspend fun createHousingGroup(
        answers: OnboardingAnswers,
        createGroup: suspend (String, List<String>) -> Unit
    ) {
        val categoryNames = mutableListOf<String>()
        answers.housingSituation?.let { categoryNames.add(it.displayName) }
        if (answers.pets.any { it != Pet.NONE }) {
            categoryNames.add("Haustiere")
        }
        createGroup("Wohnen & Haushalt", categoryNames)
    }

    private suspend fun createMobilityGroup(
        answers: OnboardingAnswers,
        createGroup: suspend (String, List<String>) -> Unit
    ) {
        val categoryNames = answers.transportation.map { it.displayName }
        createGroup("Mobilität", categoryNames)
    }

    private suspend fun createCommitmentsGroup(
        answers: OnboardingAnswers,
        createGroup: suspend (String, List<String>) -> Unit
    ) {
        val categoryNames = mutableListOf<String>()
        categoryNames.addAll(answers.insurances.map { it.displayName })
        categoryNames.addAll(answers.debts.filter { it != Debt.NoDebt }.map { it.displayName })

        createGroup("Verpflichtungen", categoryNames)
    }

    private suspend fun createDailyExpensesGroup(
        answers: OnboardingAnswers,
        createGroup: suspend (String, List<String>) -> Unit
    ) {
        val categoryNames = mutableListOf<String>()
        categoryNames.addAll(answers.dailyExpenses.map { it.displayName })
        categoryNames.addAll(answers.healthExpenses.map { it.displayName })
        categoryNames.addAll(answers.shoppingExpenses.map { it.displayName })

        createGroup("Tägliche Ausgaben", categoryNames)
    }

    private suspend fun createLeisureGroup(
        answers: OnboardingAnswers,
        createGroup: suspend (String, List<String>) -> Unit
    ) {
        val categoryNames = answers.leisureExpenses.map { it.displayName }
        createGroup("Freizeit & Unterhaltung", categoryNames)
    }

    private suspend fun createSavingsGoalsGroup(
        answers: OnboardingAnswers,
        createGroup: suspend (String, List<String>) -> Unit
    ) {
        val categoryNames = answers.lifeGoals.map { it.displayName }
        createGroup("Sparziele", categoryNames)
    }
}