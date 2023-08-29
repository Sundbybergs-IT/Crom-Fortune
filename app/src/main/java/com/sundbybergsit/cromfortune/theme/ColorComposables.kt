package com.sundbybergsit.cromfortune.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val PrimaryColor = Color(0XFFBD516D)
internal val PrimaryColorVariant = Color(0XFF8B3952)
internal val SecondaryColor = Color(0XFFFCE49C)
internal val SecondaryVariantColor = Color(0XFFD0D0BF)
internal val ProfitColor = Color(0XFF0050EF)
internal val LossColor = Color(0XFFA20025)

internal val LightColors = lightColorScheme(
    primary = PrimaryColor,
    primaryContainer = PrimaryColorVariant,
    secondary = SecondaryColor,
    secondaryContainer = SecondaryVariantColor,
    background = Color.White,
    surface = Color.White,
    onSurfaceVariant = Color(0xFF707070),
    error = LossColor,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White,
)

internal val DarkColors = darkColorScheme(
    primary = PrimaryColor,
    primaryContainer = PrimaryColorVariant,
    secondary = LightColors.secondary,
    secondaryContainer = LightColors.secondaryContainer,
    // Default colors inherited from Material theme
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onSurfaceVariant = Color(0xFFECECEC),
    error = Color(0xFFCF6679),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)
