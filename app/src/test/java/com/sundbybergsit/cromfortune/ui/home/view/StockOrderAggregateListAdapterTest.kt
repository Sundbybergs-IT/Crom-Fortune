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
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.stocks.StockPriceListener
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.ui.home.HomeViewModel
import com.sundbybergsit.cromfortune.ui.home.NameAndValueHeaderAdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockAggregateAdapterItem
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

    private val dummyStockPriceListener : StockPriceListener = object : StockPriceListener {
        override fun getStockPrice(stockSymbol: String): StockPrice {
            return StockPrice(stockSymbol, Currency.getInstance("SEK"), 0.0)
        }
    }

    @Before
    fun setUp() {
        CurrencyRateRepository.add(setOf(CurrencyRate("SEK", 1.0)))
        StockPriceRepository.put(
            setOf(
                StockPrice(
                    StockPrice.SYMBOLS[0].first,
                    Currency.getInstance(StockPrice.SYMBOLS[0].third),
                    100.0
                ),
                StockPrice(
                    StockPrice.SYMBOLS[1].first,
                    Currency.getInstance(StockPrice.SYMBOLS[1].third),
                    0.02
                ),
                StockPrice(
                    StockPrice.SYMBOLS[2].first,
                    Currency.getInstance(StockPrice.SYMBOLS[2].third),
                    0.01
                ),
            )
        )
        ShadowLooper.runUiThreadTasks()
        listAdapter =
            StockOrderAggregateListAdapter(HomeViewModel(), object : FragmentManager() {}, object : StockClickListener {
                override fun onClick(stockName: String, readOnly: Boolean) {
                    // Do nothing
                }
            }, false)
        val list: List<NameAndValueAdapterItem> = listOf(
            NameAndValueHeaderAdapterItem(),
            StockAggregateAdapterItem(
                dummyStockPriceListener, getSimpleStockAggregate(
                    StockPrice.SYMBOLS[0].first,
                    StockPrice.SYMBOLS[0].third, 100.099
                )
            ),
            StockAggregateAdapterItem(
                dummyStockPriceListener, getSimpleStockAggregate(
                    StockPrice.SYMBOLS[1].first,
                    StockPrice.SYMBOLS[1].third, 0.0199
                )
            ),
            StockAggregateAdapterItem(
                dummyStockPriceListener, getSimpleStockAggregate(
                    StockPrice.SYMBOLS[2].first,
                    StockPrice.SYMBOLS[2].third, 0.0109
                )
            ),
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

        val acquisitionValue =
            viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.currency = Currency.getInstance(StockPrice.SYMBOLS[0].third)
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

        val acquisitionValue =
            viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.currency = Currency.getInstance(StockPrice.SYMBOLS[1].third)
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

        val acquisitionValue =
            viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.currency = Currency.getInstance(StockPrice.SYMBOLS[2].third)
        numberFormat.minimumFractionDigits = 3
        numberFormat.maximumFractionDigits = 3
        assertEquals(numberFormat.format(0.011), acquisitionValue.text.toString())
    }

    @Test
    fun `onClickMute - when not muted - mutes it`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = listAdapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)
        listAdapter.onBindViewHolder(viewHolder, 1)
        val muteUnmuteButton =
            viewHolder.itemView.requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute)
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
        val stockOrderAggregate = StockOrderAggregate(
            1.0, "Not important",
            stockSymbol, Currency.getInstance(currencySymbol)
        )
        stockOrderAggregate.aggregate(
            StockOrder(
                "Buy", currencySymbol, 0,
                stockSymbol, totalPrice, 0.0, 1
            ).toStockEvent()
        )
        return stockOrderAggregate
    }

}
