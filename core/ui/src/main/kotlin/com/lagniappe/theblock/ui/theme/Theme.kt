package com.lagniappe.theblock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = OpenlaneBlue,
    onPrimary = Color.White,
    primaryContainer = OpenlaneBlueTint,
    onPrimaryContainer = OpenlaneNavy,
    secondary = OpenlaneNavy,
    onSecondary = Color.White,
    secondaryContainer = OpenlaneBlueTint,
    onSecondaryContainer = OpenlaneNavy,
    tertiary = OpenlaneBlue,
    onTertiary = Color.White,
    tertiaryContainer = OpenlaneBlueTint,
    onTertiaryContainer = OpenlaneNavy,
    background = OpenlaneCanvas,
    onBackground = OpenlaneNavy,
    surface = Color.White,
    onSurface = OpenlaneNavy,
    surfaceVariant = OpenlaneCanvas,
    onSurfaceVariant = OpenlaneInkMuted,
    surfaceTint = OpenlaneBlue,
    surfaceBright = Color.White,
    surfaceDim = OpenlaneBorder,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAFBFD),
    surfaceContainer = OpenlaneCanvas,
    surfaceContainerHigh = Color(0xFFEEF1F6),
    surfaceContainerHighest = Color(0xFFE5EAF2),
    outline = Color(0xFF748099),
    outlineVariant = OpenlaneBorder,
    inverseSurface = OpenlaneNavy,
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFFAFCBFF),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

// An adapted night palette keeps blue controls readable on deep navy surfaces.
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFAFCBFF),
    onPrimary = Color(0xFF002C75),
    primaryContainer = Color(0xFF003FAD),
    onPrimaryContainer = Color(0xFFEAF1FF),
    secondary = Color(0xFFCBD7F5),
    onSecondary = OpenlaneNavy,
    secondaryContainer = Color(0xFF263757),
    onSecondaryContainer = Color(0xFFEAF1FF),
    tertiary = Color(0xFFAFCBFF),
    onTertiary = Color(0xFF002C75),
    tertiaryContainer = Color(0xFF003FAD),
    onTertiaryContainer = Color(0xFFEAF1FF),
    background = Color(0xFF0C1225),
    onBackground = Color(0xFFF0F3FA),
    surface = Color(0xFF151E34),
    onSurface = Color(0xFFF0F3FA),
    surfaceVariant = Color(0xFF263149),
    onSurfaceVariant = Color(0xFFBCC6DA),
    surfaceTint = Color(0xFFAFCBFF),
    surfaceBright = Color(0xFF35415B),
    surfaceDim = Color(0xFF10172A),
    surfaceContainerLowest = Color(0xFF0A1020),
    surfaceContainerLow = Color(0xFF121A2E),
    surfaceContainer = Color(0xFF192239),
    surfaceContainerHigh = Color(0xFF202B43),
    surfaceContainerHighest = Color(0xFF2A364F),
    outline = Color(0xFF8995AE),
    outlineVariant = Color(0xFF3B4863),
    inverseSurface = Color(0xFFEAF1FF),
    inverseOnSurface = OpenlaneNavy,
    inversePrimary = OpenlaneBlue,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val OpenlaneShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun TheBlockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Keep the brand palette consistent, independent of Android wallpaper colors.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = OpenlaneShapes,
        content = content,
    )
}
