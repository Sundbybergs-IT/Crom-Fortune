package com.sundbybergsit.cromfortune

sealed class LeafScreen(val route: String) {

    object BottomSheetsNotifications : LeafScreen("bottom-sheet/notifications")

    object BottomSheetsSettings : LeafScreen("bottom-sheet/settings")

}
