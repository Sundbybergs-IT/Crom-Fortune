package com.sundbybergsit.cromfortune.main.stocks

import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.TradableAsset
import com.sundbybergsit.cromfortune.domain.TransactionAction
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.ZoneId

class MontroseCsvImporter {

    fun import(inputStream: InputStream): ImportResult {
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
        var vardepapperIndex = headers.indexOfFirst { it.contains("Värdepapper", ignoreCase = true) || it.contains("Beskr", ignoreCase = true) }
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
            vardepapperIndex = headers.indexOfFirst { it.contains("Värdepapper", ignoreCase = true) || it.contains("Beskr", ignoreCase = true) }
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
            val line = lines[i]
            if (line.isBlank()) continue
            val columns = parseCsvLine(line, delimiter).map { it.trim() }
            val maxIndex = maxOf(datumIndex, typIndex, antalIndex, kursIndex, vardepapperIndex, tickerIndex, isinIndex, courtageIndex)
            if (columns.size <= maxIndex) {
                continue
            }

            val typeStr = columns[typIndex]
            val action = when (typeStr) {
                "Köp" -> TransactionAction.BUY
                "Sälj" -> TransactionAction.SELL
                "Utdelning" -> TransactionAction.DIVIDEND
                else -> continue
            }

            val isinStr = if (isinIndex != -1 && isinIndex < columns.size) columns[isinIndex] else ""
            val tickerStr = if (tickerIndex != -1 && tickerIndex < columns.size) columns[tickerIndex] else ""
            val nameStr = if (vardepapperIndex != -1 && vardepapperIndex < columns.size) columns[vardepapperIndex] else ""

            val matchedAsset = matchAsset(isinStr, tickerStr, nameStr)
            if (matchedAsset == null) {
                errors.add("Rad $i: Värdepapper '$nameStr' (Ticker: '$tickerStr', ISIN: '$isinStr') stöds inte")
                continue
            }

            val dateStr = columns[datumIndex]
            val dateInMillis = try {
                LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                errors.add("Rad $i: Ogiltigt datumformat '$dateStr'")
                continue
            }

            val antalStr = columns[antalIndex].replace(',', '.')
            val rawQuantity = antalStr.toBigDecimalOrNull()
            if (rawQuantity == null) {
                errors.add("Rad $i: Ogiltigt antal '$antalStr'")
                continue
            }
            val quantity = when (action) {
                TransactionAction.BUY -> {
                    if (rawQuantity <= BigDecimal.ZERO) {
                        errors.add("Rad $i: Ogiltigt köpantal")
                        continue
                    }
                    rawQuantity
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

            if (action != TransactionAction.DIVIDEND && quantity.scale().coerceAtLeast(0) > matchedAsset.quantityScale) {
                errors.add("Rad $i: För många decimaler för ${matchedAsset.symbol}")
                continue
            }

            val kursStr = columns[kursIndex].replace(',', '.')
            val unitPrice = if (kursStr.isBlank()) {
                BigDecimal.ZERO
            } else {
                kursStr.toBigDecimalOrNull()
            }
            if (unitPrice == null || unitPrice < BigDecimal.ZERO) {
                errors.add("Rad $i: Ogiltig kurs '$kursStr'")
                continue
            }

            val commissionFee = if (courtageIndex != -1 && courtageIndex < columns.size && columns[courtageIndex].isNotBlank()) {
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
                            group.sumOf { it.quantity * it.unitPrice }.divide(totalQuantity, 8, RoundingMode.HALF_UP).stripTrailingZeros()
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

        return ImportResult(aggregatedTransactions, errors)
    }

    private fun matchAsset(isinStr: String, tickerStr: String, nameStr: String): TradableAsset? {
        val normalizedIsin = isinStr.trim().uppercase()
        val normalizedTicker = tickerStr.trim().uppercase()
        val normalizedName = normalize(nameStr)

        val symbol = when (normalizedIsin) {
            "SE0005308541" -> "SALT-B.ST"
            "SE0015811963" -> "INVE-B.ST"
            "SE0008241558" -> "CRNO-B.ST"
            "US6745991058" -> "OXY"
            "US43289P1066" -> "HIMX"
            "US4581401001" -> "INTC"
            "US92921W3007" -> "VUZI"
            "FR0000121014" -> "MC.PA"
            "SE0012229920" -> "FERRO.ST"
            "SE0012853455" -> "EQT.ST"
            "US74767V1098" -> "QS"
            "SE0023468194" -> "TANGI.ST"
            else -> when (normalizedTicker) {
                "SALT B", "SALT" -> "SALT-B.ST"
                "INVE B", "INVE" -> "INVE-B.ST"
                "CRNO B", "CRNO" -> "CRNO-B.ST"
                "OXY" -> "OXY"
                "HIMX" -> "HIMX"
                "INTC" -> "INTC"
                "VUZI" -> "VUZI"
                "MC" -> "MC.PA"
                "FERRO" -> "FERRO.ST"
                "EQT" -> "EQT.ST"
                "QS" -> "QS"
                "TANGI" -> "TANGI.ST"
                "FERRO BTA" -> "FERRO.ST"
                "SALT TR" -> "SALT-B.ST"
                "SALT BTA" -> "SALT-B.ST"
                else -> null
            }
        }

        if (symbol != null) {
            return AssetCatalog.stocks.find { it.symbol.equals(symbol, ignoreCase = true) }
        }

        val nameSymbol = when {
            normalizedName.contains("cereno scientific") -> "CRNO-B.ST"
            normalizedName.contains("saltx technology") || normalizedName.contains("saltx") -> "SALT-B.ST"
            normalizedName.contains("investor") -> "INVE-B.ST"
            normalizedName.contains("ferroamp") -> "FERRO.ST"
            normalizedName.contains("soltech energy") || normalizedName.contains("soltech") -> "SOLT.ST"
            normalizedName.contains("occidental petroleum") || normalizedName.contains("oxy") -> "OXY"
            normalizedName.contains("intel") -> "INTC"
            normalizedName.contains("quantumscape") -> "QS"
            normalizedName.contains("himax") -> "HIMX"
            normalizedName.contains("vuzix") -> "VUZI"
            normalizedName.contains("assa abloy") -> "ASSA-B.ST"
            normalizedName.contains("sbb") -> "SBB-B.ST"
            normalizedName.contains("nokia") -> "NOKIA-SEK.ST"
            normalizedName.contains("airbus") -> "AIR.PA"
            normalizedName.contains("polygiene") -> "POLYG.ST"
            normalizedName.contains("coinbase") -> "COIN"
            normalizedName.contains("azelio") -> "AZELIO.ST"
            normalizedName.contains("lvmh") -> "MC.PA"
            else -> null
        }
        if (nameSymbol != null) {
            return AssetCatalog.stocks.find { it.symbol.equals(nameSymbol, ignoreCase = true) }
        }

        for (asset in AssetCatalog.stocks) {
            val normalizedDisplayName = normalize(asset.displayName)
            val normalizedAssetSymbol = normalize(asset.symbol)
            if (normalizedDisplayName == normalizedName ||
                normalizedDisplayName.startsWith(normalizedName) ||
                normalizedName.startsWith(normalizedDisplayName) ||
                normalizedAssetSymbol == normalizedName) {
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
