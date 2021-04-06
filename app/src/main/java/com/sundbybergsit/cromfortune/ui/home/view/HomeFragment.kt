package com.sundbybergsit.cromfortune.ui.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.sundbybergsit.cromfortune.databinding.FragmentHomeBinding
import com.sundbybergsit.cromfortune.ui.home.HomeViewModel
import com.sundbybergsit.cromfortune.ui.home.trade.RegisterBuyStockDialogFragment
import com.sundbybergsit.cromfortune.ui.home.trade.RegisterSellStockDialogFragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    companion object {

        const val TAG = "HomeFragment"

    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPagerFragmentHome.adapter = ScreenSlidePagerAdapter(requireActivity())
        TabLayoutMediator(binding.tabLayoutFragmentHome, binding.viewPagerFragmentHome) { tab, position ->
            tab.text = getString(if (position == 0) {
                R.string.home_stocks_personal_title
            } else {
                R.string.home_stocks_crom_title
            })
        }.attach()
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolBarFragmentHome.setupWithNavController(navController, appBarConfiguration)
        binding.toolBarFragmentHome.inflateMenu(R.menu.home_actions)
        binding.toolBarFragmentHome.setOnMenuItemClickListener { item ->
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
