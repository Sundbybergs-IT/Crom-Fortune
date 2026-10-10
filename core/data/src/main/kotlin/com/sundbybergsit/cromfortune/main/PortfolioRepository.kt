package com.sundbybergsit.cromfortune.main

import android.content.Context
import android.util.Log
import com.sundbybergsit.cromfortune.main.db.CromFortuneDatabase
import com.sundbybergsit.cromfortune.main.db.PortfolioEntity
import com.sundbybergsit.cromfortune.main.notifications.NotificationsRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PortfolioRepository : Taggable {

    const val DEFAULT_PORTFOLIO_NAME = "Default"
    const val CROM_PORTFOLIO_NAME = "Crom"

    private lateinit var appContext: Context
    private val _selectedPortfolioNameStateFlow: MutableStateFlow<String> =
        MutableStateFlow(DEFAULT_PORTFOLIO_NAME)
    val selectedPortfolioNameStateFlow: StateFlow<String> = _selectedPortfolioNameStateFlow.asStateFlow()

    private val _portfolioNamesStateFlow: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val portfolioNamesStateFlow: StateFlow<List<String>> = _portfolioNamesStateFlow.asStateFlow()

    fun init(context: Context) {
        Log.i(TAG, "init()")
        appContext = context.applicationContext
        com.sundbybergsit.cromfortune.main.db.LegacySharedPreferencesMigrator.migrate(appContext)
        val dao = CromFortuneDatabase.getInstance(appContext).portfolioDao()
        val list = dao.getAllPortfoliosList()
        if (list.isEmpty()) {
            dao.insert(PortfolioEntity(DEFAULT_PORTFOLIO_NAME))
            dao.insert(PortfolioEntity(CROM_PORTFOLIO_NAME))
            _portfolioNamesStateFlow.value = listOf(DEFAULT_PORTFOLIO_NAME, CROM_PORTFOLIO_NAME)
        } else {
            _portfolioNamesStateFlow.value = list
        }
        _selectedPortfolioNameStateFlow.value = DEFAULT_PORTFOLIO_NAME
    }

    fun saveNew(portfolioName: String) {
        Log.i(TAG, "saveNew(${portfolioName})")
        val dao = CromFortuneDatabase.getInstance(appContext).portfolioDao()
        dao.insert(PortfolioEntity(portfolioName))
        _portfolioNamesStateFlow.value = dao.getAllPortfoliosList()
    }

    fun setCurrentPortfolio(portfolioName: String) {
        Log.i(TAG, "setCurrentPortfolio(${portfolioName})")
        this._selectedPortfolioNameStateFlow.value = portfolioName
    }

    fun remove(context: Context, portfolioName: String): Boolean {
        if (portfolioName == DEFAULT_PORTFOLIO_NAME || portfolioName == CROM_PORTFOLIO_NAME) {
            Log.w(TAG, "Refusing to remove fixed portfolio [$portfolioName]")
            return false
        }

        val db = CromFortuneDatabase.getInstance(context)
        val portfolioDao = db.portfolioDao()
        val portfolioNames = portfolioDao.getAllPortfoliosList().toMutableSet()
        if (!portfolioNames.remove(portfolioName)) return false

        portfolioDao.delete(portfolioName)
        db.assetTransactionDao().deleteAllForPortfolio(portfolioName)
        db.stockOrderDao().deleteAllForPortfolio(portfolioName)
        db.stockSplitDao().deleteAllForPortfolio(portfolioName)

        val notificationsRepository = NotificationsRepositoryImpl(context)
        notificationsRepository.list()
            .filter { it.portfolioName == portfolioName }
            .forEach(notificationsRepository::remove)

        _portfolioNamesStateFlow.value = portfolioDao.getAllPortfoliosList()
        if (_selectedPortfolioNameStateFlow.value == portfolioName) {
            _selectedPortfolioNameStateFlow.value = DEFAULT_PORTFOLIO_NAME
        }
        return true
    }

}
