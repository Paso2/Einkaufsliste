package de.paso2.einkaufsliste.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.paso2.einkaufsliste.data.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B6B4A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6EFE2),
    onPrimaryContainer = Color(0xFF07291A),
    secondary = Color(0xFF4C6357),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFE9D9),
    onSecondaryContainer = Color(0xFF0A2016),
    tertiary = Color(0xFF3D6373),
    background = Color(0xFFF6F8F6),
    onBackground = Color(0xFF171D1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171D1A),
    surfaceVariant = Color(0xFFE7EDE9),
    onSurfaceVariant = Color(0xFF424C46),
    outline = Color(0xFF727D76),
    outlineVariant = Color(0xFFD5DDD8),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF72D6A6),
    onPrimary = Color(0xFF003823),
    primaryContainer = Color(0xFF1A4F38),
    onPrimaryContainer = Color(0xFFC6F4DC),
    secondary = Color(0xFFB3CCBE),
    onSecondary = Color(0xFF1F352B),
    secondaryContainer = Color(0xFF354B40),
    onSecondaryContainer = Color(0xFFCFE9D9),
    tertiary = Color(0xFFA5CCDE),
    background = Color(0xFF101412),
    onBackground = Color(0xFFE4E9E5),
    surface = Color(0xFF181D1A),
    onSurface = Color(0xFFE4E9E5),
    surfaceVariant = Color(0xFF242B27),
    onSurfaceVariant = Color(0xFFB9C4BD),
    outline = Color(0xFF838E87),
    outlineVariant = Color(0xFF2D3530),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val AppTypography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun EinkaufslisteTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
