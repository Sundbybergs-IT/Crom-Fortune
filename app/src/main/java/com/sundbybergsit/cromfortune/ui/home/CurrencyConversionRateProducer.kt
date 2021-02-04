package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.CromFortuneApp
import java.util.*

open class CurrencyConversionRateProducer(private val app : CromFortuneApp) {

    open fun getRateInSek(currency: Currency) : Double = app.currencyRates[currency.currencyCode]!!

}
