package com.sundbybergsit.cromfortune.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.home.AdapterItem
import com.sundbybergsit.cromfortune.ui.home.AdapterItemDiffUtil
import com.sundbybergsit.cromfortune.ui.home.NotificationAdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockHeaderAdapterItem
import kotlinx.android.synthetic.main.listrow_notification_item.view.*

class NotificationListAdapter : ListAdapter<AdapterItem, RecyclerView.ViewHolder>(AdapterItemDiffUtil<AdapterItem>()) {

//    private lateinit var notificationMessage: NotificationMessage

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.listrow_notification_header -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
            R.layout.listrow_notification_item -> NotificationViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
            else -> throw IllegalArgumentException("Unexpected viewType: $viewType")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is NotificationViewHolder -> {
                holder.bind(item as NotificationAdapterItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)!!) {
        is StockHeaderAdapterItem -> {
            R.layout.listrow_notification_header
        }
        is AdapterItem -> {
            R.layout.listrow_notification_item
        }
        else -> {
            throw IllegalArgumentException("Unexpected item: " + item.javaClass.canonicalName)
        }
    }

//    fun setListener(notificationMessage: NotificationMessage) {
//        this.notificationMessage = notificationMessage
//    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: NotificationAdapterItem) {
            itemView.textView_listrowNotificationItem_date.text = item.notificationMessage.dateInMillis.toString()
            itemView.textView_listrowNotificationItem_date.text = item.notificationMessage.message
        }

    }

}
