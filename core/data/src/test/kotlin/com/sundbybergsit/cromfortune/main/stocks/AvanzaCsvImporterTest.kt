package com.sundbybergsit.cromfortune.main.stocks

import com.sundbybergsit.cromfortune.domain.TransactionAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.math.BigDecimal

class AvanzaCsvImporterTest {

    @Test
    fun `import - when avanza csv with supported buy and sell transactions - parses correctly`() {
        val csvContent = """
            Datum;Konto;Typ av transaktion;Värdepapper/beskrivning;Antal;Kurs;Belopp;Transaktionsvaluta;Courtage;Valutakurs;Instrumentvaluta;ISIN;Resultat
            2025-02-05;5011017;Köp;Cereno Scientific B;204;4,89;-1000,05;SEK;2,49;;SEK;SE0008241558;
            2025-01-30;5011017;Sälj;SaltX Technology Holding B;-100;19,0;1900;SEK;10;;SEK;SE0005308541;100
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        val transactions = importer.import(ByteArrayInputStream(csvContent.toByteArray()))

        assertEquals(2, transactions.size)

        val buyTx = transactions.find { it.action == TransactionAction.BUY }!!
        assertEquals("CRNO-B.ST", buyTx.symbol)
        assertEquals(BigDecimal("204"), buyTx.quantity)
        assertTrue(BigDecimal("4.89").compareTo(buyTx.unitPrice) == 0)
        assertTrue(BigDecimal("2.49").compareTo(buyTx.commissionFee) == 0)

        val sellTx = transactions.find { it.action == TransactionAction.SELL }!!
        assertEquals("SALT-B.ST", sellTx.symbol)
        assertEquals(BigDecimal("100"), sellTx.quantity)
        assertTrue(BigDecimal("19.0").compareTo(sellTx.unitPrice) == 0)
        assertTrue(BigDecimal("10").compareTo(sellTx.commissionFee) == 0)
    }

    @Test
    fun `import - when multiple purchases on same day - aggregates quantity weighted average price and courtage`() {
        val csvContent = """
            Datum;Konto;Typ av transaktion;Värdepapper/beskrivning;Antal;Kurs;Belopp;Transaktionsvaluta;Courtage;Valutakurs;Instrumentvaluta;ISIN;Resultat
            2025-02-05;5011017;Köp;Cereno Scientific B;100;4,00;-400,00;SEK;2,00;;SEK;SE0008241558;
            2025-02-05;5011017;Köp;Cereno Scientific B;100;4,00;-400,00;SEK;2,00;;SEK;SE0008241558;
            2025-02-05;5011017;Köp;Cereno Scientific B;100;6,00;-600,00;SEK;3,00;;SEK;SE0008241558;
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        val transactions = importer.import(ByteArrayInputStream(csvContent.toByteArray()))

        assertEquals(1, transactions.size)

        val buyTx = transactions[0]
        assertEquals(TransactionAction.BUY, buyTx.action)
        assertEquals("CRNO-B.ST", buyTx.symbol)
        assertEquals(BigDecimal("300"), buyTx.quantity) // 100 + 100 + 100 = 300
        assertTrue(BigDecimal("7.00").compareTo(buyTx.commissionFee) == 0) // 2 + 2 + 3 = 7
    }

    @Test
    fun `import - when dividend transaction - parses correctly`() {
        val csvContent = """
            Datum;Konto;Typ av transaktion;Värdepapper/beskrivning;Antal;Kurs;Belopp;Transaktionsvaluta;Courtage;Valutakurs;Instrumentvaluta;ISIN;Resultat
            2025-01-16;5011017;Utdelning;Occidental Petroleum;6;0,22;14,72;SEK;;;USD;US6745991058;
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        val transactions = importer.import(ByteArrayInputStream(csvContent.toByteArray()))

        assertEquals(1, transactions.size)

        val dividendTx = transactions[0]
        assertEquals(TransactionAction.DIVIDEND, dividendTx.action)
        assertEquals("OXY", dividendTx.symbol)
        assertEquals(BigDecimal("6"), dividendTx.quantity)
        assertTrue(BigDecimal("0.22").compareTo(dividendTx.unitPrice) == 0)
        assertEquals(BigDecimal("0"), dividendTx.commissionFee)
    }

    @Test
    fun `import - when invalid csv format without required headers - throws IllegalArgumentException`() {
        val invalidCsv = """
            Felaktig;Kolumn;Rubrik
            1,2,3
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        assertThrows(IllegalArgumentException::class.java) {
            importer.import(ByteArrayInputStream(invalidCsv.toByteArray()))
        }
    }

}
