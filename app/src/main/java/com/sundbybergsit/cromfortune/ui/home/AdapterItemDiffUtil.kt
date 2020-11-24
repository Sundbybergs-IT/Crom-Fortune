package com.sundbybergsit.cromfortune.ui.home

import androidx.recyclerview.widget.DiffUtil

class AdapterItemDiffUtil<T : AdapterItem> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T,
                                 newItem: T): Boolean {
        return oldItem::class == newItem::class
    }

    override fun areContentsTheSame(oldItem: T,
                                    newItem: T): Boolean {
        return oldItem.isContentTheSame(newItem)
    }

}
