package com.sundbybergsit.cromfortune.main.currencies

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

object CurrencyRateRepository : CurrencyRateApi {

    private const val TAG = "CurrencyRateRepository"

    @VisibleForTesting
    @Suppress("ObjectPropertyName")
    val _currencyRates: MutableStateFlow<ViewState?> = MutableStateFlow(null)

    override val currencyRates: StateFlow<ViewState?> = _currencyRates

    override fun add(currencyRates: Set<CurrencyRate>) {
        Log.v(TAG, "put(${currencyRates})")
        _currencyRates.value = ViewState(Instant.now(), currencyRates)
    }

    @VisibleForTesting
    fun clear() {
        _currencyRates.value = ViewState(Instant.now(), emptySet())
    }

    class ViewState(val instant: Instant, val currencyRates: Set<CurrencyRate>)

}
