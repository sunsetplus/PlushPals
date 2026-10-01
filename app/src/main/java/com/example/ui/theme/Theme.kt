package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaPrimary,
    onPrimary = PureWhite,
    primaryContainer = TerracottaPrimaryDark,
    onPrimaryContainer = TerracottaContainer,
    secondary = AmberAccent,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF5A3900),
    onSecondaryContainer = AmberContainer,
    tertiary = SageGreen,
    onTertiary = PureWhite,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = PureWhite,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = TerracottaOnContainer,
    secondary = AmberAccent,
    onSecondary = PureWhite,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = AmberOnContainer,
    tertiary = SageGreen,
    onTertiary = PureWhite,
    tertiaryContainer = SageContainer,
    onTertiaryContainer = Color(0xFF132B18),
    background = LinenBackground,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = LinenBackground,
    onSurfaceVariant = TextSecondary,
    outline = SoftBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to stunning Light Mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
