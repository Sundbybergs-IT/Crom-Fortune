package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.databinding.FragmentNotificationsArchivedBinding
import java.util.*

class OldNotificationsFragment : Fragment(R.layout.fragment_notifications_archived) {

    companion object {

        const val TAG = "OldNotificationsFragment"

    }

    private var _binding: FragmentNotificationsArchivedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by activityViewModels()

    private val listAdapter = NotificationListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsArchivedBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewFragmentNotificationsArchived.adapter = listAdapter
        setUpLiveDataListeners()
        viewModel.refreshOld(requireContext())
    }

    private fun setUpLiveDataListeners() {
        viewModel.oldNotifications.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is NotificationsViewState.HasNotifications -> {
                    binding.textViewFragmentNotificationsArchived.visibility = View.GONE
                    listAdapter.submitList(viewState.adapterItems)
                }
                is NotificationsViewState.HasNoNotifications -> {
                    listAdapter.submitList(Collections.emptyList())
                    binding.textViewFragmentNotificationsArchived.visibility = View.VISIBLE
                    binding.textViewFragmentNotificationsArchived.text = getString(R.string.notifications_empty)
                }
            }
        })
    }

}
