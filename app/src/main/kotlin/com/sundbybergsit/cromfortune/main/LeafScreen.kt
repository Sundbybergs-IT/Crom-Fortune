package com.sundbybergsit.cromfortune.main

sealed class LeafScreen(val route: String) {

    data object BottomSheetsHome : LeafScreen("bottom-sheet/home")

    data object BottomSheetsHomeAllStocks : LeafScreen("bottom-sheet/home/stocks?profile={profile}") {

        fun createRoute(profile: String): String = "bottom-sheet/home/stocks?profile=$profile"

    }

    data object BottomSheetsHomeStock :
        LeafScreen("bottom-sheet/home/stock?portfolio_name={portfolio_name}&stock_symbol={stock_symbol}") {

        fun createRoute(portfolioName: String, stockSymbol: String): String =
            "bottom-sheet/home/stock?portfolio_name=$portfolioName&stock_symbol=$stockSymbol"

    }

    data object BottomSheetsNotifications : LeafScreen("bottom-sheet/notifications")

    data object BottomSheetsSettings : LeafScreen("bottom-sheet/settings")

}
