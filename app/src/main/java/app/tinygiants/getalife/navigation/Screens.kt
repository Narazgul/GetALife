package app.tinygiants.getalife.navigation

import app.tinygiants.getalife.R

sealed class Screens(val label: String, val iconId: Int, val route: String) {
    data object Budget : Screens(label = "Budget", iconId = R.drawable.ic_dashboard, route = "budget")
    data object Account : Screens(label = "Account", iconId = R.drawable.ic_account, route = "account")
}