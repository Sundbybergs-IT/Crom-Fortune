package com.sundbybergsit.cromfortune.ui.home.view

import com.sundbybergsit.cromfortune.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.ui.AdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockHeaderAdapterItem
import java.util.*
import kotlin.collections.ArrayList

internal object OpinionatedStockOrderWrapperAdapterItemUtil {

    fun convertToAdapterItems(
        recommendationAlgorithm: CromFortuneV1RecommendationAlgorithm,
        list: Iterable<StockOrder>,
    ): List<AdapterItem> {
        val result: MutableList<AdapterItem> = ArrayList()
        result.add(StockHeaderAdapterItem())
        val currencyRateInSek = (CurrencyRateRepository.currencyRates.value as
                CurrencyRateRepository.ViewState.VALUES).currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == list.first().currency }!!.rateInSek
        val toList = list.toList()
        for (stockOrder in toList) {
            val pdAdapterItem = OpinionatedStockOrderWrapperAdapterItem(
                OpinionatedStockOrderWrapper(
                    stockOrder,
                    recommendationAlgorithm.getRecommendation(
                        StockPrice(
                            stockOrder.name,
                            Currency.getInstance(stockOrder.currency), stockOrder.pricePerStock,
                        ),
                        currencyRateInSek, stockOrder.commissionFee,
                        toList.subList(0, toList.indexOf(stockOrder)).toSet(), stockOrder.dateInMillis
                    )
                )
            )
            result.add(pdAdapterItem)
        }
        return result
    }

}
