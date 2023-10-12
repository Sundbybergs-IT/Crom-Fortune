package com.sundbybergsit.cromfortune.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.sundbybergsit.cromfortune.main.navigation.AppNavigation
import com.sundbybergsit.cromfortune.main.stocks.StockOrderRepository
import com.sundbybergsit.cromfortune.main.theme.AppTheme

class MainActivity : ComponentActivity() {

    companion object {

        private const val APP_UPDATE_REQUEST_CODE = 1711

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, String.format("onCreate(savedInstanceState=[%s])", savedInstanceState))

        setContent {
            AppTheme {
                AppNavigation(navController = rememberNavController(), portfolioRepository = PortfolioRepository)
            }
        }

        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo, this, AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
                    APP_UPDATE_REQUEST_CODE
                )
            }
        }
        val reviewManager = ReviewManagerFactory.create(this)
        appUpdateManager.registerListener(UpdateInstallStateUpdatedListener(this))
        if (StockOrderRepository(this, porfolioName = PortfolioRepository.DEFAULT_PORTFOLIO_NAME).countAll() > 4) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        } else {
            Log.w(TAG, "Unknown activity result. Ignoring.")
        }
    }

    class UpdateInstallStateUpdatedListener(private val activity: Activity) : InstallStateUpdatedListener {

        override fun onStateUpdate(state: InstallState) {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            }
        }

        /* Displays the snackbar notification and call to action. */
        private fun popupSnackbarForCompleteUpdate() {
            DialogHandler.showSnack(activity.getString(R.string.generic_update_completed))
        }

    }

}
