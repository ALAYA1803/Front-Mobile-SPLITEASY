package com.spliteasy.spliteasy

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity // 👈 ESTA LÍNEA SE VA
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity // 👈 AÑADE ESTA LÍNEA
import com.spliteasy.spliteasy.core.Routes
import com.spliteasy.spliteasy.ui.navigation.AppNav
import com.spliteasy.spliteasy.ui.theme.SplitEasyTheme
import dagger.hilt.android.AndroidEntryPoint
import com.spliteasy.spliteasy.data.local.TokenDataStore // 👈 AÑADIDO
import com.spliteasy.spliteasy.ui.navigation.AppNav
import com.spliteasy.spliteasy.ui.theme.SplitEasyTheme
import kotlinx.coroutines.flow.first // 👈 AÑADIDO
import kotlinx.coroutines.runBlocking // 👈 AÑADIDO
import java.util.Locale // 👈 AÑADIDO
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplitEasyTheme {
                AppNav(startDestination = Routes.LOGIN)
            }
        }
    }

    private fun wrapContext(base: Context): Context {
        // Instanciamos manualmente porque Hilt no ha corrido todavía
        val dataStore = TokenDataStore(base)

        // Leemos el idioma guardado (como en tu SplitEasyApp anterior)
        // Usamos runBlocking porque esto DEBE ser síncrono
        val lang = runBlocking {
            dataStore.languageFlow.first()
        }

        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = base.resources.configuration
        config.setLocale(locale)

        return base.createConfigurationContext(config)
    }

    override fun attachBaseContext(newBase: Context) {
        // Envolvemos el contexto ANTES de que la actividad "exista"
        super.attachBaseContext(wrapContext(newBase))
    }
    // --- ⬆️ HASTA AQUÍ ⬆️ ---
}