package com.sundbybergsit.cromfortune.main.ui

import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.TransactionAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal

class AssetTransactionDialogTest {

    @Test
    fun `builder creates exact fractional crypto transaction`() {
        val bitcoin = AssetCatalog.cryptocurrencies.first()

        val transaction = buildAssetTransaction(
            bitcoin, TransactionAction.BUY, 1L, "0.00000001", "60123.456789", "0.15"
        )

        assertEquals(bitcoin.id, transaction.assetId)
        assertEquals(BigDecimal("0.00000001"), transaction.quantity)
        assertEquals(BigDecimal("60123.456789"), transaction.unitPrice)
        assertEquals(BigDecimal("0.15"), transaction.commissionFee)
    }

    @Test
    fun `builder enforces catalog quantity scale`() {
        assertThrows(IllegalArgumentException::class.java) {
            buildAssetTransaction(
                AssetCatalog.cryptocurrencies.first(),
                TransactionAction.BUY,
                1L,
                "0.000000001",
                "1",
                "0"
            )
        }
    }
}
