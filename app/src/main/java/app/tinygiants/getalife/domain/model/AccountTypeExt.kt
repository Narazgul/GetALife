package app.tinygiants.getalife.domain.model

// Extension property to determine if an account type should be included in budget calculations
val AccountType.includeInBudget: Boolean
    get() = when (this) {
        AccountType.Cash,
        AccountType.Checking,
        AccountType.Savings,
        AccountType.CreditCard -> true

        else -> false
    }