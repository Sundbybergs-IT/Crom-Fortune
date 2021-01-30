package com.sundbybergsit.cromfortune.ui.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl

class SettingsViewModel : ViewModel() {

    fun allStocks(context: Context): Iterable<String> {
        val stockOrderRepository = StockOrderRepositoryImpl(context)
        return stockOrderRepository.listOfStockNames()
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is settings Fragment"
    }
    val text: LiveData<String> = _text

    private val _todoText = MutableLiveData<String>().apply {
        value = "Att göra: Stöd för att ställa in standardvärden (courtage och valuta), " +
                "Ändra pollning till 1 gång per timme," +
                "förbättra så Croms vilja tar hänsyn till hur stora köp/försäljningar som gjordes"
    }
    val todoText: LiveData<String> = _todoText
}
