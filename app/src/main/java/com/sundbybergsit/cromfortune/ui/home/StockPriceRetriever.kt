package com.sundbybergsit.cromfortune.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sundbybergsit.cromfortune.roundTo
import kotlinx.coroutines.*
import yahoofinance.Stock
import yahoofinance.YahooFinance
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class StockPriceRetriever(private val stockPriceProducer: StockPriceProducer, private val interval: Long,
                          private val initialDelay: Long?) :
        CoroutineScope {

    companion object {

        val CURRENCIES = arrayOf("CAD", "EUR", "NOK", "SEK", "USD")
        val SYMBOLS = arrayOf(Pair("AC.TO", "Air Canada"), Pair("ACST", "Acasti Pharma Inc."),
                Pair("ANOT.ST", "Anoto Group AB (publ)"), Pair("ASSA-B.ST", "ASSA ABLOY AB (publ)"),
                Pair("AZELIO.ST", "Azelio AB (publ)"), Pair("BUBL.ST", "Bublar Group AB (publ)"),
                Pair("CLOUD.OL", "Cloudberry Clean Energy AS"), Pair("EOLU-B.ST", "Eolus Vind AB (publ)"),
                Pair("FERRO.ST", "Ferroamp Elektronik AB (publ)"), Pair("GGG.V", "G6 Materials Corp."),
                Pair("HIMX", "Himax Technologies, Inc."), Pair("IPCO.ST", "International Petroleum Corporation"),
                Pair("LHA.F", "Deutsche Lufthansa AG"), Pair("LPK.DE", "LPKF Laser & Electronics AG"),
                Pair("MIPS.ST", "MIPS AB (publ)"), Pair("NAS.OL", "Norwegian Air Shuttle ASA"),
                Pair("SALT-B.ST", "SaltX Technology Holding AB"), Pair("SAND.ST", "Sandvik AB"),
                Pair("SAS.ST", "SAS AB (publ)"), Pair("SHOT.ST", "Scandic Hotels Group AB (publ)"),
                Pair("SOLT.ST","SolTech Energy Sweden AB (publ)"),
                Pair("TANGI.ST","Tangiamo Touch Technology AB (publ)"), Pair("TSLA","Tesla, Inc.")
        )

    }

    private val _stockPrices = MutableLiveData<StockPrice>()
    private val allStockPrices: MutableSet<StockPrice> = mutableSetOf()

    val stockPrices: LiveData<StockPrice> = _stockPrices

    private val job = Job()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()

    fun stop() {
        job.cancel()
        singleThreadExecutor.shutdown()
    }

    fun start() = launch {
        initialDelay?.let {
            delay(it)
        }
        GlobalScope.launch(Dispatchers.IO) {
            StockPriceProducer.stocks = YahooFinance.get(SYMBOLS.map { pair -> pair.first }.toTypedArray())
            while (isActive) {
                val newStockPrice = stockPriceProducer.produce()
                Log.i("StockPriceRetriever", "New stock price: $newStockPrice")
                allStockPrices.add(newStockPrice)
                _stockPrices.postValue(newStockPrice)
                delay(interval)
            }
            println("coroutine done")
        }
    }

}

class StockPriceProducer {

    companion object {

        lateinit var stocks: Map<String, Stock>
        var iterator: Iterator<Map.Entry<String, Stock>> = mapOf<String, Stock>().iterator()

    }

    @Synchronized
    fun produce(): StockPrice {
        if (!iterator.hasNext()) {
            iterator = stocks.iterator()
        }
        val stockSymbol = iterator.next().key
        val quote = (stocks[stockSymbol] ?: error("")).getQuote(true)
        return StockPrice(stockSymbol, quote.price.toDouble().roundTo(3))
    }

}
