package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.home.StockPriceProducer
import com.sundbybergsit.cromfortune.ui.home.StockPriceRetriever
import kotlinx.android.synthetic.main.fragment_notifications.*

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private lateinit var notificationsViewModel: NotificationsViewModel
    private val stockPriceRetriever: StockPriceRetriever = StockPriceRetriever(StockPriceProducer(),
            3000L, 0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationsViewModel =
                ViewModelProvider.NewInstanceFactory().create(NotificationsViewModel::class.java)
        setUpLiveDataListeners()
    }

    override fun onResume() {
        super.onResume()
        stockPriceRetriever.start()
    }

    override fun onPause() {
        super.onPause()
        stockPriceRetriever.stop()
    }

    private fun setUpLiveDataListeners() {
        notificationsViewModel.text.observe(viewLifecycleOwner, {
            text_notifications.text = it
        })
        stockPriceRetriever.stockPrices.observe(viewLifecycleOwner, { stockPrice ->
            Toast.makeText(requireContext(), stockPrice.toString(), Toast.LENGTH_SHORT).show()
        })
    }

}
