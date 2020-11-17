package com.sundbybergsit.cromfortune.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.home.AdapterItem
import com.sundbybergsit.cromfortune.ui.home.AdapterItemDiffUtil
import com.sundbybergsit.cromfortune.ui.home.StockAdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockHeaderAdapterItem
import kotlinx.android.synthetic.main.listrow_stock.view.*

class StockListAdapter : ListAdapter<AdapterItem, RecyclerView.ViewHolder>(AdapterItemDiffUtil<AdapterItem>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.listrow_header -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
            R.layout.listrow_stock -> StockViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
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
            R.layout.listrow_header
        }
        is AdapterItem -> {
            R.layout.listrow_stock
        }
        else -> {
            throw IllegalArgumentException("Unexpected item: " + item.javaClass.canonicalName)
        }
    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: StockAdapterItem) {
            itemView.textView_listrowStock_quantity.text = item.stockOrder.quantity.toString()
            itemView.textView_listrowStock_name.text = item.stockOrder.name
            val acquisitionValue = item.stockOrder.getAcquisitionValue()
            val roundedAcquisitionValue = if (acquisitionValue < 1) {String.format("%.3f", acquisitionValue)} else { String.format("%.2f", acquisitionValue) }
            itemView.textView_listrowStock_acquisitionValue.text = itemView.context.resources.getQuantityString(
                    R.plurals.generic_cost, acquisitionValue.toInt(),
                    roundedAcquisitionValue)
            itemView.button_listrowStock_delete.setOnClickListener {
                Toast.makeText(itemView.context, itemView.context.getString(R.string.generic_error_not_supported),
                        Toast.LENGTH_LONG).show()
            }

        }

    }

}

