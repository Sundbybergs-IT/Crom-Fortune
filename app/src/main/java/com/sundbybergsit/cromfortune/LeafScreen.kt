package com.sundbybergsit.cromfortune

sealed class LeafScreen(val route: String) {

    data object BottomSheetsHome : LeafScreen("bottom-sheet/home")

    data object BottomSheetsHomeStock : LeafScreen("bottom-sheet/home/stock?{stock_symbol}") {

        fun createRoute(stockSymbol: String): String =
            "bottom-sheet/home/stock?stock_symbol=$stockSymbol"

    }

    data object BottomSheetsNotifications : LeafScreen("bottom-sheet/notifications")

    data object BottomSheetsSettings : LeafScreen("bottom-sheet/settings")

}
