package com.sundbybergsit.cromfortune.main.stocks

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.TransactionAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class MontroseCsvImporterTest {

    @Test
    fun `import - when montrose csv with supported buy and sell transactions - parses correctly`() {
        val csvContent = """
            Datum,Typ,Konto,Värdepapper / Beskr.,Ticker,ISIN,Antal,Kurs,Kursvaluta,Totalt,Totalvaluta,Courtage,Courtagevaluta
            2026-10-12,Köp,2149201,SaltX Technology Holding B,SALT B,SE0005308541,1000,4.345,SEK,-4352,SEK,6.52,SEK
            2026-09-01,Köp,2149201,Investor B,INVE B,SE0015811963,10,400.0,SEK,-4000,SEK,1.0,SEK
            2026-10-02,Sälj,2149201,Investor B,INVE B,SE0015811963,-2,403.35,SEK,805,SEK,1.21,SEK
        """.trimIndent()

        val importer = MontroseCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

        assertEquals(3, transactions.size)

        val buyTx = transactions.find { it.symbol == "SALT-B.ST" && it.action == TransactionAction.BUY }!!
        assertEquals("SALT-B.ST", buyTx.symbol)
        assertEquals(BigDecimal("1000"), buyTx.quantity)
        assertTrue(BigDecimal("4.345").compareTo(buyTx.unitPrice) == 0)
        assertTrue(BigDecimal("6.52").compareTo(buyTx.commissionFee) == 0)

        val sellTx = transactions.find { it.symbol == "INVE-B.ST" && it.action == TransactionAction.SELL }!!
        assertEquals("INVE-B.ST", sellTx.symbol)
        assertEquals(BigDecimal("2"), sellTx.quantity)
        assertTrue(BigDecimal("403.35").compareTo(sellTx.unitPrice) == 0)
        assertTrue(BigDecimal("1.21").compareTo(sellTx.commissionFee) == 0)
    }

    @Test
    fun `import - when dividend transaction - parses correctly`() {
        val csvContent = """
            Datum,Typ,Konto,Värdepapper / Beskr.,Ticker,ISIN,Antal,Kurs,Kursvaluta,Totalt,Totalvaluta,Courtage,Courtagevaluta
            2026-05-13,Utdelning,2149201,Investor B,INVE B,SE0015811963,186,4,SEK,744,SEK,,
        """.trimIndent()

        val importer = MontroseCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

        assertEquals(1, transactions.size)

        val dividendTx = transactions[0]
        assertEquals(TransactionAction.DIVIDEND, dividendTx.action)
        assertEquals("INVE-B.ST", dividendTx.symbol)
        assertEquals(BigDecimal("186"), dividendTx.quantity)
        assertTrue(BigDecimal("4").compareTo(dividendTx.unitPrice) == 0)
        assertEquals(BigDecimal("0"), dividendTx.commissionFee)
    }

    @Test
    fun `import - when multiple buy and sell on same day - aggregates quantity weighted average price and courtage`() {
        val csvContent = """
            Datum,Typ,Konto,Värdepapper / Beskr.,Ticker,ISIN,Antal,Kurs,Kursvaluta,Totalt,Totalvaluta,Courtage,Courtagevaluta
            2025-08-21,Köp,2364560,Intel,INTC,US4581401001,150,22.95,USD,-33194.94,SEK,49.72,SEK
            2025-08-21,Sälj,2149201,Intel,INTC,US4581401001,-150,23.05,USD,33145.76,SEK,49.79,SEK
            2025-08-21,Köp,2149201,Intel,INTC,US4581401001,150,22.99,USD,-33256.78,SEK,49.81,SEK
        """.trimIndent()

        val importer = MontroseCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

        assertEquals(2, transactions.size)

        val buyTx = transactions[0]
        assertEquals(TransactionAction.BUY, buyTx.action)
        assertEquals("INTC", buyTx.symbol)
        assertEquals(BigDecimal("300"), buyTx.quantity)
        assertEquals(BigDecimal("22.97"), buyTx.unitPrice)
        assertEquals(BigDecimal("99.53").toDouble(), buyTx.commissionFee.toDouble(), 0.1)
        val sellTx = transactions[1]
        assertEquals(TransactionAction.SELL, sellTx.action)
        assertEquals("INTC", sellTx.symbol)
        assertEquals(BigDecimal("150"), sellTx.quantity)
        assertEquals(BigDecimal("23.05"), sellTx.unitPrice)
        assertEquals(BigDecimal("49.79").toDouble(), sellTx.commissionFee.toDouble(), 0.1)
    }

    @Test
    fun `import - when multiple purchases on same day - aggregates quantity weighted average price and courtage`() {
        val csvContent = """
            Datum,Typ,Konto,Värdepapper / Beskr.,Ticker,ISIN,Antal,Kurs,Kursvaluta,Totalt,Totalvaluta,Courtage,Courtagevaluta
            2026-10-02,Köp,2149201,SaltX Technology Holding B,SALT B,SE0005308541,1000,4.9,SEK,-4907,SEK,7.35,SEK
            2026-10-02,Köp,2149201,SaltX Technology Holding B,SALT B,SE0005308541,500,4.88,SEK,-2444,SEK,3.66,SEK
        """.trimIndent()

        val importer = MontroseCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

        assertEquals(1, transactions.size)

        val buyTx = transactions[0]
        assertEquals(TransactionAction.BUY, buyTx.action)
        assertEquals("SALT-B.ST", buyTx.symbol)
        assertEquals(BigDecimal("1500"), buyTx.quantity)
        assertTrue(BigDecimal("11.01").compareTo(buyTx.commissionFee) == 0) // 7.35 + 3.66 = 11.01
    }

    @Test
    fun `import - when various stock transactions including foreign stocks - parses successfully`() {
        val csvContent = """
            Datum,Typ,Konto,Värdepapper / Beskr.,Ticker,ISIN,Antal,Kurs,Kursvaluta,Totalt,Totalvaluta,Courtage,Courtagevaluta
            2025-01-01,Köp,2149201,Intel,INTC,US4581401001,200,20.0,USD,4000,SEK,10,SEK
            2025-08-21,Sälj,2149201,Intel,INTC,US4581401001,-150,23.05,USD,33145.76,SEK,49.79,SEK
            2025-01-01,Köp,2149201,LVMH Moët Hennessy Louis Vuitton,MC,FR0000121014,10,400.0,EUR,4000,SEK,10,SEK
            2025-08-20,Sälj,2149201,LVMH Moët Hennessy Louis Vuitton,MC,FR0000121014,-7,495.2,EUR,38634.64,SEK,58.04,SEK
            2025-04-16,Utdelning,2149201,Occidental Petroleum Corp,OXY,US6745991058,6,0.24,USD,14.11,SEK,,
        """.trimIndent()

        val importer = MontroseCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

        assertEquals(5, transactions.size)
        assertTrue(transactions.any { it.symbol == "INTC" && it.action == TransactionAction.SELL })
        assertTrue(transactions.any { it.symbol == "MC.PA" && it.action == TransactionAction.SELL })
        assertTrue(transactions.any { it.symbol == "OXY" && it.action == TransactionAction.DIVIDEND })
    }

    @Test
    fun `import - when montrose csv with intel sell on 2025-03-17 - parses action and price correctly`() {
        val csvContent = """
            Datum,Typ,Konto,Värdepapper / Beskr.,Ticker,ISIN,Antal,Kurs,Kursvaluta,Totalt,Totalvaluta,Courtage,Courtagevaluta
            2025-03-17,Sälj,2149201,Intel,INTC,US4581401001,-40,26.03,USD,10498.86,SEK,10.51,SEK
        """.trimIndent()

        val importer = MontroseCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

        assertEquals(2, transactions.size)
        val sellTx = transactions.find { it.action == TransactionAction.SELL }!!
        assertEquals(TransactionAction.SELL, sellTx.action)
        assertEquals(BigDecimal("40"), sellTx.quantity)
        assertEquals(BigDecimal("26.03"), sellTx.unitPrice)
    }

    @Test
    fun `import - when invalid csv format without required headers - throws IllegalArgumentException`() {
        val invalidCsv = """
            Felaktig,Kolumn,Rubrik
            1,2,3
        """.trimIndent()

        val importer = MontroseCsvImporter()
        assertThrows(IllegalArgumentException::class.java) {
            importer.import(ByteArrayInputStream(invalidCsv.toByteArray()))
        }
    }

}
