package com.sundbybergsit.cromfortune.main.currencies

import androidx.compose.runtime.State
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository.ViewState

interface CurrencyRateApi {

    val currencyRates: State<ViewState?>

    fun add(currencyRates: Set<CurrencyRate>)

}
