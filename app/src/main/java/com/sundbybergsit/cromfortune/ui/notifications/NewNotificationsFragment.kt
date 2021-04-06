package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.databinding.FragmentNotificationsCurrentBinding
import java.util.*

class NewNotificationsFragment : Fragment(R.layout.fragment_notifications_current) {

    companion object {

        const val TAG = "NewNotificationsFragment"

    }

    private var _binding: FragmentNotificationsCurrentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by activityViewModels()

    private val listAdapter = NotificationListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsCurrentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewFragmentNotificationsCurrent.adapter = listAdapter
        setUpLiveDataListeners()
        viewModel.refreshNew(requireContext())
    }

    private fun setUpLiveDataListeners() {
        viewModel.newNotifications.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is NotificationsViewState.HasNotifications -> {
                    binding.textViewFragmentNotificationsCurrent.visibility = View.GONE
                    listAdapter.submitList(viewState.adapterItems)
                }
                is NotificationsViewState.HasNoNotifications -> {
                    listAdapter.submitList(Collections.emptyList())
                    binding.textViewFragmentNotificationsCurrent.visibility = View.VISIBLE
                    binding.textViewFragmentNotificationsCurrent.text = getString(R.string.notifications_empty)
                }
            }
        })
    }

}
