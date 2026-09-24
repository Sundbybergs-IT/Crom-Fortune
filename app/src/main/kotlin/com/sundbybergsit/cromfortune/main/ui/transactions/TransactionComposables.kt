package com.sundbybergsit.cromfortune.main.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.sundbybergsit.cromfortune.domain.TransactionAction
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.ui.home.HomeViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date

internal data class PortfolioTransactionRow(
    val portfolioName: String,
    val symbol: String,
    val displayName: String,
    val action: TransactionAction,
    val dateInMillis: Long,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val currency: Currency
)

internal fun transactionRows(
    portfolios: Map<String, HomeViewModel.ViewState>
): List<PortfolioTransactionRow> = portfolios.flatMap { (portfolioName, state) ->
    state.items.flatMap { item ->
        val assetTransactions = item.assetEvents.mapNotNull { event -> event.transaction }.map { transaction ->
            PortfolioTransactionRow(
                portfolioName = portfolioName,
                symbol = transaction.symbol,
                displayName = transaction.displayName,
                action = transaction.action,
                dateInMillis = transaction.dateInMillis,
                quantity = transaction.quantity,
                unitPrice = transaction.unitPrice,
                currency = transaction.quoteCurrency
            )
        }
        val cromTransactions = item.legacyStockEvents.mapNotNull { event -> event.stockOrder }.map { order ->
            PortfolioTransactionRow(
                portfolioName = portfolioName,
                symbol = order.name,
                displayName = item.displayName,
                action = if (order.orderAction == "Buy") TransactionAction.BUY else TransactionAction.SELL,
                dateInMillis = order.dateInMillis,
                quantity = order.quantity,
                unitPrice = order.pricePerStock.toBigDecimal(),
                currency = Currency.getInstance(order.currency)
            )
        }
        assetTransactions + cromTransactions
    }
}.sortedByDescending(PortfolioTransactionRow::dateInMillis)

@Composable
fun Transactions(viewModel: HomeViewModel) {
    val portfolios by viewModel.portfoliosStateFlow.collectAsState()
    val rows = transactionRows(portfolios)
    val configuration = LocalConfiguration.current
    val locale = ConfigurationCompat.getLocales(configuration)[0]
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", locale)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transactions_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        if (rows.isEmpty()) {
            Text(
                text = stringResource(R.string.transactions_empty),
                modifier = Modifier.fillMaxWidth().padding(paddingValues).padding(top = 160.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(rows) { transaction ->
                    TransactionRow(transaction, dateFormat)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: PortfolioTransactionRow, dateFormat: SimpleDateFormat) {
    val currencyFormat = NumberFormat.getCurrencyInstance().apply { currency = transaction.currency }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${transaction.displayName} (${transaction.symbol})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(transaction.portfolioName, style = MaterialTheme.typography.bodySmall)
            Text(dateFormat.format(Date(transaction.dateInMillis)), style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(
                    if (transaction.action == TransactionAction.BUY) R.string.home_buy else R.string.home_sell
                ),
                fontWeight = FontWeight.Bold,
                color = if (transaction.action == TransactionAction.BUY) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Text("${transaction.quantity.stripTrailingZeros().toPlainString()} × ${currencyFormat.format(transaction.unitPrice)}")
        }
    }
}
