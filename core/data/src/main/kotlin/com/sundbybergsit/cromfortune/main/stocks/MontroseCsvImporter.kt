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
            val line = lines[i]
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
            val action = when (typeStr) {
                "Köp" -> TransactionAction.BUY
                "Sälj" -> TransactionAction.SELL
                "Utdelning" -> TransactionAction.DIVIDEND
                else -> continue // Strictly ignore Insättning, Uttag, VP-överföring, Övrigt, etc.
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

            val antalStr = columns[antalIndex].replace(',', '.')
            val rawQuantity = antalStr.toBigDecimalOrNull()
            if (rawQuantity == null) {
                errors.add("Rad $i: Ogiltigt antal '$antalStr'")
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
                BigDecimal.ZERO
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

        val balancedTransactions = ensureChronologicalBalance(aggregatedTransactions)

        return ImportResult(balancedTransactions, errors)
    }

    private fun ensureChronologicalBalance(
        transactions: List<AssetTransaction>
    ): List<AssetTransaction> {
        val mutableTransactions = transactions.toMutableList()

        val assetIds = transactions.map { it.assetId }.distinct()
        for (assetId in assetIds) {
            var changed = true
            while (changed) {
                changed = false
                val currentAssetTransactions = mutableTransactions.filter { it.assetId == assetId }
                val allEvents = currentAssetTransactions.map { Triple(it.dateInMillis, it.quantity, it) }
                    .sortedWith(
                        compareBy(
                            { it.first },
                            { if (it.third.action == TransactionAction.BUY || it.third.action == TransactionAction.DIVIDEND) 0 else 1 }
                        ))

                var balance = BigDecimal.ZERO
                for (event in allEvents) {
                    val obj = event.third
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
    }

    private fun matchAsset(isinStr: String, tickerStr: String, nameStr: String): TradableAsset? {
        val combined = "$isinStr $tickerStr $nameStr"
        if (Regex("\\b(UR|TR|TO\\d*|BTU|BTA|IA|IR)\\b", RegexOption.IGNORE_CASE).containsMatchIn(combined)) {
            return null
        }

        val symbol = when (val normalizedTicker = tickerStr.trim().uppercase()) {
            "ASSA B" -> "ASSA-B.ST"
            "SALT B" -> "SALT-B.ST"
            "INVE B" -> "INVE-B.ST"
            "CRNO B" -> "CRNO-B.ST"
            "MC" -> "MC.PA"
            "FERRO" -> "FERRO.ST"
            "EQT" -> "EQT.ST"
            "TANGI" -> "TANGI.ST"
            "QNT.NAS" -> "QNT"
            "SPACEX" -> "SPCX"
            else -> normalizedTicker
        }

        return AssetCatalog.stocks.find { it.symbol.equals(symbol, ignoreCase = true) }
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
