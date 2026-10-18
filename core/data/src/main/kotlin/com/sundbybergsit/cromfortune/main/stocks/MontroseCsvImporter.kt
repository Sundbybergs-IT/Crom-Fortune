package com.sundbybergsit.cromfortune.main.stocks

import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.TradableAsset
import com.sundbybergsit.cromfortune.domain.TransactionAction
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.ZoneId

class MontroseCsvImporter {

    fun import(
        inputStream: InputStream,
        existingTransactions: Collection<AssetTransaction> = emptyList(),
        existingSplits: Collection<StockSplit> = emptyList()
    ): ImportResult {
        val bytes = inputStream.readBytes()
        val errors = mutableListOf<String>()
        if (bytes.isEmpty()) {
            throw IllegalArgumentException("Invalid CSV file: empty file")
        }

        var text = String(bytes, Charsets.UTF_8).removePrefix("\uFEFF")
        var delimiter = if (text.contains(';')) ';' else ','
        var lines = text.lines()
        var headers = parseCsvLine(lines.firstOrNull().orEmpty(), delimiter).map { it.trim().removePrefix("\uFEFF") }

        var datumIndex = headers.indexOf("Datum")
        var typIndex = headers.indexOf("Typ")
        var vardepapperIndex = headers.indexOfFirst {
            it.contains("Värdepapper", ignoreCase = true) || it.contains(
                "Beskr",
                ignoreCase = true
            )
        }
        var tickerIndex = headers.indexOf("Ticker")
        var isinIndex = headers.indexOf("ISIN")
        var antalIndex = headers.indexOf("Antal")
        var kursIndex = headers.indexOf("Kurs")

        if (datumIndex == -1 || typIndex == -1 || antalIndex == -1 || kursIndex == -1) {
            text = String(bytes, Charset.forName("Windows-1252")).removePrefix("\uFEFF")
            delimiter = if (text.contains(';')) ';' else ','
            lines = text.lines()
            headers = parseCsvLine(lines.firstOrNull().orEmpty(), delimiter).map { it.trim().removePrefix("\uFEFF") }

            datumIndex = headers.indexOf("Datum")
            typIndex = headers.indexOf("Typ")
            vardepapperIndex = headers.indexOfFirst {
                it.contains("Värdepapper", ignoreCase = true) || it.contains(
                    "Beskr",
                    ignoreCase = true
                )
            }
            tickerIndex = headers.indexOf("Ticker")
            isinIndex = headers.indexOf("ISIN")
            antalIndex = headers.indexOf("Antal")
            kursIndex = headers.indexOf("Kurs")
        }

        val courtageIndex = headers.indexOf("Courtage")

        if (datumIndex == -1 || typIndex == -1 || antalIndex == -1 || kursIndex == -1) {
            throw IllegalArgumentException("Invalid Montrose CSV format: missing required columns")
        }

        val rawTransactions = mutableListOf<AssetTransaction>()

        for (i in 1 until lines.size) {
            val line = lines[lines.size-i]
            if (line.isBlank()) continue
            val columns = parseCsvLine(line, delimiter).map { it.trim() }
            val maxIndex = maxOf(
                datumIndex,
                typIndex,
                antalIndex,
                kursIndex,
                vardepapperIndex,
                tickerIndex,
                isinIndex,
                courtageIndex
            )
            if (columns.size <= maxIndex) {
                continue
            }

            val typeStr = columns[typIndex]
            val antalStr = columns[antalIndex].replace(',', '.')
            val rawQuantity = antalStr.toBigDecimalOrNull()
            val kursStr = if (kursIndex != -1 && kursIndex < columns.size) columns[kursIndex].replace(',', '.') else ""
            val unitPrice = if (kursStr.isBlank()) BigDecimal.ZERO else (kursStr.toBigDecimalOrNull() ?: BigDecimal.ZERO)

            val action = when {
                typeStr == "Köp" -> TransactionAction.BUY
                typeStr == "Sälj" -> TransactionAction.SELL
                typeStr == "Utdelning" -> TransactionAction.DIVIDEND
                typeStr == "Övrigt" && rawQuantity != null && rawQuantity > BigDecimal.ZERO && unitPrice == BigDecimal.ZERO -> TransactionAction.BUY
                else -> continue
            }

            if (rawQuantity == null) {
                if (typeStr != "Övrigt") {
                    errors.add("Rad $i: Ogiltigt antal '$antalStr'")
                }
                continue
            }

            val isinStr = if (isinIndex != -1 && isinIndex < columns.size) columns[isinIndex] else ""
            val tickerStr = if (tickerIndex != -1 && tickerIndex < columns.size) columns[tickerIndex] else ""
            val nameStr =
                if (vardepapperIndex != -1 && vardepapperIndex < columns.size) columns[vardepapperIndex] else ""

            val matchedAsset = matchAsset(isinStr, tickerStr, nameStr)
            if (matchedAsset == null) {
                // Ignore unmapped or rights/BTU/BTA/IA/TO/TR assets
                continue
            }

            val dateStr = columns[datumIndex]
            val dateInMillis = try {
                LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                errors.add("Rad $i: Ogiltigt datumformat '$dateStr'")
                continue
            }

            val quantity = when (action) {
                TransactionAction.BUY -> {
                    val absQty = rawQuantity.abs()
                    if (absQty <= BigDecimal.ZERO) {
                        errors.add("Rad $i: Ogiltigt köpantal")
                        continue
                    }
                    absQty
                }

                TransactionAction.SELL -> {
                    val absQty = rawQuantity.abs()
                    if (absQty <= BigDecimal.ZERO) {
                        errors.add("Rad $i: Ogiltigt säljantal")
                        continue
                    }
                    absQty
                }

                TransactionAction.DIVIDEND -> {
                    if (rawQuantity < BigDecimal.ZERO) {
                        errors.add("Rad $i: Ogiltigt utdelningsantal")
                        continue
                    }
                    rawQuantity
                }
            }

            if (action != TransactionAction.DIVIDEND && quantity.scale()
                    .coerceAtLeast(0) > matchedAsset.quantityScale
            ) {
                errors.add("Rad $i: För många decimaler för ${matchedAsset.symbol}")
                continue
            }

            if (unitPrice < BigDecimal.ZERO) {
                errors.add("Rad $i: Ogiltig kurs '$kursStr'")
                continue
            }

            val commissionFee =
                if (courtageIndex != -1 && courtageIndex < columns.size && columns[courtageIndex].isNotBlank()) {
                    columns[courtageIndex].replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
                } else {
                    BigDecimal.ZERO
                }

            rawTransactions.add(
                AssetTransaction(
                    assetId = matchedAsset.id,
                    assetType = matchedAsset.type,
                    symbol = matchedAsset.symbol,
                    displayName = matchedAsset.displayName,
                    quoteCurrencyCode = matchedAsset.quoteCurrency.currencyCode,
                    action = action,
                    dateInMillis = dateInMillis,
                    unitPrice = unitPrice,
                    commissionFee = commissionFee,
                    quantity = quantity,
                )
            )
        }

        val aggregatedTransactions = rawTransactions.groupBy { Triple(it.assetId, it.action, it.dateInMillis) }
            .map { (_, group) ->
                val first = group.first()
                if (first.action == TransactionAction.DIVIDEND) {
                    if (group.size == 1) {
                        first
                    } else {
                        val totalQuantity = group.sumOf { it.quantity }
                        val avgPrice = if (totalQuantity > BigDecimal.ZERO) {
                            group.sumOf { it.quantity * it.unitPrice }.divide(totalQuantity, 8, RoundingMode.HALF_UP)
                                .stripTrailingZeros()
                        } else {
                            first.unitPrice
                        }
                        first.copy(quantity = totalQuantity, unitPrice = avgPrice)
                    }
                } else {
                    val totalQuantity = group.sumOf { it.quantity }
                    val totalCost = group.sumOf { it.quantity * it.unitPrice }
                    val avgPrice = if (totalQuantity > BigDecimal.ZERO) {
                        totalCost.divide(totalQuantity, 8, RoundingMode.HALF_UP).stripTrailingZeros()
                    } else {
                        BigDecimal.ZERO
                    }
                    val totalCommission = group.sumOf { it.commissionFee }
                    first.copy(
                        quantity = totalQuantity,
                        unitPrice = avgPrice,
                        commissionFee = totalCommission
                    )
                }
            }

        val combined = existingTransactions + aggregatedTransactions
        val balancedCombined = ChronologicalBalanceValidator.ensureChronologicalBalance(combined, existingSplits)
        val syntheticTransactions = balancedCombined.drop(combined.size)
        val newTransactions = aggregatedTransactions + syntheticTransactions

        return ImportResult(newTransactions, errors)
    }

    private fun matchAsset(isinStr: String, tickerStr: String, nameStr: String): TradableAsset? {
        val combined = "$isinStr $tickerStr $nameStr"
        if (Regex("\\b(UR|TR|TO\\d*|BTU|BTA|IA|IR)\\b", RegexOption.IGNORE_CASE).containsMatchIn(combined)) {
            return null
        }

        if (isinStr.isNotBlank()) {
            val symbolByIsin = when (isinStr.trim().uppercase()) {
                "SE0005308541" -> "SALT-B.ST"
                "US4581401001" -> "INTC"
                "SE0015811963" -> "INVE-B.ST"
                "SE0008241558" -> "CRNO-B.ST"
                "US74767V1098" -> "QS"
                "US6745991058" -> "OXY"
                "US92921W3007" -> "VUZI"
                "US43289P1066" -> "HIMX"
                "FR0000121014" -> "MC.PA"
                "SE0012229920" -> "FERRO.ST"
                "SE0005392537" -> "SOLT.ST"
                else -> null
            }
            if (symbolByIsin != null) {
                val found = AssetCatalog.stocks.find { it.symbol.equals(symbolByIsin, ignoreCase = true) }
                if (found != null) return found
            }
        }

        val symbol = when (val normalizedTicker = tickerStr.trim().uppercase()) {
            "ASSA B" -> "ASSA-B.ST"
            "SALT B" -> "SALT-B.ST"
            "INVE B" -> "INVE-B.ST"
            "CRNO B" -> "CRNO-B.ST"
            "MC" -> "MC.PA"
            "FERRO" -> "FERRO.ST"
            "EQT" -> "EQT.ST"
            "RMS" -> "RMS.PA"
            "TANGI" -> "TANGI.ST"
            "QNT.NAS" -> "QNT"
            "SPACEX" -> "SPCX"
            "SOLT" -> "SOLT.ST"
            "SOLTECH" -> "SOLT.ST"
            "AZELIO" -> "AZELIO.ST"
            "POLYG" -> "POLYG.ST"
            "SHOT" -> "SHOT.ST"
            "GBK" -> "GBK.ST"
            "IPCO" -> "IPCO.ST"
            "SAND" -> "SAND.ST"
            "SAS" -> "SAS.ST"
            "SBB B" -> "SBB-B.ST"
            "SHB A" -> "SHB-A.ST"
            else -> normalizedTicker
        }

        val directMatch = AssetCatalog.stocks.find { it.symbol.equals(symbol, ignoreCase = true) }
        if (directMatch != null) {
            return directMatch
        }

        val stMatch = AssetCatalog.stocks.find { it.symbol.equals("$symbol.ST", ignoreCase = true) }
        if (stMatch != null) {
            return stMatch
        }

        return matchAssetByName(nameStr)
    }

    private fun matchAssetByName(nameStr: String): TradableAsset? {
        val trimmed = nameStr.trim()
        if (trimmed.isBlank()) return null
        val normalizedQuery = normalize(trimmed)
        val symbol = when {
            normalizedQuery == "air canada inc." -> "AC.TO"
            normalizedQuery == "airbus" -> "AIR.PA"
            normalizedQuery == "assa abloy" || normalizedQuery == "assa abloy b" -> "ASSA-B.ST"
            normalizedQuery == "azelio" || normalizedQuery == "azelio ab" -> "AZELIO.ST"
            normalizedQuery == "cereno scientific" || normalizedQuery == "cereno scientific b" -> "CRNO-B.ST"
            normalizedQuery == "cloudberry clean energy" -> "CLOUD.OL"
            normalizedQuery == "coinbase" -> "COIN"
            normalizedQuery == "deutsche lufthansa ag" -> "LHA.F"
            normalizedQuery == "eolus b" -> "EOLU-B.ST"
            normalizedQuery == "ferroamp" -> "FERRO.ST"
            normalizedQuery == "goodbye kansas group" -> "GBK.ST"
            normalizedQuery == "Grace Therapeutics, Inc." -> "GRCE"
            normalizedQuery == "himax" -> "HIMX"
            normalizedQuery == "intel" -> "INTC"
            normalizedQuery == "international petroleum corp." -> "IPCO.ST"
            normalizedQuery == "investor" || normalizedQuery == "investor b" -> "INVE-B.ST"
            normalizedQuery == "lvmh" || normalizedQuery == "lvmh moet hennessy louis vuitton" -> "MC.PA"
            normalizedQuery == "mips" -> "MIPS.ST"
            normalizedQuery == "nas" -> "NAS.OL"
            normalizedQuery == "nokia" -> "NOKIA-SEK.ST"
            normalizedQuery == "occidental petroleum" || normalizedQuery == "oxy" || normalizedQuery == "occidental petroleum corp" -> "OXY"
            normalizedQuery == "polygiene" -> "POLYG.ST"
            normalizedQuery == "hermes international" -> "RMS.PA"
            normalizedQuery == "quantumscape" -> "QS"
            normalizedQuery == "rocket companies" -> "RKT"
            normalizedQuery == "saltx technology holding b" -> "SALT-B.ST"
            normalizedQuery == "sas" -> "SAS.ST"
            normalizedQuery == "sbb" || normalizedQuery.startsWith("sbb") -> "SBB-B.ST"
            normalizedQuery == "scandic hotels group" -> "SHOT.ST"
            normalizedQuery == "soltech energy" || normalizedQuery == "soltech" || normalizedQuery ==
                "soltech energy sweden" || normalizedQuery == "soltech energy sweden ab"
                -> "SOLT.ST"
            normalizedQuery == "tesla" -> "TSLA"
            normalizedQuery == "vuzix" -> "VUZI"
            else -> null
        }
        if (symbol != null) {
            return AssetCatalog.stocks.find { it.symbol.equals(symbol, ignoreCase = true) }
        }

        for (asset in AssetCatalog.stocks) {
            val normalizedDisplayName = normalize(asset.displayName)
            val normalizedSymbol = normalize(asset.symbol)
            if (normalizedDisplayName == normalizedQuery ||
                normalizedDisplayName.startsWith(normalizedQuery) ||
                normalizedQuery.startsWith(normalizedDisplayName) ||
                normalizedSymbol == normalizedQuery
            ) {
                return asset
            }
        }
        return null
    }

    private fun normalize(str: String): String {
        return str.lowercase()
            .replace("(publ)", "")
            .replace("(publ.)", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when (c) {
                '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        cur.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }

                delimiter -> {
                    if (inQuotes) {
                        cur.append(c)
                    } else {
                        result.add(cur.toString())
                        cur.setLength(0)
                    }
                }

                else -> {
                    cur.append(c)
                }
            }
            i++
        }
        result.add(cur.toString())
        return result
    }

}
