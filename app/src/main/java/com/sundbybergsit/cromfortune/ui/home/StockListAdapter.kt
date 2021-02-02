package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.listrow_stock_item.view.*
import java.text.NumberFormat
import java.util.*

class StockListAdapter(private val stockClickListener: StockClickListener) :
        ListAdapter<AdapterItem, RecyclerView.ViewHolder>(AdapterItemDiffUtil<AdapterItem>()) {

    private lateinit var stockRemoveClickListener: StockRemoveClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.listrow_stock_header -> HeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(viewType, parent, false))
            R.layout.listrow_stock_item -> StockViewHolder(stockClickListener = stockClickListener,
                    stockRemoveClickListener = stockRemoveClickListener,
                    itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                    context = parent.context)
            else -> throw IllegalArgumentException("Unexpected viewType: $viewType")
        }
    }

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
            R.layout.listrow_stock_header
        }
        is AdapterItem -> {
            R.layout.listrow_stock_item
        }
        else -> {
            throw IllegalArgumentException("Unexpected item: " + item.javaClass.canonicalName)
        }
    }

    fun setListener(stockRemoveClickListener: StockRemoveClickListener) {
        this.stockRemoveClickListener = stockRemoveClickListener
    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal class StockViewHolder(
            private val context: Context,
            private val stockClickListener: StockClickListener,
            private val stockRemoveClickListener: StockRemoveClickListener,
            itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: StockAdapterItem) {
            itemView.textView_listrowStockItem_quantity.text = item.stockOrder.quantity.toString()
            val stockName = StockPriceRetriever.SYMBOLS.find { pair -> pair.first == item.stockOrder.name }!!.second
            itemView.textView_listrowStockItem_name.text = "$stockName (${item.stockOrder.name})"
            val acquisitionValue = item.stockOrder.getAcquisitionValue()
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            if (acquisitionValue < 1) {
                format.maximumFractionDigits = 3
            } else {
                format.maximumFractionDigits = 2
            }
            format.currency = Currency.getInstance(item.stockOrder.currency)
            itemView.button_listrowStockItem_buy.setOnClickListener {
                Toast.makeText(context, R.string.generic_error_not_supported, Toast.LENGTH_LONG).show()
            }
            itemView.button_listrowStockItem_sell.setOnClickListener {
                Toast.makeText(context, R.string.generic_error_not_supported, Toast.LENGTH_LONG).show()
            }
            itemView.textView_listrowStockItem_acquisitionValue.text = format.format(acquisitionValue)
            val overflowMenu = PopupMenu(context, itemView.imageView_listrowStockItem_overflowMenu)
            itemView.imageView_listrowStockItem_overflowMenu.setOnClickListener { overflowMenu.show() }
            overflowMenu.inflate(R.menu.menu_stock_list)
            overflowMenu.setOnMenuItemClickListener(PopupMenuListener(context, stockRemoveClickListener,
                    item.stockOrder.name))
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

}

