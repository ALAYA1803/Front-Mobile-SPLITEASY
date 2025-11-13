package com.spliteasy.spliteasy

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.core.Routes
import com.spliteasy.spliteasy.ui.navigation.AppNav
import com.spliteasy.spliteasy.ui.theme.SplitEasyTheme
import dagger.hilt.android.AndroidEntryPoint
import com.spliteasy.spliteasy.data.local.TokenDataStore
import kotlinx.coroutines.flow.first
import com.spliteasy.spliteasy.ui.settings.ThemeViewModel
import kotlinx.coroutines.runBlocking
import java.util.Locale
import com.spliteasy.spliteasy.ui.chat.FloatingChatWidget

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val theme by themeViewModel.theme.collectAsState()

            val useDarkTheme = when (theme) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            SplitEasyTheme(darkTheme = useDarkTheme) {
                AppNav(startDestination = Routes.LOGIN)
            }
        }
    }

    override fun recreate() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.recreate()
    }

    private fun wrapContext(base: Context): Context {
        val dataStore = TokenDataStore(base)
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
        super.attachBaseContext(wrapContext(newBase))
    }
}