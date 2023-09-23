package com.sundbybergsit.cromfortune.main.currencies

import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository.ViewState
import kotlinx.coroutines.flow.StateFlow

interface CurrencyRateApi {

    val currencyRates: StateFlow<ViewState?>

    fun add(currencyRates: Set<CurrencyRate>)

}
