package com.sundbybergsit.cromfortune.currencies

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.time.Instant

object CurrencyRateRepository {

    private const val TAG = "CurrencyRateRepository"

    @Suppress("ObjectPropertyName")
    private val _currencyRates = MutableLiveData<ViewState>()

    val currencyRates: LiveData<ViewState> = _currencyRates

    fun add(currencyRates: Set<CurrencyRate>) {
        Log.v(TAG, "put(${currencyRates})")
        _currencyRates.postValue(ViewState.VALUES(Instant.now(), currencyRates))
    }

    sealed class ViewState {
        data class VALUES(val instant: Instant, val currencyRates: Set<CurrencyRate>) : ViewState()
    }

}
