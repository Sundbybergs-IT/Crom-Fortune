package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.fragment_notifications.*

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    companion object {

        const val TAG = "NotificationsFragment"

    }

    private val viewModel: NotificationsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager_fragmentNotifications.adapter = ScreenSlidePagerAdapter(requireActivity())
        TabLayoutMediator(tabLayout_fragmentNotifications, viewPager_fragmentNotifications) { tab, position ->
            tab.text = getString(if (position == 0) {
                R.string.notifications_new_title
            } else {
                R.string.notifications_old_title
            })
        }.attach()
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar_fragmentNotifications.setupWithNavController(navController, appBarConfiguration)
        toolBar_fragmentNotifications.inflateMenu(R.menu.notifications_actions)
        toolBar_fragmentNotifications.setOnMenuItemClickListener { item ->
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
