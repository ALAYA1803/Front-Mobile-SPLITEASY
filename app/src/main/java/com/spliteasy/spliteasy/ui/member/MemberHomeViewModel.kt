// ui/member/MemberHomeViewModel.kt
package com.spliteasy.spliteasy.ui.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.domain.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MemberHomeUiState {
    data object Loading : MemberHomeUiState
    data class Ready(
        val balance: BalanceUi,
        val recent: List<ExpenseUi>
    ) : MemberHomeUiState
    data class Error(val message: String) : MemberHomeUiState
}

data class BalanceUi(val iOwe: Double, val meOwe: Double, val net: Double)
data class ExpenseUi(val id: Long, val description: String, val amount: Double)

@HiltViewModel
class MemberHomeViewModel @Inject constructor(
    private val repo: MemberRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemberHomeUiState>(MemberHomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = MemberHomeUiState.Loading
            val groupId = repo.getActiveGroupId() ?: run {
                _uiState.value = MemberHomeUiState.Error("Selecciona un grupo primero.")
                return@launch
            }

            val res = repo.getHomeSnapshot(groupId, forceRefresh)
            res.fold(
                onSuccess = { snapshot ->
                    _uiState.value = MemberHomeUiState.Ready(
                        balance = BalanceUi(
                            iOwe = snapshot.balance.iOwe,
                            meOwe = snapshot.balance.meOwe,
                            net = snapshot.balance.net
                        ),
                        recent = snapshot.recent.map {
                            ExpenseUi(
                                id = it.id,
                                description = it.description,
                                amount = it.amount
                            )
                        }
                    )
                },
                onFailure = { e ->
                    _uiState.value = MemberHomeUiState.Error(e.message ?: "Error al cargar")
                }
            )
        }
    }
}
