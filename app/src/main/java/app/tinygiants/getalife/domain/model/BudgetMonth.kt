package app.tinygiants.getalife.domain.model

import kotlinx.datetime.YearMonth

data class BudgetMonth(
    val yearMonth: YearMonth,
    val totalAssignableMoney: Money,
    val totalAssignedMoney: Money,
    val groups: Map<Group, List<CategoryMonthlyStatus>>
)
