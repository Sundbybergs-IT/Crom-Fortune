package com.sundbybergsit.cromfortune.ui.home.view

import com.sundbybergsit.cromfortune.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.ui.AdapterItem
import com.sundbybergsit.cromfortune.ui.home.HeaderAdapterItem
import java.util.*
import kotlin.collections.ArrayList

internal object OpinionatedStockOrderWrapperAdapterItemUtil {

    fun convertToAdapterItems(
        recommendationAlgorithm: CromFortuneV1RecommendationAlgorithm,
        list: Iterable<StockEvent>,
    ): List<AdapterItem> {
        val result: MutableList<AdapterItem> = ArrayList()
        result.add(HeaderAdapterItem())
        val stockOrderEvents = list.filter { it.stockOrder != null }.toList()
        val currencyRateInSek =
            (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES).currencyRates
                .find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrderEvents.first().stockOrder!!.currency }!!.rateInSek
        for (stockOrderEvent in stockOrderEvents) {
            with(checkNotNull(stockOrderEvent.stockOrder)) {
                val pdAdapterItem = OpinionatedStockOrderWrapperAdapterItem(
                    OpinionatedStockOrderWrapper(
                        this,
                        recommendationAlgorithm.getRecommendation(
                            StockPrice(name, Currency.getInstance(currency), pricePerStock),
                            currencyRateInSek, commissionFee,
                            stockOrderEvents.subList(0, stockOrderEvents.indexOf(stockOrderEvent)).toSet(),
                            stockOrderEvent.dateInMillis
                        )
                    )
                )
                result.add(pdAdapterItem)
            }
        }
        return result
    }

}
