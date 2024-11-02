package app.tinygiants.getalife.data.local.datagenerator

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.domain.model.BudgetPurpose
import kotlinx.datetime.Instant

val categoryEntities = listOf(
    rentCategoryEntity(),
    studentLoanRepaymentCategoryEntity(),
    insuranceCategoryEntity(),
    healthInsuranceCategoryEntity(),
    groceriesCategoryEntity(),
    transportCategoryEntity(),
    clothingCategoryEntity(),
    eatingOutCategoryEntity(),
    petsCategoryEntity(),
    savingsCategoryEntity(),
    vacationCategoryEntity(),
    hobbiesCategoryEntity(),
    educationCategoryEntity(),
    investmentsCategoryEntity(),
    debtsCategoryEntity(),
    emergencyFundCategoryEntity(),
    healthcareCategoryEntity(),
    gymCategoryEntity(),
    dentistCategoryEntity(),
    personalCareCategoryEntity()
)

fun rentCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 1L,
        groupId = 0L,
        emoji = "🏠",
        name = "Miete",
        budgetTarget = 1200.0,
        budgetPurpose = BudgetPurpose.Spending,
        assignedMoney = 1200.0,
        availableMoney = 1300.0,
        optionalText = "",
        listPosition = 0,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun studentLoanRepaymentCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 2L,
        groupId = 0L,
        emoji = "🎓",
        name = "Studienkredittilgung",
        budgetTarget = 300.0,
        budgetPurpose = BudgetPurpose.Spending,
        assignedMoney = 0.0,
        availableMoney = 100.0,
        optionalText = "",
        listPosition = 1,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun insuranceCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 3L,
        groupId = 0L,
        emoji = "🛡️",
        name = "Versicherungen",
        budgetTarget = 0.0,
        budgetPurpose = BudgetPurpose.Spending,
        assignedMoney = 0.0,
        availableMoney = 300.0,
        optionalText = "",
        listPosition = 2,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun healthInsuranceCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 4L,
        groupId = 0L,
        emoji = "💊",
        name = "Gesundheitsversicherung",
        budgetTarget = 500.0,
        budgetPurpose = BudgetPurpose.Spending,
        assignedMoney = 0.0,
        availableMoney = 500.0,
        optionalText = "",
        listPosition = 3,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun groceriesCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 5L,
        groupId = 1L,
        emoji = "🥦",
        name = "Lebensmittel",
        budgetTarget = 300.0,
        budgetPurpose = BudgetPurpose.Spending,
        assignedMoney = 150.0,
        availableMoney = 70.0,
        optionalText = "",
        listPosition = 4,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun transportCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 6L,
        groupId = 1L,
        emoji = "🚍",
        name = "Transport",
        budgetTarget = null,
        budgetPurpose = BudgetPurpose.Spending,
        assignedMoney = 0.0,
        availableMoney = 0.0,
        optionalText = "",
        listPosition = 5,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun clothingCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 7L,
        groupId = 1L,
        emoji = "👗",
        name = "Kleidung",
        budgetTarget = 200.0,
        budgetPurpose = BudgetPurpose.Spending,
        assignedMoney = 0.0,
        availableMoney = 100.0,
        optionalText = "",
        listPosition = 6,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun eatingOutCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 8L,
        groupId = 1L,
        emoji = "🍽️",
        name = "Essen gehen",
        budgetTarget = 0.0,
        budgetPurpose = BudgetPurpose.Spending,
        assignedMoney = 0.0,
        availableMoney = 150.0,
        optionalText = "",
        listPosition = 7,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun petsCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 9L,
        groupId = 1L,
        emoji = "🐾",
        name = "Haustiere",
        budgetTarget = 150.0,
        budgetPurpose = BudgetPurpose.Spending,
        assignedMoney = 0.0,
        availableMoney = 200.0,
        optionalText = "",
        listPosition = 8,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun savingsCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 10L,
        groupId = 2L,
        emoji = "💰",
        name = "Ersparnisse",
        budgetTarget = null,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 0.0,
        availableMoney = 400.0,
        optionalText = "",
        listPosition = 9,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun vacationCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 11L,
        groupId = 2L,
        emoji = "🏖️",
        name = "Urlaub",
        budgetTarget = 1000.0,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 300.0,
        availableMoney = 0.0,
        optionalText = "",
        listPosition = 10,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun hobbiesCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 12L,
        groupId = 2L,
        emoji = "🎨",
        name = "Hobbys",
        budgetTarget = 0.0,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 150.0,
        availableMoney = 180.0,
        optionalText = "",
        listPosition = 11,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun educationCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 13L,
        groupId = 2L,
        emoji = "📚",
        name = "Weiterbildung",
        budgetTarget = 250.0,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 100.0,
        availableMoney = 0.0,
        optionalText = "",
        listPosition = 12,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun investmentsCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 14L,
        groupId = 3L,
        emoji = "📈",
        name = "Investitionen",
        budgetTarget = 500.0,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 0.0,
        availableMoney = 700.0,
        optionalText = "",
        listPosition = 13,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun debtsCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 15L,
        groupId = 3L,
        emoji = "💳",
        name = "Schulden",
        budgetTarget = 1000.0,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 500.0,
        availableMoney = 300.0,
        optionalText = "",
        listPosition = 14,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun emergencyFundCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 16L,
        groupId = 3L,
        emoji = "🆘",
        name = "Notgroschen",
        budgetTarget = 300.0,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 200.0,
        availableMoney = 0.0,
        optionalText = "",
        listPosition = 15,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun healthcareCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 17L,
        groupId = 4L,
        emoji = "🏥",
        name = "Gesundheitspflege",
        budgetTarget = 100.0,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 0.0,
        availableMoney = 90.0,
        optionalText = "",
        listPosition = 16,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun gymCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 18L,
        groupId = 4L,
        emoji = "🏋️‍♂️",
        name = "Fitnessstudio",
        budgetTarget = 0.0,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 0.0,
        availableMoney = 50.0,
        optionalText = "",
        listPosition = 17,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun dentistCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 19L,
        groupId = 4L,
        emoji = "🦷",
        name = "Zahnarzt",
        budgetTarget = 200.0,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 100.0,
        availableMoney = 0.0,
        optionalText = "",
        listPosition = 18,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun personalCareCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 20L,
        groupId = 4L,
        emoji = "🧴",
        name = "Körperpflege",
        budgetTarget = 0.0,
        budgetPurpose = BudgetPurpose.Saving,
        assignedMoney = 100.0,
        availableMoney = 120.0,
        optionalText = "",
        listPosition = 19,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}