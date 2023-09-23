package com.sundbybergsit.cromfortune.domain.currencies

import kotlinx.coroutines.flow.StateFlow

interface CurrencyRateApi {

    val currencyRates: StateFlow<Set<CurrencyRate>>

    fun addAll(currencyRates: Set<CurrencyRate>)

}
