package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.stocks.StockPrice
import java.util.*
import kotlin.collections.ArrayList

internal object OpinionatedStockOrderWrapperAdapterItemUtil {

    suspend fun convertToAdapterItems(
            recommendationAlgorithm: CromFortuneV1RecommendationAlgorithm,
            list: Iterable<StockOrder>,
    ): List<AdapterItem> {
        val result: MutableList<AdapterItem> = ArrayList()
        result.add(StockHeaderAdapterItem())
        val currencyRateInSek = (CurrencyRateRepository.currencyRates.value as
                CurrencyRateRepository.ViewState.VALUES).currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == list.first().currency }!!.rateInSek
        val toList = list.toList()
        for (stockOrder in toList) {
            val pdAdapterItem = OpinionatedStockOrderWrapperAdapterItem(OpinionatedStockOrderWrapper(stockOrder,
                    recommendationAlgorithm.getRecommendation(StockPrice(
                            stockOrder.name,
                            Currency.getInstance(stockOrder.currency), stockOrder.pricePerStock,
                    ),
                            currencyRateInSek, stockOrder.commissionFee,
                            toList.subList(0, toList.indexOf(stockOrder)).toSet())))
            result.add(pdAdapterItem)
        }
        return result
    }

}
