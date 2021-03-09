package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.fragment_notifications.*

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private val viewModel: NotificationsViewModel by viewModels()

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

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            if (position == 0) {
                return NewNotificationsFragment()
            } else {
                return OldNotificationsFragment()
            }
        }
    }

}
