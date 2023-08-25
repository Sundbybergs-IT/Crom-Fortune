package com.sundbybergsit.cromfortune.ui.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.stocks.StockPriceListener
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.ui.AdapterItemDiffUtil
import com.sundbybergsit.cromfortune.ui.home.HomeViewModel
import com.sundbybergsit.cromfortune.ui.home.StockAggregateAdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockAggregateHeaderAdapterItem
import com.sundbybergsit.cromfortune.ui.home.trade.RegisterBuyStockDialogFragment
import com.sundbybergsit.cromfortune.ui.home.trade.RegisterSellStockDialogFragment
import java.text.NumberFormat
import java.util.*

internal class StockOrderAggregateListAdapter(
    private val viewModel: HomeViewModel,
    private val parentFragmentManager: FragmentManager,
    private val stockClickListener: StockClickListener,
    private val readOnly: Boolean,
) :
    ListAdapter<NameAndValueAdapterItem, RecyclerView.ViewHolder>(AdapterItemDiffUtil<NameAndValueAdapterItem>()),
    StockPriceListener {

    private lateinit var stockRemoveClickListener: StockRemoveClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.listrow_stock_header -> StockOrderAggregateHeaderViewHolder(
                context = parent.context,
                stockPriceListener = this,
                itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                adapter = this
            )
            R.layout.listrow_stock_item -> StockOrderAggregateViewHolder(
                context = parent.context,
                viewModel = viewModel,
                parentFragmentManager = parentFragmentManager,
                stockClickListener = stockClickListener,
                stockRemoveClickListener = stockRemoveClickListener,
                stockPriceListener = this,
                itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                readOnly = readOnly
            )
            else -> throw IllegalArgumentException("Unexpected viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is StockOrderAggregateHeaderViewHolder -> {
                holder.bind(item as StockAggregateHeaderAdapterItem)
            }
            is StockOrderAggregateViewHolder -> {
                holder.bind(item as StockAggregateAdapterItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)!!) {
        is StockAggregateHeaderAdapterItem -> {
            R.layout.listrow_stock_header
        }
        is StockAggregateAdapterItem -> {
            R.layout.listrow_stock_item
        }
        else -> {
            throw IllegalArgumentException("Unexpected item: " + item.javaClass.canonicalName)
        }
    }

    fun setListener(stockRemoveClickListener: StockRemoveClickListener) {
        this.stockRemoveClickListener = stockRemoveClickListener
    }

    internal class StockOrderAggregateHeaderViewHolder(
        private val context: Context,
        private val stockPriceListener: StockPriceListener,
        private val adapter: StockOrderAggregateListAdapter,
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: StockAggregateHeaderAdapterItem) {
            var count = 0.0
            val currencyRates = (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES)
                .currencyRates.toList()
            for (stockOrderAggregate in item.stockOrderAggregates.toList()) {
                for (currencyRate in currencyRates) {
                    if (currencyRate.iso4217CurrencySymbol == stockOrderAggregate.currency.currencyCode) {
                        count += (stockOrderAggregate.getProfit(
                            stockPriceListener.getStockPrice(
                                stockOrderAggregate.stockSymbol
                            ).price
                        )) * currencyRate.rateInSek
                        break
                    }
                }
            }
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            format.currency = Currency.getInstance("SEK")
            format.maximumFractionDigits = 2
            itemView.requireViewById<TextView>(R.id.textView_listrowStockHeader_totalProfit).text = format.format(count)
            itemView.requireViewById<TextView>(R.id.textView_listrowStockHeader_totalProfit).setTextColor(
                ContextCompat.getColor(
                    context, if (count >= 0.0) {
                        R.color.colorProfit
                    } else {
                        R.color.colorLoss
                    }
                )
            )
            val overflowMenuImageView =
                itemView.requireViewById<ImageView>(R.id.imageView_listrowStockHeader_overflowMenu)
            val overflowMenu = PopupMenu(context, overflowMenuImageView)
            overflowMenuImageView.setOnClickListener { overflowMenu.show() }
            overflowMenu.inflate(R.menu.home_listrowheader_actions)
            overflowMenu.setOnMenuItemClickListener(PopupMenuListener(adapter))
        }

        class PopupMenuListener(
            private val adapter: StockOrderAggregateListAdapter,
        ) : PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.action_sort_alphabetical_up -> {
                        adapter.submitList(adapter.currentList.subList(0, 1) +
                                adapter.currentList.subList(1, adapter.currentList.size)
                                    .sortedByDescending { adapterItem -> adapterItem.name })
                        true
                    }
                    R.id.action_sort_alphabetical_down -> {
                        adapter.submitList(adapter.currentList.subList(0, 1) +
                                adapter.currentList.subList(1, adapter.currentList.size)
                                    .sortedBy { adapterItem -> adapterItem.name })
                        true
                    }
                    R.id.action_sort_profit_up -> {
                        adapter.submitList(adapter.currentList.subList(0, 1) +
                                adapter.currentList.subList(1, adapter.currentList.size)
                                    .sortedByDescending { adapterItem -> adapterItem.value })
                        true
                    }
                    R.id.action_sort_profit_down -> {
                        adapter.submitList(adapter.currentList.subList(0, 1) +
                                adapter.currentList.subList(1, adapter.currentList.size)
                                    .sortedBy { adapterItem -> adapterItem.value })
                        true
                    }
                    else -> {
                        false
                    }
                }
            }

        }

    }

    internal class StockOrderAggregateViewHolder(
        private val context: Context,
        private val viewModel: HomeViewModel,
        private val parentFragmentManager: FragmentManager,
        private val stockPriceListener: StockPriceListener,
        private val stockClickListener: StockClickListener,
        private val stockRemoveClickListener: StockRemoveClickListener,
        itemView: View,
        private val readOnly: Boolean,
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: StockAggregateAdapterItem) {
            val stockOrderAggregate = item.stockOrderAggregate
            requireViewById<TextView>(R.id.textView_listrowStockItem_quantity).text =
                stockOrderAggregate.getQuantity().toString()
            @SuppressLint("SetTextI18n")
            requireViewById<TextView>(R.id.textView_listrowStockItem_name).text =
                stockOrderAggregate.displayName
            val acquisitionValue = stockOrderAggregate.getAcquisitionValue()
            val stockCurrencyFormat: NumberFormat = getStockCurrencyFormat(item, acquisitionValue)
            initializeCurrentStockPrice(item, stockCurrencyFormat, acquisitionValue)
            setUpMuteAndUnmuteMenu(item)
            val currencyRates = (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES)
                .currencyRates.toList()
            val profitInSek = getProfitInSek(currencyRates, stockOrderAggregate)
            setUpProfit(profitInSek)
            val stockSymbol = stockOrderAggregate.stockSymbol
            setUpBuyMenu(stockSymbol)
            setUpSellMenu(stockSymbol)
            itemView.setOnClickListener {
                stockClickListener.onClick(stockSymbol, readOnly)
            }
            setUpOverflowMenu(stockSymbol)
        }

        private fun setUpBuyMenu(stockSymbol: String) {
            if (readOnly) {
                requireViewById<View>(R.id.button_listrowStockItem_buy).visibility = View.INVISIBLE
            }
            requireViewById<Button>(R.id.button_listrowStockItem_buy).setOnClickListener {
                val dialog = RegisterBuyStockDialogFragment(viewModel)
                dialog.arguments = bundleOf(Pair(RegisterSellStockDialogFragment.EXTRA_STOCK_SYMBOL, stockSymbol))
//                dialog.show(parentFragmentManager, HomePersonalStocksFragment.TAG)
            }
        }

        private fun setUpSellMenu(stockSymbol: String) {
            if (readOnly) {
                requireViewById<View>(R.id.button_listrowStockItem_sell).visibility = View.INVISIBLE
            }
            requireViewById<Button>(R.id.button_listrowStockItem_sell).setOnClickListener {
                val dialog = RegisterSellStockDialogFragment(viewModel)
                dialog.arguments = bundleOf(Pair(RegisterSellStockDialogFragment.EXTRA_STOCK_SYMBOL, stockSymbol))
//                dialog.show(parentFragmentManager, HomePersonalStocksFragment.TAG)
            }
        }

        private fun getProfitInSek(
            currencyRates: List<CurrencyRate>,
            stockOrderAggregate: StockOrderAggregate
        ): Double {
            var profitInSek = 0.0
            for (currencyRate in currencyRates) {
                if (currencyRate.iso4217CurrencySymbol == stockOrderAggregate.currency.currencyCode) {
                    profitInSek = (stockOrderAggregate.getProfit(
                        stockPriceListener.getStockPrice(
                            stockOrderAggregate.stockSymbol
                        ).price
                    )) * currencyRate.rateInSek
                    break
                }
            }
            return profitInSek
        }

        private fun initializeCurrentStockPrice(
            item: StockAggregateAdapterItem,
            stockCurrencyFormat: NumberFormat,
            acquisitionValue: Double
        ) {
            requireViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue).text =
                stockCurrencyFormat.format(acquisitionValue)
            val currentStockPrice = stockPriceListener.getStockPrice(item.stockOrderAggregate.stockSymbol).price
            requireViewById<TextView>(R.id.textView_listrowStockItem_latestValue).text =
                stockCurrencyFormat.format(currentStockPrice)
        }

        private fun setUpMuteAndUnmuteMenu(adapterItem: StockAggregateAdapterItem) {
            requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute).setImageDrawable(
                if (adapterItem.muted) {
                    ContextCompat.getDrawable(context, R.drawable.ic_fas_bell_slash)
                } else {
                    ContextCompat.getDrawable(context, R.drawable.ic_fas_bell)
                }
            )
            requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute).setOnClickListener {
                if (adapterItem.muted) {
                    StockMuteSettingsRepository.unmute(adapterItem.stockOrderAggregate.stockSymbol)
                    requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute)
                        .setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_fas_bell))
                } else {
                    StockMuteSettingsRepository.mute(adapterItem.stockOrderAggregate.stockSymbol)
                    requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute)
                        .setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_fas_bell_slash))
                }
                adapterItem.muted = !adapterItem.muted
            }
            if (readOnly) {
                requireViewById<View>(R.id.imageButton_listrowStockItem_muteUnmute).visibility = View.INVISIBLE
            }
        }

        private fun setUpProfit(profitInSek: Double) {
            val swedishCurrencyFormat: NumberFormat = NumberFormat.getCurrencyInstance()
            swedishCurrencyFormat.currency = Currency.getInstance("SEK")
            requireViewById<TextView>(R.id.textView_listrowStockItem_profit).text =
                swedishCurrencyFormat.format(profitInSek)
            requireViewById<TextView>(R.id.textView_listrowStockItem_profit).setTextColor(
                ContextCompat.getColor(
                    context, if (profitInSek > 0) {
                        R.color.colorProfit
                    } else {
                        R.color.colorLoss
                    }
                )
            )
        }

        private fun setUpOverflowMenu(
            stockSymbol: String
        ) {
            val overflowMenuImageView = requireViewById<View>(R.id.imageView_listrowStockItem_overflowMenu)
            val overflowMenu = PopupMenu(context, overflowMenuImageView)
            overflowMenuImageView.setOnClickListener { overflowMenu.show() }
            overflowMenu.inflate(R.menu.home_listrow_actions)
            overflowMenu.setOnMenuItemClickListener(
                PopupMenuListener(context, stockRemoveClickListener, stockSymbol)
            )
            if (readOnly) {
                overflowMenuImageView.visibility = View.INVISIBLE
            }
        }

        private fun getStockCurrencyFormat(
            item: StockAggregateAdapterItem,
            acquisitionValue: Double
        ): NumberFormat {
            val stockCurrencyFormat: NumberFormat = NumberFormat.getCurrencyInstance()
            stockCurrencyFormat.currency = item.stockOrderAggregate.currency
            if (acquisitionValue < 1) {
                stockCurrencyFormat.maximumFractionDigits = 3
            } else {
                stockCurrencyFormat.maximumFractionDigits = 2
            }
            if (acquisitionValue < 1) {
                stockCurrencyFormat.maximumFractionDigits = 3
            } else {
                stockCurrencyFormat.maximumFractionDigits = 2
            }
            return stockCurrencyFormat
        }

        private fun <T : View> requireViewById(@IdRes id: Int): T {
            return itemView.requireViewById(id)
        }

        class PopupMenuListener(
            private val context: Context,
            private val stockRemoveClickListener: StockRemoveClickListener,
            private val stockName: String,
        ) : PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem?): Boolean {
                return if (item?.itemId == R.id.action_delete) {
                    stockRemoveClickListener.onClickRemove(context, stockName)
                    true
                } else {
                    false
                }
            }

        }

    }

    override fun getStockPrice(stockSymbol: String): com.sundbybergsit.cromfortune.domain.StockPrice {
        return (StockPriceRepository.stockPrices.value as StockPriceRepository.ViewState.VALUES)
            .stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol }!!
    }

}

