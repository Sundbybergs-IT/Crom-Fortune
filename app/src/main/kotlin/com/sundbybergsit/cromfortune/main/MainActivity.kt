package com.sundbybergsit.cromfortune.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
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
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    Log.d(TAG, "Update was ok.")
                } else {
                    Log.d(TAG, "Update flow failed! Result code: " + result.resultCode)
                }
            }
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                )
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

