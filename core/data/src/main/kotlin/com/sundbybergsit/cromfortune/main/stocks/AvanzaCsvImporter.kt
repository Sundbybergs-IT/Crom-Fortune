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

        val btaCosts = mutableMapOf<String, Pair<BigDecimal, BigDecimal>>()
        for (i in 1 until lines.size) {
            val line = lines[lines.size - i]
            if (line.isBlank()) continue
            val columns = parseCsvLine(line, delimiter).map { it.trim() }
            val minIndexForType = maxOf(datumIndex, typIndex, vardepapperIndex, antalIndex)
            if (columns.size <= minIndexForType) continue
            val nameStr = columns[vardepapperIndex]
            if (Regex("\\b(TR|BTU|BTA)\\b", RegexOption.IGNORE_CASE).containsMatchIn(nameStr)) {
                val antalStr = columns[antalIndex].replace(',', '.')
                val qty = antalStr.toBigDecimalOrNull()?.abs() ?: BigDecimal.ZERO
                val beloppIndex = headers.indexOf("Belopp")
                val beloppStr = if (beloppIndex != -1 && beloppIndex < columns.size) columns[beloppIndex].replace(',', '.') else ""
                val cost = beloppStr.toBigDecimalOrNull()?.abs() ?: (qty * (columns.getOrNull(kursIndex)?.replace(',', '.')?.toBigDecimalOrNull() ?: BigDecimal.ZERO))
                if (qty > BigDecimal.ZERO && cost > BigDecimal.ZERO) {
                    val symbol = if (nameStr.contains("SaltX", ignoreCase = true)) "SALT-B.ST" else null
                    if (symbol != null) {
                        val current = btaCosts[symbol] ?: (BigDecimal.ZERO to BigDecimal.ZERO)
                        btaCosts[symbol] = (current.first + qty) to (current.second + cost)
                    }
                }
            }
        }
        val btaAvgPrice = btaCosts.mapValues { (_, pair) ->
            if (pair.first > BigDecimal.ZERO) pair.second.divide(pair.first, 8, RoundingMode.HALF_UP).stripTrailingZeros() else BigDecimal.ZERO
        }

        for (i in 1 until lines.size) {
            val line = lines[lines.size-i]
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

            val isinIndex = headers.indexOf("ISIN")
            val isinStr = if (isinIndex != -1 && isinIndex < columns.size) columns[isinIndex] else ""
            val nameStr = columns[vardepapperIndex]
            val matchedAsset = matchAsset(nameStr, isinStr)
            if (matchedAsset == null) {
                // Ignore rights / BTU / BTA / IA / TO / TR / unmapped assets
                continue
            }

            val antalStr = columns[antalIndex].replace(',', '.')
            val rawQuantity = antalStr.toBigDecimalOrNull()
            if (rawQuantity == null) {
                continue
            }

            val action = when {
                typeStr == "Köp" -> TransactionAction.BUY
                typeStr in setOf("Sälj", "Rensning av värdepapper") -> TransactionAction.SELL
                typeStr in setOf("Utdelning", "Utdelning värdepapper") -> TransactionAction.DIVIDEND
                typeStr == "Byte värdepapper" && rawQuantity > BigDecimal.ZERO -> TransactionAction.BUY
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

            if (action != TransactionAction.DIVIDEND && quantity.scale()
                    .coerceAtLeast(0) > matchedAsset.quantityScale
            ) {
                errors.add("Rad $i: För många decimaler för ${matchedAsset.symbol}")
                continue
            }

            val kursStr = columns[kursIndex].replace(',', '.')
            val unitPrice = if (kursStr.isBlank()) {
                if (typeStr == "Byte värdepapper" && rawQuantity > BigDecimal.ZERO) {
                    btaAvgPrice[matchedAsset.symbol] ?: BigDecimal.ZERO
                } else if (action == TransactionAction.BUY && rawQuantity > BigDecimal.ZERO) {
                    calculateAverageUnitPrice(matchedAsset.symbol, dateInMillis, rawTransactions)
                } else {
                    BigDecimal.ZERO
                }
            } else {
                kursStr.toBigDecimalOrNull()
            }
            if (unitPrice == null || unitPrice < BigDecimal.ZERO) {
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
                    quantity = quantity
                )
            )
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
                        (positiveSum + negativeSum).divide(negativeSum, 0, RoundingMode.HALF_UP)
                    }
                    val factorInt = factorDecimal.toInt().coerceAtLeast(StockSplit.MIN_QUANTITY)

                    if (!reverse) {
                        val matchedAsset = AssetCatalog.stocks.find { it.symbol.equals(symbol, ignoreCase = true) }
                        if (matchedAsset != null) {
                            val preBalance = rawTransactions.filter { it.symbol.equals(symbol, ignoreCase = true) && it.dateInMillis < dateInMillis }
                                .fold(BigDecimal.ZERO) { acc, tx ->
                                    if (tx.action == TransactionAction.BUY) acc + tx.quantity
                                    else if (tx.action == TransactionAction.SELL) acc - tx.quantity
                                    else acc
                                }
                            if (preBalance < negativeSum) {
                                val deficit = negativeSum - preBalance
                                rawTransactions.add(
                                    AssetTransaction(
                                        assetId = matchedAsset.id,
                                        assetType = matchedAsset.type,
                                        symbol = matchedAsset.symbol,
                                        displayName = matchedAsset.displayName,
                                        quoteCurrencyCode = matchedAsset.quoteCurrency.currencyCode,
                                        action = TransactionAction.BUY,
                                        dateInMillis = dateInMillis - 86400000L,
                                        unitPrice = BigDecimal.ZERO,
                                        commissionFee = BigDecimal.ZERO,
                                        quantity = deficit
                                    )
                                )
                            }
                        }
                    }

                    listOf(
                        StockSplit(
                            reverse = reverse,
                            dateInMillis = dateInMillis,
                            name = symbol,
                            quantity = factorInt
                        )
                    )
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
            .toList()

        // Aggregate multiple transactions of the same asset and action on the same day
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

        val combinedTransactions = existingTransactions + aggregatedTransactions
        val combinedSplits = existingSplits + aggregatedSplits
        val balancedCombined = ChronologicalBalanceValidator.ensureChronologicalBalance(combinedTransactions, combinedSplits)
        val syntheticTransactions = balancedCombined.drop(combinedTransactions.size)
        val newTransactions = aggregatedTransactions + syntheticTransactions

        return ImportResult(newTransactions, errors, aggregatedSplits)
    }

    private fun matchAsset(nameStr: String, isinStr: String = ""): TradableAsset? {
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
        val trimmed = nameStr.trim()
        if (trimmed.isBlank()) return null
        if (Regex("\\b(UR|TR|TO\\d*|BTU|BTA|IA|IR)\\b", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) {
            return null
        }

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
            normalizedQuery == "lvmh" -> "MC.PA"
            normalizedQuery == "mips" -> "MIPS.ST"
            normalizedQuery == "nas" -> "NAS.OL"
            normalizedQuery == "nokia" -> "NOKIA-SEK.ST"
            normalizedQuery == "occidental petroleum" || normalizedQuery == "oxy" -> "OXY"
            normalizedQuery == "polygiene" -> "POLYG.ST"
            normalizedQuery == "quantumscape" -> "QS"
            normalizedQuery == "rocket companies" -> "RKT"
            normalizedQuery == "saltx technology holding b" -> "SALT-B.ST"
            normalizedQuery == "sas" -> "SAS.ST"
            normalizedQuery == "sbb" || normalizedQuery.startsWith("sbb") -> "SBB-B.ST"
            normalizedQuery == "scandic hotels group" -> "SHOT.ST"
            normalizedQuery == "soltech energy" || normalizedQuery == "soltech" || normalizedQuery ==
                "soltech energy sweden"
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

    private fun calculateAverageUnitPrice(symbol: String, dateInMillis: Long, transactions: List<AssetTransaction>): BigDecimal {
        val preceding = transactions.filter { it.symbol.equals(symbol, ignoreCase = true) && it.dateInMillis <= dateInMillis && it.action == TransactionAction.BUY && it.unitPrice > BigDecimal.ZERO }
        val totalQty = preceding.fold(BigDecimal.ZERO) { acc, tx -> acc + tx.quantity }
        val totalCost = preceding.fold(BigDecimal.ZERO) { acc, tx -> acc + (tx.quantity * tx.unitPrice) }
        return if (totalQty > BigDecimal.ZERO) {
            totalCost.divide(totalQty, 8, RoundingMode.HALF_UP).stripTrailingZeros()
        } else {
            BigDecimal.ZERO
        }
    }

}
