package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import kotlinx.android.synthetic.main.listrow_stock_order_item.view.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class StockOrderListAdapter(
        private val context: Context, private val fragmentManager: FragmentManager,
) :
        ListAdapter<AdapterItem, RecyclerView.ViewHolder>(AdapterItemDiffUtil<AdapterItem>()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is StockViewHolder -> {
                holder.bind(item as StockAdapterItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)!!) {
        is StockHeaderAdapterItem -> {
            R.layout.listrow_stock_order_header
        }
        is StockAdapterItem -> {
            R.layout.listrow_stock_order_item
        }
        else -> {
            throw IllegalArgumentException("Unexpected item: " + item.javaClass.canonicalName)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.listrow_stock_order_header -> HeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(viewType, parent, false))
            R.layout.listrow_stock_order_item -> StockViewHolder(context = context, adapter = this, fragmentManager = fragmentManager, itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false))
            else -> throw IllegalArgumentException("Unexpected viewType: $viewType")
        }
    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal class StockViewHolder(
            private val context: Context,
            private val fragmentManager: FragmentManager,
            itemView: View,
            private val adapter: StockOrderListAdapter,
    ) : RecyclerView.ViewHolder(itemView) {

        var formatter = SimpleDateFormat("yyyy-MM-dd", ConfigurationCompat.getLocales(context.resources.configuration).get(0))

        fun bind(item: StockAdapterItem) {
            itemView.textView_listrowStockOrderItem_date.text = formatter.format(Date(item.stockOrder.dateInMillis))
            itemView.textView_listrowStockOrderItem_quantity.text = item.stockOrder.quantity.toString()
            itemView.setOnLongClickListener {
                val dialog = DeleteStockOrderDialogFragment(context = context,adapter = adapter, stockOrder = item.stockOrder)
                dialog.show(fragmentManager, HomeFragment.TAG)
                true
            }
            val pricePerStock = item.stockOrder.pricePerStock
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            if (pricePerStock < 1) {
                format.maximumFractionDigits = 3
            } else {
                format.maximumFractionDigits = 2
            }
            format.currency = Currency.getInstance(item.stockOrder.currency)
            itemView.textView_listrowStockOrderItem_price.text = format.format(pricePerStock)
             val rateInSek: Double = (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES)
                    .currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == item.stockOrder.currency }!!.rateInSek
            itemView.textView_listrowStockOrderItem_total.text = format.format(item.stockOrder.getTotalCost(rateInSek))
            itemView.setBackgroundColor(getBuyOrSellColor(item.stockOrder.orderAction))
        }

        @ColorInt
        private fun getBuyOrSellColor(orderAction: String): Int {
            return if (orderAction == "Buy") {
                ContextCompat.getColor(context, android.R.color.holo_green_light)
            } else {
                ContextCompat.getColor(context, android.R.color.holo_red_light)
            }
        }

    }

}
