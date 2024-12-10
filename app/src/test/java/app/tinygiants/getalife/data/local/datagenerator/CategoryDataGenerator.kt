package app.tinygiants.getalife.data.local.datagenerator

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import kotlinx.datetime.Instant

const val FIXED_COST_GROUP = 1L
const val DAILY_LIFE_GROUP = 2L
const val DREAMS_GROUP = 3L
const val SAVINGS_GROUP = 4L

val categoryEntities = listOf(
    rentCategoryEntity(),
    studentLoanRepaymentCategoryEntity(),
    insuranceCategoryEntity(),
    electricityCategoryEntity(),
    groceriesCategoryEntity(),
    transportCategoryEntity(),
    clothingCategoryEntity(),
    eatingOutCategoryEntity(),
    fitnessCategoryEntity(),
    savingsCategoryEntity(),
    vacationCategoryEntity(),
    hobbiesCategoryEntity(),
    educationCategoryEntity(),
    investmentsCategoryEntity(),
    debtsCategoryEntity(),
    emergencyFundCategoryEntity(),
    subscriptionCategoryEntity(),
    gymCategoryEntity(),
    dentistCategoryEntity(),
    personalCareCategoryEntity()
)

val categories = categoryEntities.map { categoryEntity -> categoryEntity.toDomain() }

fun rentCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 1L,
        groupId = FIXED_COST_GROUP,
        emoji = "🏠",
        name = "Rent",
        budgetTarget = 1200.0,
        assignedMoney = 1200.0,
        availableMoney = 1300.0,
        listPosition = 0,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun studentLoanRepaymentCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 2L,
        groupId = FIXED_COST_GROUP,
        emoji = "🎓",
        name = "Student loan repayment",
        budgetTarget = 300.0,
        assignedMoney = 0.0,
        availableMoney = 100.0,
        listPosition = 1,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun insuranceCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 3L,
        groupId = FIXED_COST_GROUP,
        emoji = "🛡️",
        name = "Insurance",
        budgetTarget = 0.0,
        assignedMoney = 0.0,
        availableMoney = 300.0,
        listPosition = 2,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun electricityCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 4L,
        groupId = FIXED_COST_GROUP,
        emoji = "⚡",
        name = "EON",
        budgetTarget = 500.0,
        assignedMoney = 0.0,
        availableMoney = 500.0,
        listPosition = 3,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun groceriesCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 5L,
        groupId = DAILY_LIFE_GROUP,
        emoji = "🥦",
        name = "Groceries",
        budgetTarget = 300.0,
        assignedMoney = 150.0,
        availableMoney = 70.0,
        listPosition = 4,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun transportCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 6L,
        groupId = DAILY_LIFE_GROUP,
        emoji = "🚍",
        name = "Transport",
        budgetTarget = 0.0,
        assignedMoney = 0.0,
        availableMoney = 0.0,
        listPosition = 5,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun clothingCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 7L,
        groupId = DAILY_LIFE_GROUP,
        emoji = "👗",
        name = "Clothes",
        budgetTarget = 200.0,
        assignedMoney = 0.0,
        availableMoney = 100.0,
        listPosition = 6,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun eatingOutCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 8L,
        groupId = DAILY_LIFE_GROUP,
        emoji = "🍽️",
        name = "Eat out",
        budgetTarget = 0.0,
        assignedMoney = 0.0,
        availableMoney = 150.0,
        listPosition = 7,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun fitnessCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 9L,
        groupId = FIXED_COST_GROUP,
        emoji = "🐾",
        name = "Fitness",
        budgetTarget = 150.0,
        assignedMoney = 0.0,
        availableMoney = 200.0,
        listPosition = 8,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun savingsCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 10L,
        groupId = SAVINGS_GROUP,
        emoji = "💰",
        name = "Savings",
        budgetTarget = 0.0,
        assignedMoney = 0.0,
        availableMoney = 400.0,
        listPosition = 9,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun vacationCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 11L,
        groupId = DREAMS_GROUP,
        emoji = "🏖️",
        name = "Holiday",
        budgetTarget = 1000.0,
        assignedMoney = 300.0,
        availableMoney = 0.0,
        listPosition = 10,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun hobbiesCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 12L,
        groupId = DREAMS_GROUP,
        emoji = "🎨",
        name = "Hobbies",
        budgetTarget = 0.0,
        assignedMoney = 150.0,
        availableMoney = 180.0,
        listPosition = 11,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun educationCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 13L,
        groupId = DREAMS_GROUP,
        emoji = "📚",
        name = "Further Training",
        budgetTarget = 250.0,
        assignedMoney = 100.0,
        availableMoney = 0.0,
        listPosition = 12,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun investmentsCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 14L,
        groupId = SAVINGS_GROUP,
        emoji = "📈",
        name = "Investments",
        budgetTarget = 500.0,
        assignedMoney = 0.0,
        availableMoney = 700.0,
        listPosition = 13,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun debtsCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 15L,
        groupId = SAVINGS_GROUP,
        emoji = "💳",
        name = "Debts",
        budgetTarget = 1000.0,
        assignedMoney = 500.0,
        availableMoney = 300.0,
        listPosition = 14,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun emergencyFundCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 16L,
        groupId = SAVINGS_GROUP,
        emoji = "🆘",
        name = "Emergency Fund",
        budgetTarget = 300.0,
        assignedMoney = 200.0,
        availableMoney = 0.0,
        listPosition = 15,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun subscriptionCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 17L,
        groupId = DAILY_LIFE_GROUP,
        emoji = "🏥",
        name = "Subscriptions",
        budgetTarget = 100.0,
        assignedMoney = 0.0,
        availableMoney = 90.0,
        listPosition = 16,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun gymCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 18L,
        groupId = DAILY_LIFE_GROUP,
        emoji = "🏋️‍♂️",
        name = "Gym",
        budgetTarget = 0.0,
        assignedMoney = 0.0,
        availableMoney = 50.0,
        listPosition = 17,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun dentistCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 19L,
        groupId = DAILY_LIFE_GROUP,
        emoji = "🦷",
        name = "Dentist",
        budgetTarget = 200.0,
        assignedMoney = 100.0,
        availableMoney = 0.0,
        listPosition = 18,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}

fun personalCareCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = 20L,
        groupId = DAILY_LIFE_GROUP,
        emoji = "🧴",
        name = "Body care",
        budgetTarget = 0.0,
        assignedMoney = 100.0,
        availableMoney = 120.0,
        listPosition = 19,
        isInitialCategory = false,
        updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        createdAt = Instant.parse("2024-01-01T12:00:00Z")
    )
}