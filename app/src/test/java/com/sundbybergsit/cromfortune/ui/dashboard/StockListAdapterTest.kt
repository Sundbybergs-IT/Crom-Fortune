package com.sundbybergsit.cromfortune.ui.dashboard

import android.os.Build
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.home.AdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockAdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockHeaderAdapterItem
import com.sundbybergsit.cromfortune.ui.home.StockOrder
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
        adapter = StockListAdapter()
        val currency = currency
        val list: List<AdapterItem> = listOf(StockHeaderAdapterItem(),
                StockAdapterItem(StockOrder("Buy", currency.toString(), 0L, "Stock.A", 100.099, 0.0, 1)),
                StockAdapterItem(StockOrder("Buy", currency.toString(), 0L, "Stock.B", 0.0199, 0.0, 1)),
        )
        adapter.submitList(list)
    }

    @Test
    fun `onCreateViewHolder - when stock type  - returns view holder`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())

        val viewHolder = adapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock)

        assertTrue(viewHolder is StockListAdapter.StockViewHolder)
    }

    @Test
    fun `onBindViewHolder - when stock with price over 1 - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock)

        adapter.onBindViewHolder(viewHolder, 1)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStock_acquisitionValue)
        assertEquals("100.10 $currency", acquisitionValue.text.toString())
    }

    @Test
    fun `onBindViewHolder - when stock with price under 1 - shows correct price`() {
        val frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapter.onCreateViewHolder(frameLayout, R.layout.listrow_stock)

        adapter.onBindViewHolder(viewHolder, 2)

        val acquisitionValue = viewHolder.itemView.findViewById<TextView>(R.id.textView_listrowStock_acquisitionValue)
        assertEquals("0.020 $currency", acquisitionValue.text.toString())
    }

}
