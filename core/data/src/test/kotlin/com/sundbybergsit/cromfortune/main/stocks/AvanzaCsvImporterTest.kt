package com.sundbybergsit.cromfortune.main.stocks

import com.sundbybergsit.cromfortune.domain.TransactionAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            2025-01-25;5011017;Köp;SaltX Technology Holding B;100;18,0;-1800;SEK;10;;SEK;SE0005308541;
            2025-01-30;5011017;Sälj;SaltX Technology Holding B;-100;19,0;1900;SEK;10;;SEK;SE0005308541;100
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

        assertEquals(3, transactions.size)

        val buyTx = transactions.find { it.symbol == "CRNO-B.ST" && it.action == TransactionAction.BUY }!!
        assertEquals("CRNO-B.ST", buyTx.symbol)
        assertEquals(BigDecimal("204"), buyTx.quantity)
        assertTrue(BigDecimal("4.89").compareTo(buyTx.unitPrice) == 0)
        assertTrue(BigDecimal("2.49").compareTo(buyTx.commissionFee) == 0)

        val sellTx = transactions.find { it.symbol == "SALT-B.ST" && it.action == TransactionAction.SELL }!!
        assertEquals("SALT-B.ST", sellTx.symbol)
        assertEquals(BigDecimal("100"), sellTx.quantity)
        assertTrue(BigDecimal("19.0").compareTo(sellTx.unitPrice) == 0)
        assertTrue(BigDecimal("10").compareTo(sellTx.commissionFee) == 0)
    }

    @Test
    fun `import - when file with various stocks like Nokia SBB Investor - parses successfully`() {
        val csvContent = """
            Datum;Konto;Typ av transaktion;Värdepapper/beskrivning;Antal;Kurs;Belopp;Transaktionsvaluta;Courtage;Valutakurs;Instrumentvaluta;ISIN;Resultat
            2024-12-19;5011017;Köp;Investor B;10;290,7;-2914;SEK;7,27;;SEK;SE0015811963;
            2024-01-01;5011017;Köp;Nokia;360;30,-10800;SEK;10;;SEK;FI0009000681;
            2024-06-17;5011017;Sälj;Nokia;-360;38,335;13766;SEK;35;;SEK;FI0009000681;-3689,55493
            2024-01-01;5011017;Köp;SBB Norden B;500;5,-2500;SEK;10;;SEK;SE0009554454;
            2024-07-26;5011017;Sälj;SBB Norden B;-500;6,6;3292;SEK;8;;SEK;SE0009554454;146,334452
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))

        assertFalse(result.transactions.isEmpty())
        assertEquals(5, result.transactions.size)
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
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

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
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

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

    @Test
    fun `import - when avanza csv with reverse split transactions - parses correctly`() {
        val csvContent = """
            Datum;Konto;Typ av transaktion;Värdepapper/beskrivning;Antal;Kurs;Belopp;Transaktionsvaluta;Courtage;Valutakurs;Instrumentvaluta;ISIN;Resultat
            2025-12-17;5011017;Split inlägg värdepapper;Norwegian Air Shuttle;70;48,3245;;;;;SEK;NO0010196140;
            2025-12-17;5011017;Split uttag värdepapper;NAS;-7000;0,483245;;;;;SEK;NO9990196140;
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val splits = result.splits

        assertEquals(1, splits.size)

        val split1 = splits[0]
        assertTrue(split1.reverse)
        assertEquals("NAS.OL", split1.name)
        assertEquals(100, split1.quantity)
    }

    @Test
    fun `import - when avanza csv with split transactions - parses correctly`() {
        val csvContent = """
            Datum;Konto;Typ av transaktion;Värdepapper/beskrivning;Antal;Kurs;Belopp;Transaktionsvaluta;Courtage;Valutakurs;Instrumentvaluta;ISIN;Resultat
            2025-02-05;5011017;Split nytt värdepapper;Cereno Scientific B;2;;;;;;SEK;SE0008241558;
            2025-02-06;5011017;Split nytt värdepapper;Cereno Scientific B;-5;;;;;;SEK;SE0008241558;
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val splits = result.splits

        assertEquals(2, splits.size)

        val split1 = splits[0]
        assertTrue(split1.reverse)
        assertEquals("CRNO-B.ST", split1.name)
        assertEquals(2, split1.quantity)

        val split2 = splits[1]
        assertFalse(split2.reverse)
        assertEquals("CRNO-B.ST", split2.name)
        assertEquals(5, split2.quantity)
    }

    @Test
    fun `import - when avanza csv with rensning av vardepapper - parses correctly with synthetic buy and sell transaction`() {
        val csvContent = """
            Datum;Konto;Typ av transaktion;Värdepapper/beskrivning;Antal;Kurs;Belopp;Transaktionsvaluta;Courtage;Valutakurs;Instrumentvaluta;ISIN;Resultat
            2025-02-05;5011017;Rensning av värdepapper;Azelio;-1000;;;;;;SEK;;SE0012229920;
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

        assertEquals(2, transactions.size)

        val buyTx = transactions.find { it.action == TransactionAction.BUY }!!
        assertEquals("AZELIO.ST", buyTx.symbol)
        assertEquals(BigDecimal("1000"), buyTx.quantity)
        assertTrue(BigDecimal.ZERO.compareTo(buyTx.unitPrice) == 0)

        val sellTx = transactions.find { it.action == TransactionAction.SELL }!!
        assertEquals("AZELIO.ST", sellTx.symbol)
        assertEquals(BigDecimal("1000"), sellTx.quantity)
        assertTrue(BigDecimal.ZERO.compareTo(sellTx.unitPrice) == 0)
    }

    @Test
    fun `import - when avanza csv with azelio corporate actions and rensning - parses correctly`() {
        val csvContent = """
            Datum;Konto;Typ av transaktion;Värdepapper/beskrivning;Antal;Kurs;Belopp;Transaktionsvaluta;Courtage;Valutakurs;Instrumentvaluta;ISIN;Resultat
            2022-10-03;5011017;Tilldelning nyemission;AZELIO AB UR;500;;;;;;SEK;SE0018716573;
            2022-10-18;5011017;Teckning inlägg;AZELIO AB BTU;125;10,36;;;;;SEK;SE0018716581;
            2023-03-15;5011017;Byte;AZELIO AB;125;;;;;;SEK;SE0011973940;
            2026-01-27;5011017;Rensning av värdepapper;Azelio AB i konkurs;-125;;;;;;;SE0011973940;
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        val transactions = result.transactions

        assertFalse(transactions.isEmpty())
        assertEquals("AZELIO.ST", transactions.first().symbol)
    }

    @Test
    fun `import - when avanza csv with paired split for norwegian air shuttle - parses correctly`() {
        val csvContent = """
            Datum;Konto;Typ av transaktion;Värdepapper/beskrivning;Antal;Kurs;Belopp;Transaktionsvaluta;Courtage;Valutakurs;Instrumentvaluta;ISIN;Resultat
            2020-11-24;5011017;Köp;Norwegian Air Shuttle;7000;0,48;-3360;SEK;38;0,96;NOK;NO0010196140;
            2020-12-17;5011017;Split inlägg värdepapper;Norwegian Air Shuttle;70;48,32;;;;;SEK;NO0010196140;
            2020-12-17;5011017;Split uttag värdepapper;NAS;-7000;0,48;;;;;SEK;NO9990196140;
            2020-12-22;5011017;Sälj;Norwegian Air Shuttle;-70;62,8;-4396;SEK;37;0,95;NOK;NO0010196140;
        """.trimIndent()

        val importer = AvanzaCsvImporter()
        val result = importer.import(ByteArrayInputStream(csvContent.toByteArray()))
        assertEquals(1, result.splits.size)
        assertTrue(result.splits[0].reverse)
        assertEquals(100, result.splits[0].quantity)
    }

}
