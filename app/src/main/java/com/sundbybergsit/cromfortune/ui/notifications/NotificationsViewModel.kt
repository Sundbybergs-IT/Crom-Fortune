package com.sundbybergsit.cromfortune.ui.notifications

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.ui.home.CromFortuneV1AlgorithmConformanceScoreCalculator
import com.sundbybergsit.cromfortune.ui.home.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.ui.home.StockOrder
import com.sundbybergsit.cromfortune.ui.home.StockOrderRepositoryImpl
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Att göra: Stöd för att ställa in standardvärden (courtage och valuta), " +
                "Ändra pollning till 1 gång per timme, lagring av rekommendationer, logik för att kontrollera om rekommendationer har följts, analys av historisk data sen första köpet," +
                "en lista över alla aktiesymboler, notifieringar..."
    }
    val text: LiveData<String> = _text
    private val _score = MutableLiveData<String>().apply {
        value = ""
    }
    val score: LiveData<String> = _score

    fun refreshScore(context: Context) {
        viewModelScope.launch {
            val repository = StockOrderRepositoryImpl(context)
            val latestScore = CromFortuneV1AlgorithmConformanceScoreCalculator().getScore(
                    CromFortuneV1RecommendationAlgorithm(context, repository), stocks(repository).toSet())
            _score.postValue("Du följer Croms vilja till " + latestScore.score + "%")
        }
    }

    private fun stocks(repository: StockOrderRepositoryImpl): List<StockOrder> {
        val stocks = mutableListOf<StockOrder>()
        for (stockName in repository.listOfStockNames()) {
            for (entry in repository.list(stockName)) {
                stocks.add(entry)
            }
        }
        return stocks
    }

}
