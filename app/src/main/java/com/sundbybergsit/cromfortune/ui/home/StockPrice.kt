package com.sundbybergsit.cromfortune.ui.home

data class StockPrice(val name: String, val price: Double) {

    companion object {

        val CURRENCIES = arrayOf("CAD", "EUR", "NOK", "SEK", "USD")
        val SYMBOLS = arrayOf(Pair("AC.TO", "Air Canada"), Pair("ACST", "Acasti Pharma Inc."),
                Pair("ANOT.ST", "Anoto Group AB (publ)"), Pair("ASSA-B.ST", "ASSA ABLOY AB (publ)"),
                Pair("AZELIO.ST", "Azelio AB (publ)"), Pair("BUBL.ST", "Bublar Group AB (publ)"),
                Pair("CLOUD.OL", "Cloudberry Clean Energy AS"), Pair("EOLU-B.ST", "Eolus Vind AB (publ)"),
                Pair("FERRO.ST", "Ferroamp Elektronik AB (publ)"), Pair("FIA1S.HE", "Finnair Oyj"),
                Pair("GGG.V", "G6 Materials Corp."), Pair("HIMX", "Himax Technologies, Inc."),
                Pair("IPCO.ST", "International Petroleum Corporation"), Pair("LHA.F", "Deutsche Lufthansa AG"),
                Pair("LPK.DE", "LPKF Laser & Electronics AG"), Pair("MIPS.ST", "MIPS AB (publ)"),
                Pair("NAS.OL", "Norwegian Air Shuttle ASA"), Pair("SALT-B.ST", "SaltX Technology Holding AB"),
                Pair("SAND.ST", "Sandvik AB"), Pair("SAS.ST", "SAS AB (publ)"),
                Pair("SHOT.ST", "Scandic Hotels Group AB (publ)"), Pair("SOLT.ST", "SolTech Energy Sweden AB (publ)"),
                Pair("SWED-A.ST", "Swedbank AB (publ)"), Pair("TANGI.ST", "Tangiamo Touch Technology AB (publ)"),
                Pair("TSLA", "Tesla, Inc.")
        )

    }

}
