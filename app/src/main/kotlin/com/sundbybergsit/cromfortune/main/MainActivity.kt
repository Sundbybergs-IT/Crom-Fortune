package com.sundbybergsit.cromfortune.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.review.ReviewManagerFactory
import com.sundbybergsit.cromfortune.main.navigation.AppNavigation
import com.sundbybergsit.cromfortune.main.stocks.StockOrderRepository
import com.sundbybergsit.cromfortune.main.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, String.format("onCreate(savedInstanceState=[%s])", savedInstanceState))

        setContent {
            AppTheme {
                AppNavigation(navController = rememberNavController(), portfolioRepository = PortfolioRepository)
            }
        }

        val reviewManager = ReviewManagerFactory.create(this)
        if (StockOrderRepository(this, portfolioName = PortfolioRepository.DEFAULT_PORTFOLIO_NAME).countAll() > 4) {
            Log.i(TAG, "Time to nag about reviews! :-)")
            val request = reviewManager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    reviewInfo.let {
                        Log.i(TAG, "Launching review flow!")
                        val flow = reviewManager.launchReviewFlow(this@MainActivity, it)
                        flow.addOnCompleteListener {
                            //Irrespective of the result, the app flow should continue
                        }
                    }
                } else {
                    Log.e(TAG, "Could not retrieve reviewInfo", task.exception)
                }
            }
        }
    }

}

