package com.sundbybergsit.cromfortune.navigation

internal sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
}
