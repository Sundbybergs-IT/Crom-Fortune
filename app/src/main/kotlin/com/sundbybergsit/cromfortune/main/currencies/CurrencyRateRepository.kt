package com.sundbybergsit.cromfortune.main.currencies

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import java.time.Instant

object CurrencyRateRepository : CurrencyRateApi {

    private const val TAG = "CurrencyRateRepository"

    @VisibleForTesting
    @Suppress("ObjectPropertyName")
    val _currencyRates: MutableState<ViewState?> = mutableStateOf(null)

    override val currencyRates: State<ViewState?> = _currencyRates

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
