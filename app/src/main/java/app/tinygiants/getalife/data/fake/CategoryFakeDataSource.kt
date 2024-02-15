//package app.tinygiants.getalife.data.fake
//
//import app.tinygiants.getalife.di.Io
//import app.tinygiants.getalife.domain.model.Category
//import app.tinygiants.getalife.domain.model.Header
//import app.tinygiants.getalife.domain.model.Money
//import kotlinx.coroutines.CoroutineDispatcher
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.withContext
//import javax.inject.Inject
//
//class CategoryFakeDataSource @Inject constructor(
//    @Io private val ioDispatcher: CoroutineDispatcher
//) {
//    private val fakeCategories = MutableStateFlow(
//        mapOf(
//            fixedCosts to listOf(rent, cellphone, gym, netflix),
//            dailyLife to listOf(groceries, eatingOut, transportation),
//            nonMonthly to listOf(gifts, footballClub, liabilityInsurance),
//            goals to listOf(mauritius, frenchClass, sunglasses),
//            qualityOfLife to listOf(hobbies, entertainment, healthAndWellness)
//        )
//    )
//
//    fun fetchFakeCategories(): StateFlow<Map<Header, List<Category>>> = fakeCategories
//
//    suspend fun toggleIsExpended(categoryId: Long) {
//        withContext(ioDispatcher) {
//            val updatedCategories = fakeCategories.value.map { (categoryHeader, categoryList) ->
//                if (categoryHeader.id == categoryId) {
//                    categoryHeader.copy(isExpanded = !categoryHeader.isExpanded) to categoryList
//                } else {
//                    categoryHeader to categoryList
//                }
//            }.toMap()
//
//            fakeCategories.value = updatedCategories
//        }
//    }
//}
//
//// region Fixed costs
//
//val fixedCosts = Header(
//    id = 1234567,
//    name = "Fixed costs",
//    sumOfAvailableMoney = Money(0.00),
//    isExpanded = false
//)
//
//val rent = Category(
//    name = "Rent",
//    headerId = 1234567,
//    budgetTarget = 940.00,
//    availableMoney = 940.00
//)
//
//val cellphone = Category(
//    name = "Cellphone",
//    headerId = 1234567,
//    budgetTarget = 10.00,
//    availableMoney = 10.00
//)
//
//val gym = Category(
//    name = "Gym",
//    headerId = 1234567,
//    budgetTarget = 29.00,
//    availableMoney = 10.00
//)
//
//val netflix = Category(
//    name = "Netflix",
//    headerId = 1234567,
//    budgetTarget = 17.00,
//    availableMoney = 0.00
//)
//
//// endregion
//
//// region Daily life
//
//val dailyLife = Header(
//    id = 2345678,
//    name = "Daily life"
//)
//
//val groceries = Category(
//    name = "Groceries",
//    headerId = 2345678,
//    budgetTarget = 250.00,
//    availableMoney = 300.00
//)
//
//val eatingOut = Category(
//    name = "Eating Out",
//    headerId = 2345678,
//    budgetTarget = 100.00,
//    availableMoney = 60.00
//)
//
//val transportation = Category(
//    name = "Transportation",
//    headerId = 2345678,
//    budgetTarget = 49.00,
//    availableMoney = 49.00
//)
//
//// endregion
//
//// region Non-Monthly costs
//
//val nonMonthly = Header(
//    id = 3456789,
//    name = "Non-monthly costs"
//)
//
//val gifts = Category(
//    name = "Gifts",
//    headerId = 3456789,
//    budgetTarget = 60.00,
//    availableMoney = 30.00
//)
//
//val footballClub = Category(
//    name = "Football Club",
//    headerId = 3456789,
//    budgetTarget = 30.00,
//    availableMoney = 21.17
//)
//
//val liabilityInsurance = Category(
//    name = "Liability insurance",
//    headerId = 3456789,
//    budgetTarget = 32.47,
//    availableMoney = 25.11
//)
//
//// endregion
//
//// region Goals
//
//val goals = Header(
//    id = 4567890,
//    name = "Goals"
//)
//
//val mauritius = Category(
//    name = "Mauritius",
//    headerId = 4567890,
//    budgetTarget = 2000.00,
//    availableMoney = 1115.00
//)
//
//val frenchClass = Category(
//    name = "French class",
//    headerId = 4567890,
//    budgetTarget = 723.00,
//    availableMoney = 210.00
//)
//
//val sunglasses = Category(
//    name = "Sunglasses",
//    headerId = 4567890,
//    budgetTarget = 150.00,
//    availableMoney = 30.00
//)
//
//// endregion
//
//// region Quality of LIfe
//
//val qualityOfLife = Header(
//    id = 5678901,
//    name = "Quality of Life"
//)
//
//val hobbies = Category(
//    name = "Hobbies",
//    headerId = 5678901,
//    budgetTarget = 80.00,
//    availableMoney = 11.73
//)
//
//val entertainment = Category(
//    name = "Entertainment",
//    headerId = 5678901,
//    budgetTarget = 40.00,
//    availableMoney = 2.73
//)
//
//val healthAndWellness = Category(
//    name = "Health & Wellness",
//    headerId = 5678901,
//    budgetTarget = 50.00,
//    availableMoney = 47.19
//)
//
//// endregion