package com.sundbybergsit.cromfortune.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

val Colors.onSurfaceVariant: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFECECEC) else Color(0xFF707070)

internal val PrimaryColor = Color(0XFFBD516D)
internal val PrimaryColorVariant = Color(0XFF8B3952)
internal val SecondaryColor = Color(0XFFFCE49C)
internal val SecondaryVariantColor = Color(0XFFD0D0BF)
internal val ProfitColor = Color(0XFF0050EF)
internal val LossColor = Color(0XFFA20025)

internal val LightColors = lightColors(
    primary = PrimaryColor,
    primaryVariant = PrimaryColorVariant,
    secondary = SecondaryColor,
    secondaryVariant = SecondaryVariantColor,
    background = Color.White,
    surface = Color.White,
    error = LossColor,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White,
)

internal val DarkColors = darkColors(
    primary = PrimaryColor,
    primaryVariant = PrimaryColorVariant,
    secondary = LightColors.secondary,
    secondaryVariant = LightColors.secondaryVariant,
    // Default colors inherited from Material theme
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    error = Color(0xFFCF6679),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
).withBrandedSurface()

fun Colors.withBrandedSurface() = copy(
    surface = primary.copy(alpha = 0.08f).compositeOver(this.surface),
)
