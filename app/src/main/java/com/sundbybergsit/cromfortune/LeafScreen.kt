package com.sundbybergsit.cromfortune

sealed class LeafScreen(val route: String) {

    data object BottomSheetsHome : LeafScreen("bottom-sheet/home")

    data object BottomSheetsNotifications : LeafScreen("bottom-sheet/notifications")

    data object BottomSheetsSettings : LeafScreen("bottom-sheet/settings")

}
