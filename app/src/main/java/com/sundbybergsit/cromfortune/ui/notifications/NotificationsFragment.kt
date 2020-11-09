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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationsViewModel =
                ViewModelProvider.NewInstanceFactory().create(NotificationsViewModel::class.java)
        setUpLiveDataListeners()
    }

    private fun setUpLiveDataListeners() {
        notificationsViewModel.text.observe(viewLifecycleOwner, {
            text_notifications.text = it
        })
    }

}
