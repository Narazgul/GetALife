package app.tinygiants.getalife.data.local.datagenerator

import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.domain.model.TransactionDirection
import kotlinx.datetime.Instant

fun transactionEntities(): List<TransactionEntity> {
    return listOf(
        // Januar
        TransactionEntity(
            accountId = 1L,
            categoryId = 2L,
            amount = -50.0,
            transactionPartner = "Aldi",
            transactionDirection = TransactionDirection.Outflow,
            description = "Groceries",
            timestamp = Instant.parse("2024-01-05T10:30:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 4L,
            amount = 2000.0,
            transactionPartner = "TechCorp Ltd.",
            transactionDirection = TransactionDirection.Inflow,
            description = "Salary",
            timestamp = Instant.parse("2024-01-01T12:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 5L,
            amount = -800.0,
            transactionPartner = "Landlord",
            transactionDirection = TransactionDirection.Outflow,
            description = "Rent",
            timestamp = Instant.parse("2024-01-03T09:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 3L,
            amount = -100.0,
            transactionPartner = "E.ON",
            transactionDirection = TransactionDirection.Outflow,
            description = "Electricity bill",
            timestamp = Instant.parse("2024-01-15T08:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 6L,
            amount = -12.0,
            transactionPartner = "Netflix",
            transactionDirection = TransactionDirection.Outflow,
            description = "Netflix subscription",
            timestamp = Instant.parse("2024-01-05T18:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 2L,
            amount = -30.0,
            transactionPartner = "Lidl",
            transactionDirection = TransactionDirection.Outflow,
            description = "Groceries",
            timestamp = Instant.parse("2024-01-20T17:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 7L,
            amount = -50.0,
            transactionPartner = "Gym Membership",
            transactionDirection = TransactionDirection.Outflow,
            description = "Fitness Studio",
            timestamp = Instant.parse("2024-01-10T12:00:00Z")
        ),
        // Februar
        TransactionEntity(
            accountId = 1L,
            categoryId = 4L,
            amount = 2000.0,
            transactionPartner = "TechCorp Ltd.",
            transactionDirection = TransactionDirection.Inflow,
            description = "Salary",
            timestamp = Instant.parse("2024-02-01T12:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 5L,
            amount = -800.0,
            transactionPartner = "Landlord",
            transactionDirection = TransactionDirection.Outflow,
            description = "Rent",
            timestamp = Instant.parse("2024-02-03T09:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 3L,
            amount = -100.0,
            transactionPartner = "E.ON",
            transactionDirection = TransactionDirection.Outflow,
            description = "Electricity bill",
            timestamp = Instant.parse("2024-02-15T08:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 2L,
            amount = -45.0,
            transactionPartner = "Tesco",
            transactionDirection = TransactionDirection.Outflow,
            description = "Groceries",
            timestamp = Instant.parse("2024-02-10T11:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 6L,
            amount = -12.0,
            transactionPartner = "Netflix",
            transactionDirection = TransactionDirection.Outflow,
            description = "Netflix subscription",
            timestamp = Instant.parse("2024-02-05T18:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 7L,
            amount = -50.0,
            transactionPartner = "Gym Membership",
            transactionDirection = TransactionDirection.Outflow,
            description = "Fitness Studio",
            timestamp = Instant.parse("2024-02-10T12:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 2L,
            amount = -25.0,
            transactionPartner = "Rewe",
            transactionDirection = TransactionDirection.Outflow,
            description = "Groceries",
            timestamp = Instant.parse("2024-02-20T15:30:00Z")
        ),
        // März
        TransactionEntity(
            accountId = 1L,
            categoryId = 4L,
            amount = 2000.0,
            transactionPartner = "TechCorp Ltd.",
            transactionDirection = TransactionDirection.Inflow,
            description = "Salary",
            timestamp = Instant.parse("2024-03-01T12:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 5L,
            amount = -800.0,
            transactionPartner = "Landlord",
            transactionDirection = TransactionDirection.Outflow,
            description = "Rent",
            timestamp = Instant.parse("2024-03-03T09:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 3L,
            amount = -100.0,
            transactionPartner = "E.ON",
            transactionDirection = TransactionDirection.Outflow,
            description = "Electricity bill",
            timestamp = Instant.parse("2024-03-15T08:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 2L,
            amount = -40.0,
            transactionPartner = "Aldi",
            transactionDirection = TransactionDirection.Outflow,
            description = "Groceries",
            timestamp = Instant.parse("2024-03-05T10:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 6L,
            amount = -12.0,
            transactionPartner = "Netflix",
            transactionDirection = TransactionDirection.Outflow,
            description = "Netflix subscription",
            timestamp = Instant.parse("2024-03-05T18:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 7L,
            amount = -50.0,
            transactionPartner = "Gym Membership",
            transactionDirection = TransactionDirection.Outflow,
            description = "Fitness Studio",
            timestamp = Instant.parse("2024-03-10T12:00:00Z")
        ),
        TransactionEntity(
            accountId = 1L,
            categoryId = 2L,
            amount = -35.0,
            transactionPartner = "Lidl",
            transactionDirection = TransactionDirection.Outflow,
            description = "Groceries",
            timestamp = Instant.parse("2024-03-20T14:00:00Z")
        )
    )
}