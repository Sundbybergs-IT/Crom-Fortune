package com.sundbybergsit.cromfortune.main.navigation

import androidx.navigation.NavHostController

/**
 * Here we evaluate the last "root" we were at, by reversing the backstack and picking the first item that is a root item.
 * And since it starts at null we default to Home.
 */
internal fun NavHostController.getLastRootRoute(bottomNavigationItems: List<NavigationItem>) =
    currentBackStack.value.lastOrNull { backItem ->
        bottomNavigationItems.map { it.screen.route }.any { it == backItem.destination.route }
    }?.destination?.route ?: Screen.Home.route
