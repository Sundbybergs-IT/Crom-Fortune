package com.sundbybergsit.cromfortune.ui.home

import android.os.Build
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.assumeEquals
import com.sundbybergsit.cromfortune.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockPrice
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowDrawable
import org.robolectric.shadows.ShadowLooper
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class StockOrderAggregateListAdapterTest {

    private lateinit var adapterOrderAggregate: StockOrderAggregateListAdapter

    private val currency: Currency = Currency.getInstance("SEK")

    @Before
    fun setUp() {
        CurrencyRateRepository.add(setOf(CurrencyRate("SEK", 1.0)))
        StockPriceRepository.put(setOf(
                StockPrice(StockPrice.SYMBOLS[0].first, currency, 100.0),
                StockPrice(StockPrice.SYMBOLS[1].first, currency, 0.02),
                StockPrice(StockPrice.SYMBOLS[2].first, currency, 0.01),
        ))
        ShadowLooper.runUiThreadTasks()
        adapterOrderAggregate = StockOrderAggregateListAdapter(object : StockClickListener {
            override fun onClick(stockName: String) {
                // Do nothing
            }
        })
        val list: List<AdapterItem> = listOf(
                StockHeaderAdapterItem(),
                StockAggregateAdapterItem(StockOrderAggregate(1.0, StockPrice.SYMBOLS[0].first, StockPrice.SYMBOLS[0].first, currency, 100.099, 0.0, 1)),
                StockAggregateAdapterItem(StockOrderAggregate(1.0, StockPrice.SYMBOLS[1].first, StockPrice.SYMBOLS[1].first, currency, 0.0199, 0.0, 1)),
                StockAggregateAdapterItem(StockOrderAggregate(1.0, StockPrice.SYMBOLS[2].first, StockPrice.SYMBOLS[2].first, currency, 0.0109, 0.0, 1)),
        )
        adapterOrderAggregate.setListener(HomeViewModel())
        adapterOrderAggregate.submitList(list)
    }

    @Test
    fun `onCreateViewHolder - when stock type  - returns view holder`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())

        val viewHolder = adapterOrderAggregate.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        assertTrue(viewHolder is StockOrderAggregateListAdapter.StockOrderAggregateViewHolder)
    }

    @Test
    fun `onBindViewHolder - when stock with price over 1 - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapterOrderAggregate.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        adapterOrderAggregate.onBindViewHolder(viewHolder, 1)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        assertEquals("${currency}100.10", acquisitionValue.text.toString())
    }

    @Test
    fun `onBindViewHolder - when stock with price under 1 and no need for third fraction - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapterOrderAggregate.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        adapterOrderAggregate.onBindViewHolder(viewHolder, 2)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        assertEquals("${currency}0.02", acquisitionValue.text.toString())
    }

    @Test
    fun `onBindViewHolder - when stock with price under 1 and need for third fraction - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapterOrderAggregate.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        adapterOrderAggregate.onBindViewHolder(viewHolder, 3)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        assertEquals("${currency}0.011", acquisitionValue.text.toString())
    }

    @Test
    fun `onClickMute - when not muted - mutes it`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapterOrderAggregate.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)
        adapterOrderAggregate.onBindViewHolder(viewHolder, 1)
        val muteUnmuteButton = viewHolder.itemView.requireViewById<ImageButton>(R.id.imageButton_listrowStockItem_muteUnmute)
        val shadowDrawable = Shadow.extract<ShadowDrawable>(muteUnmuteButton.drawable)
        assumeEquals(R.drawable.ic_fas_bell, shadowDrawable.createdFromResId)
        ShadowLooper.runUiThreadTasks()

        muteUnmuteButton.performClick()
        ShadowLooper.runUiThreadTasks()

        val shadowDrawable2 = Shadow.extract<ShadowDrawable>(muteUnmuteButton.drawable)
        assertEquals(R.drawable.ic_fas_bell_slash, shadowDrawable2.createdFromResId)
    }

}
