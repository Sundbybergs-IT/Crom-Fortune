package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    companion object {

        const val TAG = "NotificationsFragment"

    }

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPagerFragmentNotifications.adapter = ScreenSlidePagerAdapter(requireActivity())
        TabLayoutMediator(binding.tabLayoutFragmentNotifications, binding.viewPagerFragmentNotifications) { tab, position ->
            tab.text = getString(if (position == 0) {
                R.string.notifications_new_title
            } else {
                R.string.notifications_old_title
            })
        }.attach()
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolBarFragmentNotifications.setupWithNavController(navController, appBarConfiguration)
        binding.toolBarFragmentNotifications.inflateMenu(R.menu.notifications_actions)
        binding.toolBarFragmentNotifications.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_clearNotifications -> {
                    viewModel.clearNotifications(requireContext())
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = 2

        private val newNotificationsFragment = NewNotificationsFragment()
        private val oldNotificationsFragment = OldNotificationsFragment()

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                newNotificationsFragment
            } else {
                oldNotificationsFragment
            }
        }
    }

}
