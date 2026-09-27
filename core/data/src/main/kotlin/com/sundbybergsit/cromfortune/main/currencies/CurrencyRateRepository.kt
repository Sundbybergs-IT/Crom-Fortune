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
    val _currencyRates: MutableStateFlow<Set<CurrencyRate>> = MutableStateFlow(setOf(CurrencyRate("SEK", 1.0)))

    override val currencyRates: StateFlow<Set<CurrencyRate>> = _currencyRates

    override fun addAll(currencyRates: Set<CurrencyRate>) {
        Log.v(TAG, "addAll(${currencyRates})")
        val mergedByCurrencyCode = linkedMapOf<String, CurrencyRate>()
        for (currencyRate in this.currencyRates.value) {
            mergedByCurrencyCode[currencyRate.iso4217CurrencySymbol] = currencyRate
        }
        for (currencyRate in currencyRates) {
            mergedByCurrencyCode[currencyRate.iso4217CurrencySymbol] = currencyRate
        }
        _currencyRates.value = mergedByCurrencyCode.values.toSet()
    }

    @VisibleForTesting
    fun clear() {
        _currencyRates.value = setOf()
    }

}
