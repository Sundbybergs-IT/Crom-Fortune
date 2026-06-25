package com.sundbybergsit.cromfortune.domain

import java.util.Currency

data class StockPrice(val stockSymbol: String, val currency: Currency, val price: Double) {

    companion object {

        val CURRENCIES = arrayOf("CAD", "EUR", "NOK", "SEK", "USD")
        val SYMBOLS = arrayOf(
            Triple("AC.TO", "Air Canada", "CAD"),
            Triple("ACST", "Acasti Pharma Inc.", "USD"),
            Triple("ANOT.ST", "Anoto Group AB (publ)", "SEK"),
            Triple("AIR.PA", "Airbus SE", "EUR"),
            Triple("ASAB.ST", "Advanced Soltech Sweden AB (publ)", "SEK"),
            Triple("ASSA-B.ST", "ASSA ABLOY AB (publ)", "SEK"),
            Triple("AZELIO.ST", "Azelio AB (publ)", "SEK"),
            Triple("BRK-B", "Berkshire Hathaway Inc. Class B", "USD"),
            Triple("CRNO-B.ST", "Cereno Scientific AB (publ)", "SEK"),
            Triple("CDI.PA", "Christian Dior SE", "EUR"),
            Triple("CLOUD.OL", "Cloudberry Clean Energy AS", "NOK"),
            Triple("COIN", "Coinbase Global, Inc.", "USD"),
            Triple("EMBRAC-B.ST", "Embracer Group AB (publ)", "SEK"),
            Triple("EQT.ST", "EQT AB", "SEK"),
            Triple("EOLU-B.ST", "Eolus Vind AB (publ)", "SEK"),
            Triple("FCX", "Freeport-McMoRan Inc.", "USD"),
            Triple("FERRO.ST", "Ferroamp Elektronik AB (publ)", "SEK"),
            Triple("FIA1S.HE", "Finnair Oyj", "EUR"),
            Triple("GBK.ST", "Goodbye Kansas Group AB (publ)", "SEK"),
            Triple("GGG.V", "G6 Materials Corp.", "CAD"),
            Triple("GIGSEK.ST", "Gaming Innovation Group Inc.", "SEK"),
            Triple("GOOGL", "Alphabet Inc. Class A", "USD"),
            Triple("SHB-A.ST", "Svenska Handelsbanken AB (publ)", "SEK"),
            Triple("HIMX", "Himax Technologies ADR", "USD"),
            Triple("IBM", "International Business Machines Corporation", "USD"),
            Triple("IMMR", "Immersion Corporation", "USD"),
            Triple("INTC", "Intel Corporation", "USD"),
            Triple("INVE-B.ST", "Investor AB (publ)", "SEK"),
            Triple("IONQ", "IonQ, Inc.", "USD"),
            Triple("IPCO.ST", "International Petroleum Corporation", "SEK"),
            Triple("LHA.F", "Deutsche Lufthansa AG", "EUR"),
            Triple("LPK.DE", "LPKF Laser & Electronics AG", "EUR"),
            Triple("MC.PA", "LVMH Moet Hennessy Louis Vuitton SE", "EUR"),
            Triple("MSFT", "Microsoft Corporation", "USD"),
            Triple("MIPS.ST", "MIPS AB (publ)", "SEK"),
            Triple("MOH.F", "LVMH Moët Hennessy - Louis Vuitton, Société Européenne", "EUR"),
            Triple("NAS.OL", "Norwegian Air Shuttle ASA", "NOK"),
            Triple("NOKIA-SEK.ST", "Nokia Corporation", "SEK"),
            Triple("NVD.DE", "NVIDIA Corporation", "EUR"),
            Triple("OXY", "Occidental Petroleum Corporation", "USD"),
            Triple("POLYG.ST", "Polygiene AB (publ.)", "SEK"),
            Triple("QNT", "Quantinuum", "USD"),
            Triple("QS", "QuantumScape Corporation", "USD"),
            Triple("RKT", "Rocket Companies, Inc.", "USD"),
            Triple("SALT-B.ST", "SaltX Technology Holding AB", "SEK"),
            Triple("SAND.ST", "Sandvik AB", "SEK"),
            Triple("SAS.ST", "SAS AB (publ)", "SEK"),
            Triple("SBB-B.ST", "Samhällsbyggnadsbolaget i Norden AB (publ)", "SEK"),
            Triple("SHOT.ST", "Scandic Hotels Group AB (publ)", "SEK"),
            Triple("SMCI", "Super Micro Computer, Inc.", "USD"),
            Triple("SOLT.ST", "SolTech Energy Sweden AB (publ)", "SEK"),
            Triple("SOS", "SOS ADR", "USD"),
            Triple("SPCX", "Space Exploration Technologies Corp.", "USD"),
            Triple("SWED-A.ST", "Swedbank AB (publ)", "SEK"),
            Triple("TANGI.ST", "Tangiamo Touch Technology AB (publ)", "SEK"),
            Triple("TSLA", "Tesla, Inc.", "USD"),
            Triple("VUZI", "Vuzix Corporation", "USD")
        )

    }

}
