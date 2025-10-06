package com.spliteasy.spliteasy.ui.member

data class MemberItemUi(
    val id: Long,
    val username: String,
    val email: String,
    val roles: List<String>
)

sealed interface MemberHomeUiState {
    data object Loading : MemberHomeUiState

    data class Ready(
        val householdName: String,
        val householdDescription: String,
        val currency: String,
        val members: List<MemberItemUi>,
        val totalPending: Double,
        val totalPaid: Double,
        val activeContribsCount: Int,
        val currentUserId: Long? = null,
        val currentUserName: String? = null
    ) : MemberHomeUiState

    data class Empty(val reason: String) : MemberHomeUiState
    data class Error(val message: String) : MemberHomeUiState
}
