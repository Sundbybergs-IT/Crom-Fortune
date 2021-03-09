package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.fragment_notifications_archived.*
import java.util.*

class OldNotificationsFragment : Fragment(R.layout.fragment_notifications_archived) {

    private val viewModel: OldNotificationsViewModel by viewModels()

    private val listAdapter = NotificationListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_fragmentNotificationsArchived.adapter = listAdapter
        setUpLiveDataListeners()
        viewModel.refresh(requireContext())
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.notifications_actions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clearNotifications -> {
                viewModel.clearNotifications(requireContext())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpLiveDataListeners() {
        viewModel.notifications.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is NotificationsViewState.HasNotifications -> {
                    textView_fragmentNotificationsArchived.visibility = View.GONE
                    listAdapter.submitList(viewState.adapterItems)
                }
                is NotificationsViewState.HasNoNotifications -> {
                    listAdapter.submitList(Collections.emptyList())
                    textView_fragmentNotificationsArchived.visibility = View.VISIBLE
                    textView_fragmentNotificationsArchived.text = getString(R.string.notifications_empty)
                }
            }
        })
    }

}
