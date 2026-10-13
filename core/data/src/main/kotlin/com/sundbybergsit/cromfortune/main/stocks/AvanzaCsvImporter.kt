package com.sundbybergsit.cromfortune.main.stocks

import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.TradableAsset
import com.sundbybergsit.cromfortune.domain.TransactionAction
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.ZoneId

data class ImportResult(
    val transactions: List<AssetTransaction>,
    val errors: List<String>,
    val splits: List<StockSplit> = emptyList()
)

class AvanzaCsvImporter {

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
        var typIndex = headers.indexOf("Typ av transaktion")
        var vardepapperIndex = headers.indexOf("Värdepapper/beskrivning")
        var antalIndex = headers.indexOf("Antal")
        var kursIndex = headers.indexOf("Kurs")

        if (datumIndex == -1 || typIndex == -1 || vardepapperIndex == -1 || antalIndex == -1 || kursIndex == -1) {
            text = String(bytes, Charset.forName("Windows-1252")).removePrefix("\uFEFF")
            delimiter = if (text.contains(';')) ';' else ','
            lines = text.lines()
            headers = parseCsvLine(lines.firstOrNull().orEmpty(), delimiter).map { it.trim().removePrefix("\uFEFF") }

            datumIndex = headers.indexOf("Datum")
            typIndex = headers.indexOf("Typ av transaktion")
            vardepapperIndex = headers.indexOf("Värdepapper/beskrivning")
            antalIndex = headers.indexOf("Antal")
            kursIndex = headers.indexOf("Kurs")
        }

        val courtageIndex = headers.indexOf("Courtage")

        if (datumIndex == -1 || typIndex == -1 || vardepapperIndex == -1 || antalIndex == -1 || kursIndex == -1) {
            throw IllegalArgumentException("Invalid Avanza CSV format: missing required columns")
        }

        val rawTransactions = mutableListOf<AssetTransaction>()
        val rawSplits = mutableListOf<StockSplit>()

        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isBlank()) continue
            val columns = parseCsvLine(line, delimiter).map { it.trim() }
            val minIndexForType = maxOf(datumIndex, typIndex, vardepapperIndex, antalIndex)
            if (columns.size <= minIndexForType) {
                continue
            }

            val typeStr = columns[typIndex]
            if (typeStr == "Split nytt värdepapper") {
                val nameStr = columns[vardepapperIndex]
                val matchedAsset = matchAsset(nameStr)
                if (matchedAsset == null) {
                    errors.add("Rad $i: Värdepapper '$nameStr' stöds inte")
                    continue
                }
                if (matchedAsset.type != AssetType.STOCK) {
                    errors.add("Rad $i: Splittar stöds endast för aktier")
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
                if (rawQuantity.abs() < BigDecimal(StockSplit.MIN_QUANTITY)) {
                    errors.add("Rad $i: Ogiltigt antal för split '$antalStr'")
                    continue
                }

                val reverse = rawQuantity < BigDecimal.ZERO
                val quantityInt = rawQuantity.abs().toInt()

                rawSplits.add(
                    StockSplit(
                        reverse = reverse,
                        dateInMillis = dateInMillis,
                        name = matchedAsset.symbol,
                        quantity = quantityInt
                    )
                )
                continue
            }

            if (typeStr == "Rensning av värdepapper") {
                val nameStr = columns[vardepapperIndex]
                val matchedAsset = matchAsset(nameStr)
                if (matchedAsset == null) {
                    errors.add("Rad $i: Värdepapper '$nameStr' stöds inte")
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
                val quantity = rawQuantity.abs()
                if (quantity <= BigDecimal.ZERO) {
                    errors.add("Rad $i: Ogiltigt antal för rensning")
                    continue
                }

                if (quantity.scale().coerceAtLeast(0) > matchedAsset.quantityScale) {
                    errors.add("Rad $i: För många decimaler för ${matchedAsset.symbol}")
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
                        action = TransactionAction.SELL,
                        dateInMillis = dateInMillis,
                        unitPrice = BigDecimal.ZERO,
                        commissionFee = commissionFee,
                        quantity = quantity
                    )
                )
                continue
            }

            if (columns.size <= maxOf(datumIndex, typIndex, vardepapperIndex, antalIndex, kursIndex)) {
                continue
            }

            val action = when (typeStr) {
                "Köp" -> TransactionAction.BUY
                "Sälj" -> TransactionAction.SELL
                "Utdelning" -> TransactionAction.DIVIDEND
                else -> continue
            }

            val nameStr = columns[vardepapperIndex]
            val matchedAsset = matchAsset(nameStr)
            if (matchedAsset == null) {
                errors.add("Rad $i: Värdepapper '$nameStr' stöds inte")
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
            val unitPrice = kursStr.toBigDecimalOrNull()
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
                    quantity = quantity
                )
            )
        }

        // Aggregate multiple transactions (including duplicates/similar transactions) of the same asset and action on the same day
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

        val aggregatedSplits = rawSplits.groupBy { Triple(it.name, it.dateInMillis, it.reverse) }
            .map { (_, group) ->
                group.first()
            }

        return ImportResult(aggregatedTransactions, errors, aggregatedSplits)
    }

    private fun matchAsset(nameStr: String): TradableAsset? {
        val normalizedQuery = normalize(nameStr)
        val symbol = when {
            normalizedQuery.contains("cereno scientific") -> "CRNO-B.ST"
            normalizedQuery.contains("saltx technology") || normalizedQuery.contains("saltx") -> "SALT-B.ST"
            normalizedQuery.contains("investor") -> "INVE-B.ST"
            normalizedQuery.contains("ferroamp") -> "FERRO.ST"
            normalizedQuery.contains("soltech energy") || normalizedQuery.contains("soltech") -> "SOLT.ST"
            normalizedQuery.contains("occidental petroleum") || normalizedQuery.contains("oxy") -> "OXY"
            normalizedQuery.contains("intel") -> "INTC"
            normalizedQuery.contains("quantumscape") -> "QS"
            normalizedQuery.contains("himax") -> "HIMX"
            normalizedQuery.contains("vuzix") -> "VUZI"
            normalizedQuery.contains("assa abloy") -> "ASSA-B.ST"
            normalizedQuery.contains("sbb") -> "SBB-B.ST"
            normalizedQuery.contains("nokia") -> "NOKIA-SEK.ST"
            normalizedQuery.contains("airbus") -> "AIR.PA"
            normalizedQuery.contains("polygiene") -> "POLYG.ST"
            normalizedQuery.contains("coinbase") -> "COIN"
            normalizedQuery.contains("azelio") -> "AZELIO.ST"
            normalizedQuery.contains("lvmh") -> "MC.PA"
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
                normalizedSymbol == normalizedQuery) {
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
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        cur.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    result.add(cur.toString())
                    cur.setLength(0)
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
