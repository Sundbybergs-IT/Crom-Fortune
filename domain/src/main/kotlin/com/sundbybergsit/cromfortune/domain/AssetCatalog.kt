package com.sundbybergsit.cromfortune.domain

import java.util.Currency

object AssetCatalog {

    val stocks: List<TradableAsset> = listOf(
        stock("AC.TO", "Air Canada", "CAD"),
        stock("ACST", "Acasti Pharma Inc.", "USD"),
        stock("ANOT.ST", "Anoto Group AB (publ)", "SEK"),
        stock("AIR.PA", "Airbus SE", "EUR"),
        stock("ASAB.ST", "Advanced Soltech Sweden AB (publ)", "SEK"),
        stock("ASSA-B.ST", "ASSA ABLOY AB (publ)", "SEK"),
        stock("AZELIO.ST", "Azelio AB (publ)", "SEK"),
        stock("BRK-B", "Berkshire Hathaway Inc. Class B", "USD"),
        stock("CRNO-B.ST", "Cereno Scientific AB (publ)", "SEK"),
        stock("CDI.PA", "Christian Dior SE", "EUR"),
        stock("CLOUD.OL", "Cloudberry Clean Energy AS", "NOK"),
        stock("COIN", "Coinbase Global, Inc.", "USD"),
        stock("EMBRAC-B.ST", "Embracer Group AB (publ)", "SEK"),
        stock("EQT.ST", "EQT AB", "SEK"),
        stock("EOLU-B.ST", "Eolus Vind AB (publ)", "SEK"),
        stock("FCX", "Freeport-McMoRan Inc.", "USD"),
        stock("FERRO.ST", "Ferroamp Elektronik AB (publ)", "SEK"),
        stock("FIA1S.HE", "Finnair Oyj", "EUR"),
        stock("GBK.ST", "Goodbye Kansas Group AB (publ)", "SEK"),
        stock("GGG.V", "G6 Materials Corp.", "CAD"),
        stock("GIGSEK.ST", "Gaming Innovation Group Inc.", "SEK"),
        stock("GOOGL", "Alphabet Inc. Class A", "USD"),
        stock("SHB-A.ST", "Svenska Handelsbanken AB (publ)", "SEK"),
        stock("HIMX", "Himax Technologies ADR", "USD"),
        stock("IBM", "International Business Machines Corporation", "USD"),
        stock("IMMR", "Immersion Corporation", "USD"),
        stock("INTC", "Intel Corporation", "USD"),
        stock("INVE-B.ST", "Investor AB (publ)", "SEK"),
        stock("IONQ", "IonQ, Inc.", "USD"),
        stock("IPCO.ST", "International Petroleum Corporation", "SEK"),
        stock("LHA.F", "Deutsche Lufthansa AG", "EUR"),
        stock("LPK.DE", "LPKF Laser & Electronics AG", "EUR"),
        stock("MC.PA", "LVMH Moet Hennessy Louis Vuitton SE", "EUR"),
        stock("MSFT", "Microsoft Corporation", "USD"),
        stock("MIPS.ST", "MIPS AB (publ)", "SEK"),
        stock("MOH.F", "LVMH Moët Hennessy - Louis Vuitton, Société Européenne", "EUR"),
        stock("NAS.OL", "Norwegian Air Shuttle ASA", "NOK"),
        stock("NOKIA-SEK.ST", "Nokia Corporation", "SEK"),
        stock("NVD.DE", "NVIDIA Corporation", "EUR"),
        stock("OXY", "Occidental Petroleum Corporation", "USD"),
        stock("POLYG.ST", "Polygiene AB (publ.)", "SEK"),
        stock("QNT", "Quantinuum", "USD"),
        stock("QS", "QuantumScape Corporation", "USD"),
        stock("RKT", "Rocket Companies, Inc.", "USD"),
        stock("SALT-B.ST", "SaltX Technology Holding AB (publ)", "SEK"),
        stock("SAND.ST", "Sandvik AB (publ)", "SEK"),
        stock("SAS.ST", "SAS AB (publ)", "SEK"),
        stock("SBB-B.ST", "Samhällsbyggnadsbolaget i Norden AB (publ)", "SEK"),
        stock("SHOT.ST", "Scandic Hotels Group AB (publ)", "SEK"),
        stock("SMCI", "Super Micro Computer, Inc.", "USD"),
        stock("SOLT.ST", "SolTech Energy Sweden AB", "SEK"),
        stock("SOS", "SOS ADR", "USD"),
        stock("SPCX", "Space Exploration Technologies Corp.", "USD"),
        stock("SWED-A.ST", "Swedbank AB (publ)", "SEK"),
        stock("TANGI.ST", "Tangiamo Touch Technology AB (publ)", "SEK"),
        stock("TSLA", "Tesla, Inc.", "USD"),
        stock("VUZI", "Vuzix Corporation", "USD")
    )

    val cryptocurrencies: List<TradableAsset> = listOf(
        cryptocurrency("BTC", "Bitcoin", "BTC-USD"),
        cryptocurrency("ETH", "Ethereum", "ETH-USD")
    )

    val assets: List<TradableAsset> = stocks + cryptocurrencies

    private val assetsById = assets.associateBy(TradableAsset::id)
    private val assetsByTypeAndSymbol = assets.associateBy { asset -> asset.type to asset.symbol }

    init {
        require(assetsById.size == assets.size) { "Asset IDs must be unique" }
        require(assets.map(TradableAsset::marketDataSymbol).distinct().size == assets.size) {
            "Asset market-data symbols must be unique"
        }
        require(assetsByTypeAndSymbol.size == assets.size) { "Asset symbols must be unique within each asset type" }
    }

    fun findById(id: String): TradableAsset? = assetsById[id]

    fun findBySymbol(type: AssetType, symbol: String): TradableAsset? =
        assetsByTypeAndSymbol[type to symbol]

    private fun stock(symbol: String, displayName: String, currencyCode: String) = TradableAsset(
        id = "stock:$symbol",
        symbol = symbol,
        displayName = displayName,
        type = AssetType.STOCK,
        quoteCurrency = Currency.getInstance(currencyCode),
        marketDataSymbol = symbol,
        quantityScale = 0
    )

    private fun cryptocurrency(symbol: String, displayName: String, marketDataSymbol: String) = TradableAsset(
        id = "crypto:$symbol",
        symbol = symbol,
        displayName = displayName,
        type = AssetType.CRYPTO,
        quoteCurrency = Currency.getInstance("USD"),
        marketDataSymbol = marketDataSymbol,
        quantityScale = 8
    )
}
