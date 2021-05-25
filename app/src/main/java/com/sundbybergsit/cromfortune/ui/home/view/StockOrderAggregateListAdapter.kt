package com.sundbybergsit.cromfortune.ui.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.stocks.StockPriceListener
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.ui.AdapterItem
import com.sundbybergsit.cromfortune.ui.AdapterItemDiffUtil
import com.sundbybergsit.cromfortune.ui.home.StockAggregateAdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockAggregateHeaderAdapterItem
import java.text.NumberFormat
import java.util.*

internal class StockOrderAggregateListAdapter(
    private val stockClickListener: StockClickListener,
    private val readOnly: Boolean,
) :
    ListAdapter<AdapterItem, RecyclerView.ViewHolder>(AdapterItemDiffUtil<AdapterItem>()), StockPriceListener {

    private lateinit var stockRemoveClickListener: StockRemoveClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.listrow_stock_header -> StockOrderAggregateHeaderViewHolder(
                stockPriceListener = this,
                itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                context = parent.context
            )
            R.layout.listrow_stock_item -> StockOrderAggregateViewHolder(
                stockClickListener = stockClickListener,
                stockRemoveClickListener = stockRemoveClickListener,
                stockPriceListener = this,
                itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                context = parent.context,
                readOnly = readOnly
            )
            else -> throw IllegalArgumentException("Unexpected viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is StockOrderAggregateHeaderViewHolder -> {
                holder.bind(item as StockAggregateHeaderAdapterItem)
            }
            is StockOrderAggregateViewHolder -> {
                holder.bind(item as StockAggregateAdapterItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)!!) {
        is StockAggregateHeaderAdapterItem -> {
            R.layout.listrow_stock_header
        }
        is StockAggregateAdapterItem -> {
            R.layout.listrow_stock_item
        }
        else -> {
            throw IllegalArgumentException("Unexpected item: " + item.javaClass.canonicalName)
        }
    }

    fun setListener(stockRemoveClickListener: StockRemoveClickListener) {
        this.stockRemoveClickListener = stockRemoveClickListener
    }

    internal class StockOrderAggregateHeaderViewHolder(
        private val stockPriceListener: StockPriceListener,
        itemView: View,
        private val context: Context,
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: StockAggregateHeaderAdapterItem) {
            var count = 0.0
            val currencyRates = (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES)
                .currencyRates.toList()
            for (stockOrderAggregate in item.stockOrderAggregates.toList()) {
                for (currencyRate in currencyRates) {
                    if (currencyRate.iso4217CurrencySymbol == stockOrderAggregate.currency.currencyCode) {
                        count += (stockOrderAggregate.getProfit(
                            stockPriceListener.getStockPrice(
                                stockOrderAggregate.stockSymbol
                            ).price
                        )) * currencyRate.rateInSek
                        break
                    }
                }
            }
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            format.currency = Currency.getInstance("SEK")
            format.maximumFractionDigits = 2
            itemView.requireViewById<TextView>(R.id.textView_listrowStockHeader_totalProfit).text = format.format(count)
            itemView.requireViewById<TextView>(R.id.textView_listrowStockHeader_totalProfit).setTextColor(
                ContextCompat.getColor(
                    context, if (count >= 0.0) {
                        R.color.colorProfit
                    } else {
                        R.color.colorLoss
                    }
                )
            )
            val overflowMenuImageView =
                itemView.requireViewById<ImageView>(R.id.imageView_listrowStockHeader_overflowMenu)
            val overflowMenu = PopupMenu(context, overflowMenuImageView)
            overflowMenuImageView.setOnClickListener { overflowMenu.show() }
            overflowMenu.inflate(R.menu.home_listrowheader_actions)
            overflowMenu.setOnMenuItemClickListener(
                PopupMenuListener(context)
            )
        }

        class PopupMenuListener(
            private val context: Context,
        ) : PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem?): Boolean {
                return if (item?.itemId == R.id.action_sort_alphabetical) {
                    Toast.makeText(context, R.string.generic_to_do, Toast.LENGTH_LONG).show()
                    true
                } else {
                    false
                }
            }

        }

    }

    internal class StockOrderAggregateViewHolder(
        private val context: Context,
        private val stockPriceListener: StockPriceListener,
        private val stockClickListener: StockClickListener,
        private val stockRemoveClickListener: StockRemoveClickListener,
        itemView: View,
        private val readOnly: Boolean,
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: StockAggregateAdapterItem) {
            itemView.requireViewById<TextView>(R.id.textView_listrowStockItem_quantity).text =
                item.stockOrderAggregate.getQuantity().toString()
            @SuppressLint("SetTextI18n")
            itemView.requireViewById<TextView>(R.id.textView_listrowStockItem_name).text =
                item.stockOrderAggregate.displayName
            val acquisitionValue = item.stockOrderAggregate.getAcquisitionValue()
            val stockCurrencyFormat: NumberFormat = NumberFormat.getCurrencyInstance()
            if (acquisitionValue < 1) {
                stockCurrencyFormat.maximumFractionDigits = 3
            } else {
                stockCurrencyFormat.maximumFractionDigits = 2
            }
            stockCurrencyFormat.currency = item.stockOrderAggregate.currency
            val swedishCurrencyFormat: NumberFormat = NumberFormat.getCurrencyInstance()
            if (acquisitionValue < 1) {
                stockCurrencyFormat.maximumFractionDigits = 3
            } else {
                stockCurrencyFormat.maximumFractionDigits = 2
            }
            swedishCurrencyFormat.currency = Currency.getInstance("SEK")
            itemView.setOnClickListener {
                stockClickListener.onClick(item.stockOrderAggregate.stockSymbol, readOnly)
            }
            val overflowMenuImageView = itemView.requireViewById<View>(R.id.imageView_listrowStockItem_overflowMenu)
            if (readOnly) {
                itemView.requireViewById<View>(R.id.button_listrowStockItem_buy).visibility = View.INVISIBLE
                itemView.requireViewById<View>(R.id.button_listrowStockItem_sell).visibility = View.INVISIBLE
                itemView.requireViewById<View>(R.id.imageButton_listrowStockItem_muteUnmute).visibility = View.INVISIBLE
                overflowMenuImageView.visibility = View.INVISIBLE
            }
            itemView.requireViewById<Button>(R.id.button_listrowStockItem_buy).setOnClickListener {
                Toast.makeText(context, R.string.generic_error_not_supported, Toast.LENGTH_LONG).show()
            }
            itemView.requireViewById<Button>(R.id.button_listrowStockItem_sell).setOnClickListener {
                Toast.makeText(context, R.string.generic_error_not_supported, Toast.LENGTH_LONG).show()
            }
            itemView.requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute).setImageDrawable(
                if (item.muted) {
                    ContextCompat.getDrawable(context, R.drawable.ic_fas_bell_slash)
                } else {
                    ContextCompat.getDrawable(context, R.drawable.ic_fas_bell)
                }
            )
            itemView.requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute).setOnClickListener {
                if (item.muted) {
                    StockMuteSettingsRepository.unmute(item.stockOrderAggregate.stockSymbol)
                    itemView.requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute)
                        .setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_fas_bell))
                } else {
                    StockMuteSettingsRepository.mute(item.stockOrderAggregate.stockSymbol)
                    itemView.requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute)
                        .setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_fas_bell_slash))
                }
                item.muted = !item.muted
            }
            itemView.requireViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue).text =
                stockCurrencyFormat.format(acquisitionValue)
            val currentStockPrice = stockPriceListener.getStockPrice(item.stockOrderAggregate.stockSymbol).price
            var profitInSek = 0.0
            val currencyRates = (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES)
                .currencyRates.toList()
            for (currencyRate in currencyRates) {
                if (currencyRate.iso4217CurrencySymbol == item.stockOrderAggregate.currency.currencyCode) {
                    profitInSek = (item.stockOrderAggregate.getProfit(
                        stockPriceListener.getStockPrice(
                            item.stockOrderAggregate.stockSymbol
                        ).price
                    )) * currencyRate.rateInSek
                    break
                }
            }
            itemView.requireViewById<TextView>(R.id.textView_listrowStockItem_latestValue).text =
                stockCurrencyFormat.format(currentStockPrice)
            itemView.requireViewById<TextView>(R.id.textView_listrowStockItem_profit).text =
                swedishCurrencyFormat.format(profitInSek)
            itemView.requireViewById<TextView>(R.id.textView_listrowStockItem_profit).setTextColor(
                ContextCompat.getColor(
                    context, if (profitInSek > 0) {
                        R.color.colorProfit
                    } else {
                        R.color.colorLoss
                    }
                )
            )
            val overflowMenu = PopupMenu(context, overflowMenuImageView)
            overflowMenuImageView.setOnClickListener { overflowMenu.show() }
            overflowMenu.inflate(R.menu.home_listrow_actions)
            overflowMenu.setOnMenuItemClickListener(
                PopupMenuListener(
                    context, stockRemoveClickListener,
                    item.stockOrderAggregate.stockSymbol
                )
            )
        }

        class PopupMenuListener(
            private val context: Context,
            private val stockRemoveClickListener: StockRemoveClickListener,
            private val stockName: String,
        ) : PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem?): Boolean {
                return if (item?.itemId == R.id.action_delete) {
                    stockRemoveClickListener.onClickRemove(context, stockName)
                    true
                } else {
                    false
                }
            }

        }

    }

    override fun getStockPrice(stockSymbol: String): com.sundbybergsit.cromfortune.domain.StockPrice {
        return (StockPriceRepository.stockPrices.value as StockPriceRepository.ViewState.VALUES)
            .stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol }!!
    }

}

