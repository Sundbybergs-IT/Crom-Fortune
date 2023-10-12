package com.sundbybergsit.cromfortune.main.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sundbybergsit.cromfortune.main.PortfolioRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class HomeViewModelFactory(
    private val portfolioRepository: PortfolioRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            ioDispatcher = ioDispatcher,
            portfolioRepository = portfolioRepository
        ) as T
    }

}
