package com.sundbybergsit.cromfortune.ui

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import java.util.*
import kotlin.collections.ArrayList

class AutoCompleteAdapter(context: Context, resource: Int, textViewResourceId: Int, objects: List<String>) :
        ArrayAdapter<String>(context, resource, textViewResourceId, objects), Filterable {

    private var fullList: ArrayList<String>
    private var mOriginalValues: ArrayList<String>?
    private var mFilter: ArrayFilter? = null

    init {
        fullList = objects as ArrayList<String>
        mOriginalValues = ArrayList(fullList)
    }

    override fun getCount(): Int {
        return fullList.size
    }

    override fun getItem(position: Int): String {
        return fullList[position]
    }

    override fun getFilter(): Filter {
        if (mFilter == null) {
            mFilter = ArrayFilter()
        }
        return mFilter!!
    }

    private inner class ArrayFilter : Filter() {

        private val lock: Any? = null
        override fun performFiltering(prefix: CharSequence?): FilterResults {
            val results = FilterResults()
            if (mOriginalValues == null) {
                synchronized(lock!!) { mOriginalValues = ArrayList(fullList) }
            }
            if (prefix == null || prefix.isEmpty()) {
                synchronized(lock!!) {
                    val list = ArrayList(mOriginalValues!!.toMutableList())
                    results.values = list
                    results.count = list.size
                }
            } else {
                val prefixString = prefix.toString().lowercase(Locale.ROOT)
                val values = mOriginalValues
                val count: Int = values!!.size
                val newValues = ArrayList<String>(count)
                for (i in 0 until count) {
                    val item = values[i]
                    if (item.lowercase(Locale.ROOT).contains(prefixString)) {
                        newValues.add(item)
                    }
                }
                results.values = newValues
                results.count = newValues.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            fullList = if (results.values != null) {
                results.values as ArrayList<String>
            } else {
                ArrayList()
            }
            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

}
