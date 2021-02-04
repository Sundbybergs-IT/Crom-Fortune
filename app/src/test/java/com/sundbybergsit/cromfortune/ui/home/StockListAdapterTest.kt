package com.sundbybergsit.cromfortune.ui.home

import android.os.Build
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
class StockListAdapterTest {

    private lateinit var adapter: StockListAdapter

    private val currency: Currency = Currency.getInstance("SEK")

    @Before
    fun setUp() {
        adapter = StockListAdapter(object : StockClickListener {
            override fun onClick(stockName: String) {
                // Do nothing
            }
        }, StubbedCurrencyConversionRateProducer())
        val currency = currency
        val list: List<AdapterItem> = listOf(StockHeaderAdapterItem(),
                StockAdapterItem(StockOrder("Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first, 100.099, 0.0, 1)),
                StockAdapterItem(StockOrder("Buy", currency.toString(), 0L, StockPrice.SYMBOLS[1].first, 0.0199, 0.0, 1)),
                StockAdapterItem(StockOrder("Buy", currency.toString(), 0L, StockPrice.SYMBOLS[2].first, 0.0109, 0.0, 1)),
        )
        adapter.setListener(HomeViewModel())
        adapter.submitList(list)
    }

    @Test
    fun `onCreateViewHolder - when stock type  - returns view holder`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())

        val viewHolder = adapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        assertTrue(viewHolder is StockListAdapter.StockViewHolder)
    }

    @Test
    fun `onBindViewHolder - when stock with price over 1 - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        adapter.onBindViewHolder(viewHolder, 1)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        assertEquals("${currency}100.10", acquisitionValue.text.toString())
    }

    @Test
    fun `onBindViewHolder - when stock with price under 1 and no need for third fraction - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        adapter.onBindViewHolder(viewHolder, 2)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        assertEquals("${currency}0.02", acquisitionValue.text.toString())
    }

    @Test
    fun `onBindViewHolder - when stock with price under 1 and need for third fraction - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock_item)

        adapter.onBindViewHolder(viewHolder, 3)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStockItem_acquisitionValue)
        assertEquals("${currency}0.011", acquisitionValue.text.toString())
    }

    class StubbedCurrencyConversionRateProducer : CurrencyConversionRateProducer(ApplicationProvider.getApplicationContext()) {

        override fun getRateInSek(currency: Currency): Double {
            return 1.0
        }

    }

}
