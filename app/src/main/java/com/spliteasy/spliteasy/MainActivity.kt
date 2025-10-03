package com.spliteasy.spliteasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spliteasy.spliteasy.core.Routes
import com.spliteasy.spliteasy.ui.navigation.AppNav
import com.spliteasy.spliteasy.ui.theme.SplitEasyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplitEasyTheme {
                AppNav(startDestination = Routes.LOGIN)
            }
        }
    }
}
