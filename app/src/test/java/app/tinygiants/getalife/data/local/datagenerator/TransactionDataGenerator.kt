package app.tinygiants.getalife.data.local.datagenerator

import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import kotlinx.datetime.Instant

// Account IDs
const val CASH_ACCOUNT: Long = 1L
const val BANK_ACCOUNT_ONE: Long = 2L
const val BANK_ACCOUNT_TWO: Long = 3L
const val SAVINGS_ACCOUNT: Long = 4L
const val CREDIT_CARD_ACCOUNT: Long = 5L
const val MORTGAGE_ACCOUNT: Long = 6L
const val LOAN_ACCOUNT: Long = 7L
const val DEPOT_ACCOUNT: Long = 8L

// Category IDs
const val RENT: Long = 1L
const val STUDENT_LOAN = 2L
const val INSURANCE: Long = 3L
const val ELECTRICITY_BILL: Long = 4L
const val GROCERIES: Long = 5L
const val TRANSPORTATION: Long = 6L
const val FITNESS: Long = 9L
const val SUBSCRIPTIONS: Long = 17L

val transactions = listOf(

    // January
    aldiGroceriesJanuary(),
    techCorpSalaryJanuary(),
    landlordRentJanuary(),
    eonElectricityJanuary(),
    netflixJanuary(),
    lidlGroceriesJanuary(),
    gymMembershipJanuary(),

    // February
    techCorpSalaryFebruary(),
    landlordRentFebruary(),
    eonElectricityFebruary(),
    sevenElevenGroceriesFebruary(),
    netflixFebruary(),
    gymMembershipFebruary(),
    walmartGroceriesFebruary(),

    // March
    techCorpSalaryMarch(),
    landlordRentMarch(),
    eonElectricityMarch(),
    aldiGroceriesMarch(),
    netflixMarch(),
    gymMembershipMarch(),
    lidlGroceriesMarch()
)

// region January

fun aldiGroceriesJanuary(): TransactionEntity {
    return TransactionEntity(
        id = 1L,
        accountId = CASH_ACCOUNT,
        categoryId = GROCERIES,
        amount = -50.0,
        transactionPartner = "Aldi",
        transactionDirection = TransactionDirection.Outflow,
        description = "Groceries",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-01-05T10:30:00Z")
    )
}

fun techCorpSalaryJanuary(): TransactionEntity {
    return TransactionEntity(
        id = 2L,
        accountId = BANK_ACCOUNT_ONE,
        categoryId = null,
        amount = 2000.0,
        transactionPartner = "TechCorp Ltd.",
        transactionDirection = TransactionDirection.Inflow,
        description = "Salary",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun landlordRentJanuary(): TransactionEntity {
    return TransactionEntity(
        id = 3L,
        accountId = CASH_ACCOUNT,
        categoryId = RENT,
        amount = -800.0,
        transactionPartner = "Landlord",
        transactionDirection = TransactionDirection.Outflow,
        description = "Rent",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-01-03T09:00:00Z")
    )
}

fun eonElectricityJanuary(): TransactionEntity {
    return TransactionEntity(
        id = 4L,
        accountId = CASH_ACCOUNT,
        categoryId = ELECTRICITY_BILL,
        amount = -100.0,
        transactionPartner = "E.ON",
        transactionDirection = TransactionDirection.Outflow,
        description = "Electricity bill",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-01-15T08:00:00Z")
    )
}

fun netflixJanuary(): TransactionEntity {
    return TransactionEntity(
        id = 5L,
        accountId = CASH_ACCOUNT,
        categoryId = SUBSCRIPTIONS,
        amount = -12.0,
        transactionPartner = "Netflix",
        transactionDirection = TransactionDirection.Outflow,
        description = "Netflix subscription",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-01-05T18:00:00Z")
    )
}

fun lidlGroceriesJanuary(): TransactionEntity {
    return TransactionEntity(
        id = 6L,
        accountId = CASH_ACCOUNT,
        categoryId = GROCERIES,
        amount = -30.0,
        transactionPartner = "Lidl",
        transactionDirection = TransactionDirection.Outflow,
        description = "Groceries",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-01-20T17:00:00Z")
    )
}

fun gymMembershipJanuary(): TransactionEntity {
    return TransactionEntity(
        id = 7L,
        accountId = CASH_ACCOUNT,
        categoryId = FITNESS,
        amount = -50.0,
        transactionPartner = "Gym Membership",
        transactionDirection = TransactionDirection.Outflow,
        description = "Fitness Studio",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-01-10T12:00:00Z")
    )
}

// endregion

// region February

fun techCorpSalaryFebruary(): TransactionEntity {
    return TransactionEntity(
        id = 8L,
        accountId = BANK_ACCOUNT_ONE,
        categoryId = null,
        amount = 2000.0,
        transactionPartner = "TechCorp Ltd.",
        transactionDirection = TransactionDirection.Inflow,
        description = "Salary",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-02-01T12:00:00Z")
    )
}

fun landlordRentFebruary(): TransactionEntity {
    return TransactionEntity(
        id = 9L,
        accountId = CASH_ACCOUNT,
        categoryId = RENT,
        amount = -800.0,
        transactionPartner = "Landlord",
        transactionDirection = TransactionDirection.Outflow,
        description = "Rent",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-02-03T09:00:00Z")
    )
}

fun eonElectricityFebruary(): TransactionEntity {
    return TransactionEntity(
        id = 10L,
        accountId = CASH_ACCOUNT,
        categoryId = ELECTRICITY_BILL,
        amount = -100.0,
        transactionPartner = "E.ON",
        transactionDirection = TransactionDirection.Outflow,
        description = "Electricity bill",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-02-15T08:00:00Z")
    )
}

fun sevenElevenGroceriesFebruary(): TransactionEntity {
    return TransactionEntity(
        id = 11L,
        accountId = CASH_ACCOUNT,
        categoryId = GROCERIES,
        amount = -45.0,
        transactionPartner = "7/11",
        transactionDirection = TransactionDirection.Outflow,
        description = "Groceries",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-02-10T11:00:00Z")
    )
}

fun netflixFebruary(): TransactionEntity {
    return TransactionEntity(
        id = 12L,
        accountId = CASH_ACCOUNT,
        categoryId = SUBSCRIPTIONS,
        amount = -12.0,
        transactionPartner = "Netflix",
        transactionDirection = TransactionDirection.Outflow,
        description = "Netflix subscription",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-02-05T18:00:00Z")
    )
}

fun gymMembershipFebruary(): TransactionEntity {
    return TransactionEntity(
        id = 13L,
        accountId = CASH_ACCOUNT,
        categoryId = FITNESS,
        amount = -50.0,
        transactionPartner = "Gym Membership",
        transactionDirection = TransactionDirection.Outflow,
        description = "Fitness Studio",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-02-10T12:00:00Z")
    )
}

fun walmartGroceriesFebruary(): TransactionEntity {
    return TransactionEntity(
        id = 14L,
        accountId = CASH_ACCOUNT,
        categoryId = GROCERIES,
        amount = -25.0,
        transactionPartner = "Walmart",
        transactionDirection = TransactionDirection.Outflow,
        description = "Groceries",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-02-20T15:30:00Z")
    )
}

// endregion

// region March

fun techCorpSalaryMarch(): TransactionEntity {
    return TransactionEntity(
        id = 15L,
        accountId = BANK_ACCOUNT_TWO,
        categoryId = null,
        amount = 2000.0,
        transactionPartner = "TechCorp Ltd.",
        transactionDirection = TransactionDirection.Inflow,
        description = "Salary",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-03-01T12:00:00Z")
    )
}

fun landlordRentMarch(): TransactionEntity {
    return TransactionEntity(
        id = 16L,
        accountId = CASH_ACCOUNT,
        categoryId = RENT,
        amount = -800.0,
        transactionPartner = "Landlord",
        transactionDirection = TransactionDirection.Outflow,
        description = "Rent",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-03-03T09:00:00Z")
    )
}

fun eonElectricityMarch(): TransactionEntity {
    return TransactionEntity(
        id = 17L,
        accountId = CASH_ACCOUNT,
        categoryId = ELECTRICITY_BILL,
        amount = -100.0,
        transactionPartner = "E.ON",
        transactionDirection = TransactionDirection.Outflow,
        description = "Electricity bill",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-03-15T08:00:00Z")
    )
}

fun aldiGroceriesMarch(): TransactionEntity {
    return TransactionEntity(
        id = 18L,
        accountId = CASH_ACCOUNT,
        categoryId = GROCERIES,
        amount = -40.0,
        transactionPartner = "Aldi",
        transactionDirection = TransactionDirection.Outflow,
        description = "Groceries",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-03-05T10:00:00Z")
    )
}

fun netflixMarch(): TransactionEntity {
    return TransactionEntity(
        id = 19L,
        accountId = CASH_ACCOUNT,
        categoryId = SUBSCRIPTIONS,
        amount = -12.0,
        transactionPartner = "Netflix",
        transactionDirection = TransactionDirection.Outflow,
        description = "Netflix subscription",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-03-05T18:00:00Z")
    )
}

fun gymMembershipMarch(): TransactionEntity {
    return TransactionEntity(
        id = 20L,
        accountId = CASH_ACCOUNT,
        categoryId = FITNESS,
        amount = -50.0,
        transactionPartner = "Gym Membership",
        transactionDirection = TransactionDirection.Outflow,
        description = "Fitness Studio",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-03-10T12:00:00Z")
    )
}

fun lidlGroceriesMarch(): TransactionEntity {
    return TransactionEntity(
        id = 21L,
        accountId = CASH_ACCOUNT,
        categoryId = GROCERIES,
        amount = -35.0,
        transactionPartner = "Lidl",
        transactionDirection = TransactionDirection.Outflow,
        description = "Groceries",
        updatedAt = Instant.parse("2024-01-05T10:30:00Z"),
        createdAt = Instant.parse("2024-03-20T14:00:00Z")
    )
}

fun TransactionEntity.toTransaction(account: Account? = null, category: Category? = null) = Transaction(
    id = id,
    amount = Money(amount),
    account = account,
    category = category,
    transactionPartner = transactionPartner,
    transactionDirection = transactionDirection,
    description = description,
    updatedAt = updatedAt,
    createdAt = createdAt
)

// endregion