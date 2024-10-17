package app.tinygiants.getalife.data.local.datagenerator

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.domain.model.AccountType
import kotlinx.datetime.Instant

val accounts = listOf(
    cashAccountEntity(),
    checkingAccountEntity(),
    savingsAccountEntity(),
    creditCardAccountEntity(),
    mortgageAccountEntity(),
    loanAccountEntity(),
    depotAccountEntity()
)

fun cashAccountEntity() = AccountEntity(
    id = 1L,
    name = "Cash Account",
    balance = 500.00,
    type = AccountType.Cash,
    listPosition = 0,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun checkingAccountEntity() = AccountEntity(
    id = 2L,
    name = "Checking Account",
    balance = 1200.50,
    type = AccountType.Checking,
    listPosition = 1,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun savingsAccountEntity() = AccountEntity(
    id = 3L,
    name = "Savings Account",
    balance = 3500.00,
    type = AccountType.Savings,
    listPosition = 2,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun creditCardAccountEntity() = AccountEntity(
    id = 4L,
    name = "Credit Card Account",
    balance = -500.75,
    type = AccountType.CreditCard,
    listPosition = 3,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun mortgageAccountEntity() = AccountEntity(
    id = 5L,
    name = "Mortgage Account",
    balance = -150000.00,
    type = AccountType.Mortgage,
    listPosition = 4,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun loanAccountEntity() = AccountEntity(
    id = 6L,
    name = "Loan Account",
    balance = -20000.00,
    type = AccountType.Loan,
    listPosition = 5,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun depotAccountEntity() = AccountEntity(
    id = 7L,
    name = "Depot Account",
    balance = 25000.00,
    type = AccountType.Depot,
    listPosition = 6,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)