package com.sundbybergsit.cromfortune.main.currencies

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRateApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CurrencyRateRepository : CurrencyRateApi {

    private const val TAG = "CurrencyRateRepository"

    @VisibleForTesting
    @Suppress("ObjectPropertyName")
    val _currencyRates: MutableStateFlow<Set<CurrencyRate>> = MutableStateFlow(setOf())

    override val currencyRates: StateFlow<Set<CurrencyRate>> = _currencyRates

    override fun addAll(currencyRates: Set<CurrencyRate>) {
        Log.v(TAG, "addAll(${currencyRates})")
        _currencyRates.value = this.currencyRates.value + currencyRates
    }

    @VisibleForTesting
    fun clear() {
        _currencyRates.value = setOf()
    }

}
