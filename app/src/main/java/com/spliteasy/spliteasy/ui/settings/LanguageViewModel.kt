package com.spliteasy.spliteasy.ui.settings

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.local.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    app: Application,
    private val dataStore: TokenDataStore
) : AndroidViewModel(app) {

    val currentLanguageFlow = dataStore.languageFlow

    private val _isRestarting = MutableStateFlow(false)
    val isRestarting = _isRestarting.asStateFlow()

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            val currentLang = dataStore.languageFlow.first()
            if (lang == currentLang) return@launch
            _isRestarting.value = true

            Log.d("LanguageSetup", "[ViewModel] Guardando idioma '$lang'...")
            dataStore.saveLanguage(lang)
            Log.d("LanguageSetup", "[ViewModel] Aplicando locale '$lang'...")
            val appLocale = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun onLanguageApplied() {
        if (_isRestarting.value) {
            _isRestarting.value = false
        }
    }

}