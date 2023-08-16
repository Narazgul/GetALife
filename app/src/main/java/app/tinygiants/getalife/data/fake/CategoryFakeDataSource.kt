package app.tinygiants.getalife.data.fake

import app.tinygiants.getalife.di.Io
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.CategoryHeader
import app.tinygiants.getalife.util.toCurrencyFormattedString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryFakeDataSource @Inject constructor(
    @Io private val ioDispatcher: CoroutineDispatcher
) {
    private val fakeCategories = MutableStateFlow(
        mapOf(
            fixedCosts to listOf(rent, cellphone, gym, netflix),
            dailyLife to listOf(groceries, eatingOut, transportation),
            nonMonthly to listOf(gifts, footballClub, liabilityInsurance),
            goals to listOf(mauritius, frenchClass, sunglasses),
            qualityOfLife to listOf(hobbies, entertainment, healthAndWellness)
        )
    )
    fun fetchFakeCategories(): StateFlow<Map<CategoryHeader, List<Category>>> = fakeCategories

    suspend fun toggleIsExpended(categoryId: Int) {
        withContext(ioDispatcher) {
            val updatedCategories = fakeCategories.value.map { (categoryHeader, categoryList) ->
                if (categoryHeader.id == categoryId) {
                    categoryHeader.copy(isExpanded = !categoryHeader.isExpanded) to categoryList
                }  else {
                    categoryHeader to categoryList
                }
            }.toMap()

            fakeCategories.value = updatedCategories
        }
    }
}

// region Fixed costs

val fixedCosts = CategoryHeader(
    id = 0,
    name = "Fixed costs"
)

val rent = Category(
    id = 10,
    name = "Rent",
    budgetTarget = 940.00,
    availableMoney = 940.00
)

val cellphone = Category(
    id = 11,
    name = "Cellphone",
    budgetTarget = 10.00,
    availableMoney = 10.00
)

val gym = Category(
    id = 12,
    name = "Gym",
    budgetTarget = 29.00,
    availableMoney = 10.00,
    optionalText = "${(29.00 - 10.00).toCurrencyFormattedString()} more needed by the 30th"
)

val netflix = Category(
    id = 13,
    name = "Netflix",
    budgetTarget = 17.00,
    availableMoney = 0.00
)

// endregion

// region Daily life

val dailyLife = CategoryHeader(
    id = 1,
    name = "Daily life"
)

val groceries = Category(
    id = 20,
    name = "Groceries",
    budgetTarget = 250.00,
    availableMoney = 300.00
)

val eatingOut = Category(
    id = 21,
    name = "Eating Out",
    budgetTarget = 100.00,
    availableMoney = 60.00
)

val transportation = Category(
    id = 22,
    name = "Transportation",
    budgetTarget = 49.00,
    availableMoney = 49.00
)

// endregion

// region Non-Monthly costs

val nonMonthly = CategoryHeader(
    id = 2,
    name = "Non-monthly costs"
)

val gifts = Category(
    id = 30,
    name = "Gifts",
    budgetTarget = 60.00,
    availableMoney = 30.00
)

val footballClub = Category(
    id = 31,
    name = "Football Club",
    budgetTarget = 30.00,
    availableMoney = 21.17
)

val liabilityInsurance = Category(
    id = 32,
    name = "Liability insurance",
    budgetTarget = 32.47,
    availableMoney = 25.11
)

// endregion

// region Goals

val goals = CategoryHeader(
    id = 3,
    name = "Goals"
)

val mauritius = Category(
    id = 40,
    name = "Mauritius",
    budgetTarget = 2000.00,
    availableMoney = 1115.00
)

val frenchClass = Category(
    id = 41,
    name = "French class",
    budgetTarget = 723.00,
    availableMoney = 210.00
)

val sunglasses = Category(
    id = 42,
    name = "Sunglasses",
    budgetTarget = 150.00,
    availableMoney = 30.00
)

// endregion

// region Quality of LIfe

val qualityOfLife = CategoryHeader(
    id = 4,
    name = "Quality of Life"
)

val hobbies = Category(
    id = 50,
    name = "Hobbies",
    budgetTarget = 80.00,
    availableMoney = 11.73
)

val entertainment = Category(
    id = 51,
    name = "Entertainment",
    budgetTarget = 40.00,
    availableMoney = 2.73
)

val healthAndWellness = Category(
    id = 52,
    name = "Health & Wellness",
    budgetTarget = 50.00,
    availableMoney = 47.19
)

// endregion

