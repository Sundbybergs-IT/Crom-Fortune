package com.sundbybergsit.cromfortune

sealed class LeafScreen(val route: String) {

    object BottomSheetsSettings : LeafScreen("bottom-sheet/settings")

}
