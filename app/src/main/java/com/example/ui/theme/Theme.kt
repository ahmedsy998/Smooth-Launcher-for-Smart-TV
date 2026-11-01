package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.model.TypographyConfig

private val SmoothTvDarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    onPrimary = PureWhiteText,
    primaryContainer = MidnightNavyElevated,
    onPrimaryContainer = PureWhiteText,
    secondary = RoyalBlueAccent,
    onSecondary = PureWhiteText,
    tertiary = LuminousCyanGlow,
    background = MidnightNavyDeep,
    onBackground = PureWhiteText,
    surface = MidnightNavyBase,
    onSurface = PureWhiteText,
    surfaceVariant = MidnightNavyElevated,
    onSurfaceVariant = SoftLavenderText,
    error = DisconnectedRedSlash
)

@Composable
fun SmoothTvLauncherTheme(
    typographyConfig: TypographyConfig = TypographyConfig(),
    content: @Composable () -> Unit
) {
    val activeTypography = buildLauncherTypography(typographyConfig)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = SmoothTvDarkColorScheme,
            typography = activeTypography,
            content = content
        )
    }
}
