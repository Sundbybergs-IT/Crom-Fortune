package com.sundbybergsit.cromfortune.main.stocks

import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.TradableAsset
import com.sundbybergsit.cromfortune.domain.TransactionAction
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId

class AvanzaCsvImporter {

    fun import(inputStream: InputStream): List<AssetTransaction> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines()
        if (lines.isEmpty()) {
            throw IllegalArgumentException("Invalid CSV file: empty file")
        }

        val headerLine = lines[0]
        val delimiter = if (headerLine.contains(';')) ';' else ','
        val headers = parseCsvLine(headerLine, delimiter).map { it.trim() }

        val datumIndex = headers.indexOf("Datum")
        val typIndex = headers.indexOf("Typ av transaktion")
        val vardepapperIndex = headers.indexOf("Värdepapper/beskrivning")
        val antalIndex = headers.indexOf("Antal")
        val kursIndex = headers.indexOf("Kurs")
        val courtageIndex = headers.indexOf("Courtage")

        if (datumIndex == -1 || typIndex == -1 || vardepapperIndex == -1 || antalIndex == -1 || kursIndex == -1) {
            throw IllegalArgumentException("Invalid Avanza CSV format: missing required columns")
        }

        val rawTransactions = mutableListOf<AssetTransaction>()

        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isBlank()) continue
            val columns = parseCsvLine(line, delimiter).map { it.trim() }
            if (columns.size <= maxOf(datumIndex, typIndex, vardepapperIndex, antalIndex, kursIndex)) {
                continue
            }

            val typeStr = columns[typIndex]
            val action = when (typeStr) {
                "Köp" -> TransactionAction.BUY
                "Sälj" -> TransactionAction.SELL
                "Utdelning" -> TransactionAction.DIVIDEND
                else -> continue
            }

            val nameStr = columns[vardepapperIndex]
            val matchedAsset = matchAsset(nameStr) ?: continue

            val dateStr = columns[datumIndex]
            val dateInMillis = try {
                LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                continue
            }

            val antalStr = columns[antalIndex].replace(',', '.')
            val quantity = antalStr.toBigDecimalOrNull()
            if (quantity == null || quantity < BigDecimal.ZERO) {
                continue
            }
            if (action != TransactionAction.DIVIDEND && quantity == BigDecimal.ZERO) {
                continue
            }
            if (action != TransactionAction.DIVIDEND && quantity.scale().coerceAtLeast(0) > matchedAsset.quantityScale) {
                continue
            }

            val kursStr = columns[kursIndex].replace(',', '.')
            val unitPrice = kursStr.toBigDecimalOrNull()
            if (unitPrice == null || unitPrice < BigDecimal.ZERO) {
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

        return aggregatedTransactions
    }

    private fun matchAsset(nameStr: String): TradableAsset? {
        val normalizedQuery = normalize(nameStr)
        val symbol = when {
            normalizedQuery.contains("cereno scientific") -> "CRNO-B.ST"
            normalizedQuery.contains("saltx technology") -> "SALT-B.ST"
            normalizedQuery.contains("investor") -> "INVE-B.ST"
            normalizedQuery.contains("ferroamp") -> "FERRO.ST"
            normalizedQuery.contains("soltech energy") -> "SOLT.ST"
            normalizedQuery.contains("occidental petroleum") -> "OXY"
            normalizedQuery.contains("intel") -> "INTC"
            normalizedQuery.contains("quantumscape") -> "QS"
            normalizedQuery.contains("himax") -> "HIMX"
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
