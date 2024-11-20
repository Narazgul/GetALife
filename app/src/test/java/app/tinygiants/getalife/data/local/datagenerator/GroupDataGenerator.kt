package app.tinygiants.getalife.data.local.datagenerator

import app.tinygiants.getalife.data.local.entities.GroupEntity

val groups = listOf(
    fixedCosts(),
    dailyLife(),
    dreams(),
    savings()
)

fun fixedCosts() = GroupEntity(
    id = 1L,
    name = "Fixed costs",
    listPosition = 0,
    isExpanded = false
)

fun dailyLife() = GroupEntity(
    id = 2L,
    name = "Daily life",
    listPosition = 1,
    isExpanded = false
)

fun dreams() = GroupEntity(
    id = 3L,
    name = "Dreams",
    listPosition = 2,
    isExpanded = false
)

fun savings() = GroupEntity(
    id = 4L,
    name = "Savings",
    listPosition = 3,
    isExpanded = false
)