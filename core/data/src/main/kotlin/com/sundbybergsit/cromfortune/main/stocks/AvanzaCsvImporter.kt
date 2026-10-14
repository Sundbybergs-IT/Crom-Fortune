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

private data class RawSplitItem(
    val symbol: String,
    val dateInMillis: Long,
    val quantity: BigDecimal
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
        val rawSplitItems = mutableListOf<RawSplitItem>()

        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isBlank()) continue
            val columns = parseCsvLine(line, delimiter).map { it.trim() }
            val minIndexForType = maxOf(datumIndex, typIndex, vardepapperIndex, antalIndex)
            if (columns.size <= minIndexForType) {
                continue
            }

            val typeStr = columns[typIndex]
            if (typeStr.contains("Split", ignoreCase = true)) {
                val nameStr = columns[vardepapperIndex]
                val matchedAsset = matchAsset(nameStr)
                if (matchedAsset == null) {
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

                rawSplitItems.add(RawSplitItem(matchedAsset.symbol, dateInMillis, rawQuantity))
                continue
            }

            if (columns.size <= maxOf(datumIndex, typIndex, vardepapperIndex, antalIndex, kursIndex)) {
                continue
            }

            val nameStr = columns[vardepapperIndex]
            val matchedAsset = matchAsset(nameStr)
            if (matchedAsset == null) {
                // Ignore rights / BTU / BTA / IA / TO / TR / unmapped assets
                continue
            }

            val antalStr = columns[antalIndex].replace(',', '.')
            val rawQuantity = antalStr.toBigDecimalOrNull()
            if (rawQuantity == null) {
                continue
            }

            val action = when (typeStr) {
                "Köp", "Nyemission" -> TransactionAction.BUY
                "Sälj", "Inlösen", "Rensning av värdepapper" -> TransactionAction.SELL
                "Utdelning", "Utdelning värdepapper" -> TransactionAction.DIVIDEND
                "Teckning inlägg", "Tilldelning nyemission", "Värdepappersinsättning" -> TransactionAction.BUY
                "Värdepappersuttag" -> TransactionAction.SELL
                "Byte", "Byte värdepapper" -> if (rawQuantity >= BigDecimal.ZERO) TransactionAction.BUY else TransactionAction.SELL
                else -> continue
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

        val aggregatedSplits = rawSplitItems.groupBy { Pair(it.symbol, it.dateInMillis) }
            .flatMap { (_, group) ->
                val negativeSum = group.filter { it.quantity < BigDecimal.ZERO }.sumOf { it.quantity }.abs()
                val positiveSum = group.filter { it.quantity >= BigDecimal.ZERO }.sumOf { it.quantity }
                val symbol = group.first().symbol
                val dateInMillis = group.first().dateInMillis

                if (negativeSum > BigDecimal.ZERO && positiveSum > BigDecimal.ZERO) {
                    val reverse = positiveSum < negativeSum
                    val factorDecimal = if (reverse) {
                        negativeSum.divide(positiveSum, 0, RoundingMode.HALF_UP)
                    } else {
                        positiveSum.divide(negativeSum, 0, RoundingMode.HALF_UP)
                    }
                    val factorInt = factorDecimal.toInt().coerceAtLeast(StockSplit.MIN_QUANTITY)
                    listOf(StockSplit(reverse = reverse, dateInMillis = dateInMillis, name = symbol, quantity = factorInt))
                } else {
                    group.mapNotNull { single ->
                        val absQty = single.quantity.abs()
                        if (absQty >= BigDecimal(StockSplit.MIN_QUANTITY)) {
                            StockSplit(
                                reverse = single.quantity > BigDecimal.ZERO,
                                dateInMillis = dateInMillis,
                                name = symbol,
                                quantity = absQty.toInt()
                            )
                        } else {
                            null
                        }
                    }
                }
            }
            .groupBy { Triple(it.name, it.dateInMillis, it.reverse) }
            .map { (_, group) -> group.first() }

        val balancedTransactions = ensureChronologicalBalance(aggregatedTransactions, aggregatedSplits)

        return ImportResult(balancedTransactions, errors, aggregatedSplits)
    }

    private fun ensureChronologicalBalance(
        transactions: List<AssetTransaction>,
        splits: List<StockSplit>
    ): List<AssetTransaction> {
        val mutableTransactions = transactions.toMutableList()
        val splitsBySymbol = splits.groupBy { it.name }

        val assetIds = transactions.map { it.assetId }.distinct()
        for (assetId in assetIds) {
            val assetTransactions = mutableTransactions.filter { it.assetId == assetId }
            if (assetTransactions.isEmpty()) continue
            val symbol = assetTransactions.first().symbol
            val assetSplits = splitsBySymbol[symbol] ?: emptyList()

            var changed = true
            while (changed) {
                changed = false
                val currentAssetTransactions = mutableTransactions.filter { it.assetId == assetId }
                val allEvents = (currentAssetTransactions.map { Triple(it.dateInMillis, it.quantity, it as Any) } +
                    assetSplits.map { Triple(it.dateInMillis, it.quantity.toBigDecimal(), if (it.reverse) "REVERSE_SPLIT" else "SPLIT" as Any) })
                    .sortedWith(compareBy(
                        { it.first },
                        {
                            val obj = it.third
                            when (obj) {
                                is AssetTransaction -> if (obj.action == TransactionAction.BUY || obj.action == TransactionAction.DIVIDEND) 0 else 1
                                else -> 0
                            }
                        }
                    ))

                var balance = BigDecimal.ZERO
                for (event in allEvents) {
                    when (val obj = event.third) {
                        is AssetTransaction -> {
                            when (obj.action) {
                                TransactionAction.BUY -> balance += obj.quantity
                                TransactionAction.DIVIDEND -> {}
                                TransactionAction.SELL -> {
                                    if (balance < obj.quantity) {
                                        val deficit = obj.quantity - balance
                                        val syntheticBuy = AssetTransaction(
                                            assetId = obj.assetId,
                                            assetType = obj.assetType,
                                            symbol = obj.symbol,
                                            displayName = obj.displayName,
                                            quoteCurrencyCode = obj.quoteCurrency.currencyCode,
                                            action = TransactionAction.BUY,
                                            dateInMillis = obj.dateInMillis,
                                            unitPrice = BigDecimal.ZERO,
                                            commissionFee = BigDecimal.ZERO,
                                            quantity = deficit
                                        )
                                        mutableTransactions.add(syntheticBuy)
                                        changed = true
                                        break
                                    }
                                    balance -= obj.quantity
                                }
                            }
                        }
                        is String -> {
                            when (obj) {
                                "SPLIT" -> balance = balance.multiply(event.second)
                                "REVERSE_SPLIT" -> balance = balance.divide(event.second, 16, RoundingMode.DOWN)
                            }
                        }
                    }
                }
            }
        }

        return mutableTransactions.groupBy { Triple(it.assetId, it.action, it.dateInMillis) }
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
    }

    private fun matchAsset(nameStr: String): TradableAsset? {
        val trimmed = nameStr.trim()
        if (Regex("\\b(UR|TR|TO\\d*|BTU|BTA|IA|IR)\\b", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) {
            return null
        }
        val normalizedQuery = normalize(trimmed)
        val symbol = when {
            normalizedQuery.contentEquals("cereno scientific") || normalizedQuery.contentEquals("cereno scientific b") -> "CRNO-B.ST"
            normalizedQuery.contentEquals("saltx technology") || normalizedQuery.contentEquals("saltx") -> "SALT-B.ST"
            normalizedQuery.contentEquals("investor") -> "INVE-B.ST"
            normalizedQuery.contentEquals("ferroamp") -> "FERRO.ST"
            normalizedQuery.contentEquals("soltech energy") || normalizedQuery.contentEquals("soltech") || normalizedQuery.contentEquals("soltech energy sweden") -> "SOLT.ST"
            normalizedQuery.contentEquals("occidental petroleum") || normalizedQuery.contentEquals("oxy") -> "OXY"
            normalizedQuery.contentEquals("intel") -> "INTC"
            normalizedQuery.contentEquals("quantumscape") -> "QS"
            normalizedQuery.contentEquals("himax") -> "HIMX"
            normalizedQuery.contentEquals("vuzix") -> "VUZI"
            normalizedQuery.contentEquals("assa abloy") -> "ASSA-B.ST"
            normalizedQuery == "nas" -> "NAS.OL"
            normalizedQuery.contentEquals("sbb") -> "SBB-B.ST"
            normalizedQuery.contentEquals("nokia") -> "NOKIA-SEK.ST"
            normalizedQuery.contentEquals("airbus") -> "AIR.PA"
            normalizedQuery.contentEquals("polygiene") -> "POLYG.ST"
            normalizedQuery.contentEquals("coinbase") -> "COIN"
            normalizedQuery.contentEquals("azelio") -> "AZELIO.ST"
            normalizedQuery.contentEquals("lvmh") -> "MC.PA"
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
