package com.sundbybergsit.cromfortune.stocks

import java.util.*

data class StockPrice(val stockSymbol: String, val currency: Currency, val price: Double) {

    companion object {

        val CURRENCIES = arrayOf("CAD", "EUR", "NOK", "SEK", "USD")
        val SYMBOLS = arrayOf(Triple("AC.TO", "Air Canada", "CAD"), Triple("ACST", "Acasti Pharma Inc.", "USD"),
                Triple("ANOT.ST", "Anoto Group AB (publ)", "SEK"), Triple("ASSA-B.ST", "ASSA ABLOY AB (publ)", "SEK"),
                Triple("AZELIO.ST", "Azelio AB (publ)", "SEK"), Triple("BUBL.ST", "Bublar Group AB (publ)", "SEK"),
                Triple("CLOUD.OL", "Cloudberry Clean Energy AS", "NOK"), Triple("EOLU-B.ST", "Eolus Vind AB (publ)", "SEK"),
                Triple("FERRO.ST", "Ferroamp Elektronik AB (publ)", "SEK"), Triple("FIA1S.HE", "Finnair Oyj", "EUR"),
                Triple("GGG.V", "G6 Materials Corp.", "CAD"), Triple("SHB-A.ST","Svenska Handelsbanken AB (publ)", "SEK"),
                Triple("HIMX", "Himax Technologies, Inc.", "USD"),
                Triple("IMMR", "Immersion Corporation", "USD"),
                Triple("IPCO.ST", "International Petroleum Corporation", "SEK"), Triple("LHA.F", "Deutsche Lufthansa AG", "EUR"),
                Triple("LPK.DE", "LPKF Laser & Electronics AG", "EUR"), Triple("MIPS.ST", "MIPS AB (publ)", "SEK"),
                Triple("NAS.OL", "Norwegian Air Shuttle ASA", "NOK"), Triple("SALT-B.ST", "SaltX Technology Holding AB", "SEK"),
                Triple("SAND.ST", "Sandvik AB", "SEK"), Triple("SAS.ST", "SAS AB (publ)", "SEK"),
                Triple("SHOT.ST", "Scandic Hotels Group AB (publ)", "SEK"), Triple("SOLT.ST", "SolTech Energy Sweden AB (publ)", "SEK"),
                Triple("SWED-A.ST", "Swedbank AB (publ)", "SEK"), Triple("TANGI.ST", "Tangiamo Touch Technology AB (publ)", "SEK"),
                Triple("TSLA", "Tesla, Inc.", "USD"), Triple("VUZI", "Vuzix Corporation", "USD")
        )

    }

}
