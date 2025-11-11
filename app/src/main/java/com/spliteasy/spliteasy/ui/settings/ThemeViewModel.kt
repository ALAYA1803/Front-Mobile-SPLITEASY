package com.spliteasy.spliteasy.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.local.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val dataStore: TokenDataStore
) : ViewModel() {

    val theme = dataStore.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "SYSTEM"
    )

    fun setTheme(theme: String) {
        viewModelScope.launch {
            dataStore.saveTheme(theme)
        }
    }
}