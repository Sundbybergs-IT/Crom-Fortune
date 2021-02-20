package com.sundbybergsit.cromfortune.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockPrice
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import kotlinx.android.synthetic.main.listrow_stock_header.view.*
import kotlinx.android.synthetic.main.listrow_stock_item.view.*
import java.text.NumberFormat
import java.util.*

class StockOrderAggregateListAdapter(private val stockClickListener: StockClickListener) :
        ListAdapter<AdapterItem, RecyclerView.ViewHolder>(AdapterItemDiffUtil<AdapterItem>()), StockPriceListener {

    private lateinit var stockRemoveClickListener: StockRemoveClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.listrow_stock_header -> StockOrderAggregateHeaderViewHolder(stockPriceListener = this,
                    itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                    context = parent.context)
            R.layout.listrow_stock_item -> StockOrderAggregateViewHolder(stockClickListener = stockClickListener,
                    stockRemoveClickListener = stockRemoveClickListener,
                    stockPriceListener = this,
                    itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                    context = parent.context)
            else -> throw IllegalArgumentException("Unexpected viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is StockOrderAggregateHeaderViewHolder -> {
                holder.bind(item as StockAggregateHeaderAdapteritem)
            }
            is StockOrderAggregateViewHolder -> {
                holder.bind(item as StockAggregateAdapterItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)!!) {
        is StockAggregateHeaderAdapteritem -> {
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

        fun bind(item: StockAggregateHeaderAdapteritem) {
            var count = 0.0
            val currencyRates = (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES)
                    .currencyRates.toList()
            for (stockOrderAggregate in item.stockOrderAggregates.toList()) {
                for (currencyRate in currencyRates) {
                    if (currencyRate.iso4217CurrencySymbol == stockOrderAggregate.currency.currencyCode) {
                        count += (stockOrderAggregate.getProfit(stockPriceListener.getStockPrice(
                                stockOrderAggregate.stockSymbol).price)) * currencyRate.rateInSek
                        break
                    }
                }
            }
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            format.currency = Currency.getInstance("SEK")
            format.maximumFractionDigits = 2
            itemView.textView_listrowStockHeader_totalProfit.text = format.format(count)
            itemView.textView_listrowStockHeader_totalProfit.setTextColor(ContextCompat.getColor(context, if (count >= 0.0) {
                R.color.colorProfit
            } else {
                R.color.colorLoss
            }))
        }

    }

    internal class StockOrderAggregateViewHolder(
            private val context: Context,
            private val stockPriceListener: StockPriceListener,
            private val stockClickListener: StockClickListener,
            private val stockRemoveClickListener: StockRemoveClickListener,
            itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: StockAggregateAdapterItem) {
            itemView.textView_listrowStockItem_quantity.text = item.stockOrderAggregate.getQuantity().toString()
            @SuppressLint("SetTextI18n")
            itemView.textView_listrowStockItem_name.text = item.stockOrderAggregate.displayName
            val acquisitionValue = item.stockOrderAggregate.getAcquisitionValue()
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            if (acquisitionValue < 1) {
                format.maximumFractionDigits = 3
            } else {
                format.maximumFractionDigits = 2
            }
            format.currency = item.stockOrderAggregate.currency
            itemView.setOnClickListener {
                stockClickListener.onClick(item.stockOrderAggregate.stockSymbol)
            }
            itemView.button_listrowStockItem_buy.setOnClickListener {
                Toast.makeText(context, R.string.generic_error_not_supported, Toast.LENGTH_LONG).show()
            }
            itemView.button_listrowStockItem_sell.setOnClickListener {
                Toast.makeText(context, R.string.generic_error_not_supported, Toast.LENGTH_LONG).show()
            }
            itemView.imageButton_listrowStockItem_muteUnmute.setOnClickListener {
                Toast.makeText(context, R.string.generic_error_not_supported, Toast.LENGTH_LONG).show()
            }
            itemView.textView_listrowStockItem_acquisitionValue.text = format.format(acquisitionValue)
            val currentStockPrice = stockPriceListener.getStockPrice(item.stockOrderAggregate.stockSymbol).price
            val profit = item.stockOrderAggregate.getProfit(currentStockPrice)
            itemView.textView_listrowStockItem_latestValue.text = format.format(currentStockPrice)
            itemView.textView_listrowStockItem_profit.text = format.format(profit)
            itemView.textView_listrowStockItem_profit.setTextColor(ContextCompat.getColor(context, if (profit > 0) {
                R.color.colorProfit
            } else {
                R.color.colorLoss
            }))
            val overflowMenu = PopupMenu(context, itemView.imageView_listrowStockItem_overflowMenu)
            itemView.imageView_listrowStockItem_overflowMenu.setOnClickListener { overflowMenu.show() }
            overflowMenu.inflate(R.menu.home_listrow_actions)
            overflowMenu.setOnMenuItemClickListener(PopupMenuListener(context, stockRemoveClickListener,
                    item.stockOrderAggregate.stockSymbol))
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

    override fun getStockPrice(stockSymbol: String): StockPrice {
        return (StockPriceRepository.stockPrices.value as StockPriceRepository.ViewState.VALUES)
                .stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol }!!
    }

}

