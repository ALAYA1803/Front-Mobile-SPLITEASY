package com.spliteasy.spliteasy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SplitEasyLightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = AlwaysWhite,
    secondary = BrandSecondary,
    onSecondary = AlwaysWhite,
    background = LightBgMain,
    onBackground = LightTextPrimary,
    surface = LightCardBg,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = DangerColor,
    onError = AlwaysWhite
)

private val SplitEasyDarkColors = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = AlwaysWhite,
    secondary = BrandSecondary,
    onSecondary = AlwaysWhite,
    background = DarkBgMain,
    onBackground = DarkTextPrimary,
    surface = DarkCardBg,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = DangerColor,
    onError = AlwaysWhite
)

@Composable
fun SplitEasyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) SplitEasyDarkColors else SplitEasyLightColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}