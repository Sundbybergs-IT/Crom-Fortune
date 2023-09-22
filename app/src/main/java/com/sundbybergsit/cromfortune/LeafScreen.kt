package com.sundbybergsit.cromfortune

sealed class LeafScreen(val route: String) {

    data object BottomSheetsHome : LeafScreen("bottom-sheet/home")

    data object BottomSheetsHomeAllStocks : LeafScreen("bottom-sheet/home/stocks?profile={profile}") {

        fun createRoute(profile: String): String = "bottom-sheet/home/stocks?profile=$profile"

    }

    data object BottomSheetsHomeStock : LeafScreen("bottom-sheet/home/stock?stock_symbol={stock_symbol}") {

        fun createRoute(stockSymbol: String): String = "bottom-sheet/home/stock?stock_symbol=$stockSymbol"

    }

    data object BottomSheetsNotifications : LeafScreen("bottom-sheet/notifications")

    data object BottomSheetsSettings : LeafScreen("bottom-sheet/settings")

}
