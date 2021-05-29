package com.sundbybergsit.cromfortune.ui.home.view

import android.os.Build
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.assumeEquals
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.ui.AdapterItem
import com.sundbybergsit.cromfortune.ui.home.HomeViewModel
import com.sundbybergsit.cromfortune.ui.home.StockAggregateAdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockHeaderAdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockOrderAggregate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowDrawable
import org.robolectric.shadows.ShadowLooper
import java.text.NumberFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class StockOrderAggregateListAdapterTest {

    private lateinit var listAdapter: StockOrderAggregateListAdapter

    @Before
    fun setUp() {
        CurrencyRateRepository.add(setOf(CurrencyRate("SEK", 1.0)))
        StockPriceRepository.put(setOf(
            com.sundbybergsit.cromfortune.domain.StockPrice(
                com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[0].first,
                Currency.getInstance(com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[0].third),
                100.0
            ),
            com.sundbybergsit.cromfortune.domain.StockPrice(
                com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[1].first,
                Currency.getInstance(com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[1].third),
                0.02
            ),
            com.sundbybergsit.cromfortune.domain.StockPrice(
                com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[2].first,
                Currency.getInstance(com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[2].third),
                0.01
            ),
        ))
        ShadowLooper.runUiThreadTasks()
        listAdapter = StockOrderAggregateListAdapter(HomeViewModel(), object : FragmentManager() {}, object : StockClickListener {
            override fun onClick(stockName: String, readOnly: Boolean) {
                // Do nothing
            }
        }, false)
        val list: List<AdapterItem> = listOf(
                StockHeaderAdapterItem(),
                StockAggregateAdapterItem(getSimpleStockAggregate(
                    com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[0].first,
                        com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[0].third, 100.099)),
                StockAggregateAdapterItem(getSimpleStockAggregate(
                    com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[1].first,
                        com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[1].third, 0.0199)),
                StockAggregateAdapterItem(getSimpleStockAggregate(
                    com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[2].first,
                        com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[2].third, 0.0109)),
        )
        listAdapter.setListener(HomeViewModel())
        listAdapter.submitList(list)
    }

    @Test
    fun `onCreateViewHolder - when stock type  - returns view holder`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())

        val viewHolder = listAdapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        assertTrue(viewHolder is StockOrderAggregateListAdapter.StockOrderAggregateViewHolder)
    }

    @Test
    fun `onBindViewHolder - when stock with price over 1 - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = listAdapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        listAdapter.onBindViewHolder(viewHolder, 1)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.currency = Currency.getInstance(com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[0].third)
        numberFormat.minimumFractionDigits = 2
        numberFormat.maximumFractionDigits = 2
        assertEquals(numberFormat.format(100.10), acquisitionValue.text.toString())
    }

    // TODO: Fix this
    @Ignore("Fails in CI")
    @Test
    fun `onBindViewHolder - when stock with price under 1 and no need for third fraction - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = listAdapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        listAdapter.onBindViewHolder(viewHolder, 2)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.currency = Currency.getInstance(com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[1].third)
        numberFormat.minimumFractionDigits = 2
        numberFormat.maximumFractionDigits = 2
        assertEquals(numberFormat.format(0.02), acquisitionValue.text.toString())
    }

    // TODO: Fix this
    @Ignore("Fails in CI")
    @Test
    fun `onBindViewHolder - when stock with price under 1 and need for third fraction - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = listAdapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        listAdapter.onBindViewHolder(viewHolder, 3)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.currency = Currency.getInstance(com.sundbybergsit.cromfortune.domain.StockPrice.SYMBOLS[2].third)
        numberFormat.minimumFractionDigits = 3
        numberFormat.maximumFractionDigits = 3
        assertEquals(numberFormat.format(0.011), acquisitionValue.text.toString())
    }

    @Test
    fun `onClickMute - when not muted - mutes it`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = listAdapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)
        listAdapter.onBindViewHolder(viewHolder, 1)
        val muteUnmuteButton = viewHolder.itemView.requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute)
        val shadowDrawable = Shadow.extract<ShadowDrawable>(muteUnmuteButton.drawable)
        assumeEquals(R.drawable.ic_fas_bell, shadowDrawable.createdFromResId)
        ShadowLooper.runUiThreadTasks()

        muteUnmuteButton.performClick()
        ShadowLooper.runUiThreadTasks()

        val shadowDrawable2 = Shadow.extract<ShadowDrawable>(muteUnmuteButton.drawable)
        assertEquals(R.drawable.ic_fas_bell_slash, shadowDrawable2.createdFromResId)
    }

    private fun getSimpleStockAggregate(
            stockSymbol: String,
            currencySymbol: String,
            totalPrice: Double,
    ): StockOrderAggregate {
        val stockOrderAggregate = StockOrderAggregate(1.0, "Not important",
                stockSymbol, Currency.getInstance(currencySymbol))
        stockOrderAggregate.aggregate(
            com.sundbybergsit.cromfortune.domain.StockOrder(
                "Buy", currencySymbol, 0,
                stockSymbol, totalPrice, 0.0, 1
            )
        )
        return stockOrderAggregate
    }

}
