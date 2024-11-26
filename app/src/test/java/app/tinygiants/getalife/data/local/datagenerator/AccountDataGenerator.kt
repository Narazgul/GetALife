package app.tinygiants.getalife.data.local.datagenerator

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import kotlinx.datetime.Instant

val accounts = listOf(
    cashAccount(),
    checkingAccount(),
    secondCheckingAccount(),
    savingsAccount(),
    creditCardAccount(),
    mortgageAccount(),
    loanAccount(),
    depotAccount()
)

fun cashAccount() = AccountEntity(
    id = 1L,
    name = "Cash Account",
    balance = 500.00,
    type = AccountType.Cash,
    listPosition = 0,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun checkingAccount() = AccountEntity(
    id = 2L,
    name = "Checking Account",
    balance = 1200.50,
    type = AccountType.Checking,
    listPosition = 1,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun secondCheckingAccount() = AccountEntity(
    id = 3L,
    name = "Second Checking Account",
    balance = -214.83,
    type = AccountType.Checking,
    listPosition = 2,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun savingsAccount() = AccountEntity(
    id = 4L,
    name = "Savings Account",
    balance = 3500.00,
    type = AccountType.Savings,
    listPosition = 3,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun creditCardAccount() = AccountEntity(
    id = 5L,
    name = "Credit Card Account",
    balance = -500.75,
    type = AccountType.CreditCard,
    listPosition = 4,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun mortgageAccount() = AccountEntity(
    id = 6L,
    name = "Mortgage Account",
    balance = -150000.00,
    type = AccountType.Mortgage,
    listPosition = 5,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun loanAccount() = AccountEntity(
    id = 7L,
    name = "Loan Account",
    balance = -20000.00,
    type = AccountType.Loan,
    listPosition = 6,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun depotAccount() = AccountEntity(
    id = 8L,
    name = "Depot Account",
    balance = 25000.00,
    type = AccountType.Depot,
    listPosition = 7,
    updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
    createdAt = Instant.parse("2024-01-01T12:00:00Z")
)

fun AccountEntity.toAccount() = Account(
    id = id,
    name = name,
    balance = Money(balance),
    type = type,
    listPosition = listPosition,
    updatedAt = updatedAt,
    createdAt = createdAt,
)