package yahoofinance

import yahoofinance.quotes.csv.FxQuotesRequest
import yahoofinance.quotes.csv.StockQuotesRequestV2
import yahoofinance.quotes.fx.FxQuote
import yahoofinance.quotes.query1v7.FxQuotesQuery1V7RequestV2
import yahoofinance.quotes.query1v7.StockQuotesQuery1V7RequestV2
import java.io.IOException

fun getFxHax(symbol: String): FxQuote {
    return if (YahooFinance.QUOTES_QUERY1V7_ENABLED.equals("true", ignoreCase = true)) {
        val request = FxQuotesQuery1V7RequestV2(symbol)
        request.singleResult
    } else {
        val request = FxQuotesRequest(symbol)
        request.singleResult
    }
}

@Throws(IOException::class)
fun get(symbols: Array<String>): Map<String, StockV2> {
    val query = Utils.join(symbols, ",")
    val includeHistorical = false
    val result: MutableMap<String, StockV2> = HashMap()
    if (YahooFinance.QUOTES_QUERY1V7_ENABLED.equals("true", ignoreCase = true)) {
        val request = StockQuotesQuery1V7RequestV2(query)
        val stocks = request.result
        for (stock in stocks) {
            result[stock.symbol] = stock
        }
    } else {
        val request = StockQuotesRequestV2(query)
        val quotes = request.result
        for (data in quotes) {
            val s = data.stock
            result[s.symbol] = s
        }
    }

    if (includeHistorical) {
        for (s in result.values) {
            s.history
        }
    }

    return result
}
