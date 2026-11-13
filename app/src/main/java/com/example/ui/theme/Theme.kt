package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RyujinDarkScheme = darkColorScheme(
    primary = CrimsonDragon,
    onPrimary = Color.White,
    primaryContainer = CrimsonDragonDeep,
    onPrimaryContainer = IvoryText,
    secondary = BrushedGold,
    onSecondary = Color(0xFF1A1405),
    secondaryContainer = BrushedGoldSoft,
    onSecondaryContainer = BrushedGold,
    tertiary = JadeVerified,
    background = ObsidianBg,
    onBackground = IvoryText,
    surface = ObsidianSurface,
    onSurface = IvoryText,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = SilverMutedText,
    outline = ObsidianCardBorder,
    outlineVariant = Color(0xFF1F2433)
)

private val RyujinLightScheme = lightColorScheme(
    primary = CrimsonDragonDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFADCE0),
    onPrimaryContainer = CrimsonDragonDeep,
    secondary = Color(0xFF9C7815),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5EBD0),
    onSecondaryContainer = Color(0xFF3E2E04),
    tertiary = Color(0xFF198754),
    background = PaperBg,
    onBackground = InkCharcoal,
    surface = PaperSurface,
    onSurface = InkCharcoal,
    surfaceVariant = PaperSurfaceElevated,
    onSurfaceVariant = SlateMuted,
    outline = Color(0xFFDCD5C7),
    outlineVariant = Color(0xFFE9E3D8)
)

@Composable
fun RyujinStudioTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RyujinDarkScheme else RyujinLightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = RyujinTypography,
        content = content
    )
}
