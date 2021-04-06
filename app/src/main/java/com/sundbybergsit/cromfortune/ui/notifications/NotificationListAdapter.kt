package com.sundbybergsit.cromfortune.ui.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.AdapterItem
import com.sundbybergsit.cromfortune.ui.AdapterItemDiffUtil
import com.sundbybergsit.cromfortune.ui.home.StockHeaderAdapterItem
import java.text.SimpleDateFormat
import java.util.*

class NotificationListAdapter : ListAdapter<AdapterItem, RecyclerView.ViewHolder>(AdapterItemDiffUtil<AdapterItem>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.listrow_notification_header -> HeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(viewType, parent, false))
            R.layout.listrow_notification_item -> NotificationViewHolder(parent.context, LayoutInflater
                    .from(parent.context).inflate(viewType, parent, false))
            else -> throw IllegalArgumentException("Unexpected viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is NotificationViewHolder -> {
                holder.bind(item as NotificationAdapterItem, position % 2 == 0)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)!!) {
        is StockHeaderAdapterItem -> {
            R.layout.listrow_notification_header
        }
        is NotificationAdapterItem -> {
            R.layout.listrow_notification_item
        }
        else -> {
            throw IllegalArgumentException("Unexpected item: " + item.javaClass.canonicalName)
        }
    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal class NotificationViewHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {

        var formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", ConfigurationCompat
                .getLocales(context.resources.configuration).get(0))

        fun bind(item: NotificationAdapterItem, evenRow: Boolean) {
            itemView.requireViewById<TextView>(R.id.textView_listrowNotificationItem_date).text = formatter
                    .format(Date(item.notificationMessage.dateInMillis))
            itemView.requireViewById<TextView>(R.id.textView_listrowNotificationItem_name).text = item.notificationMessage.message
            itemView.setOnLongClickListener {
                Toast.makeText(context, R.string.generic_error_not_supported, Toast.LENGTH_LONG).show()
                true
            }
            itemView.background = ContextCompat.getDrawable(context, if (evenRow) {
                R.drawable.background_accent_selectable_item_background
            } else {
                R.drawable.background_white_selectable_item_background
            })
        }

    }

}
