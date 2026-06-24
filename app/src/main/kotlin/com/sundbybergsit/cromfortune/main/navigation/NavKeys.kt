package com.sundbybergsit.cromfortune.main.navigation

import kotlinx.serialization.Serializable
import androidx.navigation3.runtime.NavKey as androidxNavKey

@Serializable
sealed interface NavKey : androidxNavKey

@Serializable
data object Home : NavKey

@Serializable
data object Dashboard : NavKey

@Serializable
data object Notifications : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object BottomSheetsHome : NavKey

@Serializable
data class BottomSheetsHomeAllStocks(val profile: String) : NavKey

@Serializable
data class BottomSheetsHomeStock(val portfolioName: String, val stockSymbol: String) : NavKey

@Serializable
data object BottomSheetsNotifications : NavKey

@Serializable
data object BottomSheetsSettings : NavKey
