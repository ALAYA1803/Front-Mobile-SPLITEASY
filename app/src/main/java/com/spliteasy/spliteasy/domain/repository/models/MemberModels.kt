
package com.spliteasy.spliteasy.domain.repository.models

data class MemberHomeSnapshot(
    val balance: Balance,
    val recent: List<Expense>
)

data class Balance(val iOwe: Double, val meOwe: Double) {
    val net: Double get() = meOwe - iOwe
}

data class Expense(
    val id: Long,
    val description: String,
    val amount: Double
)
