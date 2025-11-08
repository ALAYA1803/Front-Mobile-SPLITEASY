package com.spliteasy.spliteasy.ui.settings

import android.app.Application
import android.util.Log // 👈 AÑADIDO PARA LOGS
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.local.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    app: Application,
    private val dataStore: TokenDataStore
) : AndroidViewModel(app) {

    val currentLanguageFlow = dataStore.languageFlow

    fun setLanguage(lang: String) {
        viewModelScope.launch {

            // --- ⬇️ LOGS DE VERIFICACIÓN ⬇️ ---
            Log.d("LanguageSetup", "[ViewModel] El usuario seleccionó '$lang'. Guardando...")
            dataStore.saveLanguage(lang)
            Log.d("LanguageSetup", "[ViewModel] Guardado completo. Aplicando idioma '$lang'...")

            val appLocale = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
            Log.d("LanguageSetup", "[ViewModel] Idioma aplicado. La actividad debería reiniciarse.")
            // --- ⬆️ LOGS DE VERIFICACIÓN ⬆️ ---
        }
    }
}