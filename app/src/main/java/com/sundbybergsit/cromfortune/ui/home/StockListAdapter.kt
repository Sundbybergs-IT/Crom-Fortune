package com.sundbybergsit.cromfortune.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.listrow_stock_item.view.*
import java.text.NumberFormat
import java.util.*

class StockListAdapter(private val stockClickListener: StockClickListener) :
        ListAdapter<AdapterItem, RecyclerView.ViewHolder>(AdapterItemDiffUtil<AdapterItem>()) {

    private lateinit var stockRemovable: StockRemovable

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.listrow_stock_header -> HeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(viewType, parent, false))
            R.layout.listrow_stock_item -> StockViewHolder(stockClickListener = stockClickListener,
                    stockRemovable = stockRemovable,
                    itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false))
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

    fun setListener(stockRemovable: StockRemovable) {
        this.stockRemovable = stockRemovable
    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal class StockViewHolder(private val stockClickListener: StockClickListener,
                                   private val stockRemovable: StockRemovable,
                                   itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: StockAdapterItem) {
            itemView.textView_listrowStockItem_quantity.text = item.stockOrder.quantity.toString()
            itemView.textView_listrowStockItem_name.text = item.stockOrder.name
            val acquisitionValue = item.stockOrder.getAcquisitionValue()
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            if (acquisitionValue < 1) {
                format.maximumFractionDigits = 3
            } else {
                format.maximumFractionDigits = 2
            }
            format.currency = Currency.getInstance(item.stockOrder.currency)
            itemView.textView_listrowStockItem_acquisitionValue.text = format.format(acquisitionValue)
            itemView.button_listrowStockItem_delete.setOnClickListener {
                stockRemovable.remove(itemView.context, item.stockOrder.name)
            }
            itemView.setOnClickListener {
                stockClickListener.onClick(item.stockOrder.name)
            }
        }

    }

}

