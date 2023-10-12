package com.sundbybergsit.cromfortune.main

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PortfolioRepository : Taggable {

    const val DEFAULT_PORTFOLIO_NAME = "Default"
    const val CROM_PORTFOLIO_NAME = "Crom"

    private lateinit var portfolioSharedPreferences: SharedPreferences

    private val _selectedPortfolioNameStateFlow: MutableStateFlow<String> =
        MutableStateFlow(DEFAULT_PORTFOLIO_NAME)
    val selectedPortfolioNameStateFlow: StateFlow<String> = _selectedPortfolioNameStateFlow.asStateFlow()

    private val _portfolioNamesStateFlow: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val portfolioNamesStateFlow: StateFlow<List<String>> = _portfolioNamesStateFlow.asStateFlow()

    fun init(portfolioSharedPreferences: SharedPreferences) {
        Log.i(TAG, "init()")
        this.portfolioSharedPreferences = portfolioSharedPreferences
        _portfolioNamesStateFlow.value =
            portfolioSharedPreferences.getStringSet(Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET, setOf())?.toList()
                ?: listOf()
    }

    fun saveNew(portfolioName: String) {
        Log.i(TAG, "saveNew(${portfolioName})")
        val portfolioNames =
            portfolioSharedPreferences.getStringSet(Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET, emptySet())!!
                .toMutableSet()
        portfolioNames.add(portfolioName)
        portfolioSharedPreferences.edit()
            .putStringSet(Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET, portfolioNames.toSet()).apply()
        _portfolioNamesStateFlow.value = portfolioNames.toList()
    }

    fun setCurrentPortfolio(portfolioName: String) {
        Log.i(TAG, "setCurrentPortfolio(${portfolioName})")
        this._selectedPortfolioNameStateFlow.value = portfolioName
    }

}
