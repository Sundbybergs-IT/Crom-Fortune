package com.sundbybergsit.cromfortune.ui.home.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.home.HomeViewModel
import com.sundbybergsit.cromfortune.ui.home.trade.RegisterBuyStockDialogFragment
import com.sundbybergsit.cromfortune.ui.home.trade.RegisterSellStockDialogFragment
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    companion object {

        const val TAG = "HomeFragment"

    }

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager_fragmentHome.adapter = ScreenSlidePagerAdapter(requireActivity())
        TabLayoutMediator(tabLayout_fragmentHome, viewPager_fragmentHome) { tab, position ->
            tab.text = getString(if (position == 0) {
                R.string.home_stocks_personal_title
            } else {
                R.string.home_stocks_crom_title
            })
        }.attach()
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar_fragmentHome.setupWithNavController(navController, appBarConfiguration)
        toolBar_fragmentHome.inflateMenu(R.menu.home_actions)
        toolBar_fragmentHome.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_buyStock -> {
                    val dialog = RegisterBuyStockDialogFragment(viewModel)
                    dialog.show(parentFragmentManager, HomePersonalStocksFragment.TAG)
                    true
                }
                R.id.action_sellStock -> {
                    val dialog = RegisterSellStockDialogFragment(viewModel)
                    dialog.show(parentFragmentManager, HomePersonalStocksFragment.TAG)
                    true
                }
                R.id.action_refresh -> {
                    viewModel.refreshData(requireContext())
                    Toast.makeText(context, R.string.home_information_data_refreshed, Toast.LENGTH_LONG).show()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        private val homePersonalStocksFragment = HomePersonalStocksFragment()
        private val homeCromStocksFragment = HomeCromStocksFragment()

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                homePersonalStocksFragment
            } else {
                homeCromStocksFragment
            }
        }

        override fun getItemCount(): Int = 2

    }

}
