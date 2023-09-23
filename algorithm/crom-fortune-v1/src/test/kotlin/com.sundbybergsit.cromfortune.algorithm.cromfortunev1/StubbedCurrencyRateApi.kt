package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRateApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StubbedCurrencyRateApi : CurrencyRateApi {

    private val _currencyRates: MutableStateFlow<Set<CurrencyRate>> = MutableStateFlow(setOf())
    override val currencyRates: StateFlow<Set<CurrencyRate>> = _currencyRates
    override fun addAll(currencyRates: Set<CurrencyRate>) {
        this._currencyRates.value = this.currencyRates.value + currencyRates
    }

}
