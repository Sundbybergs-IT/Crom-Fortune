package com.sundbybergsit.cromfortune.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.sundbybergsit.cromfortune.main.navigation.AppNavigation
import com.sundbybergsit.cromfortune.main.stocks.AssetTransactionRepository
import com.sundbybergsit.cromfortune.main.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val REVIEW_PROMPT_DELAY_MILLIS = 10_000L

class MainActivity : ComponentActivity(), Taggable {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Log.v(TAG, String.format("onCreate(savedInstanceState=[%s])", savedInstanceState))

        setContent {
            AppTheme {
                AppNavigation(portfolioRepository = PortfolioRepository)
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
        var updateFlowStarted = false
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                updateFlowStarted = true
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                )
            }
        }
        appUpdateInfoTask.addOnCompleteListener {
            if (!updateFlowStarted) {
                scheduleReviewPrompt()
            }
        }
    }

    private fun scheduleReviewPrompt() {
        val policy = ReviewPromptPolicy(
            preferences = getSharedPreferences(ReviewPromptPolicy.PREFERENCES_NAME, Context.MODE_PRIVATE),
            firstInstallTimeMillis = packageManager.getPackageInfo(packageName, 0).firstInstallTime
        )
        val assetCount = AssetTransactionRepository(
            this,
            portfolioName = PortfolioRepository.DEFAULT_PORTFOLIO_NAME
        ).assetIds().size

        if (!policy.isEligible(assetCount)) return

        lifecycleScope.launch {
            delay(REVIEW_PROMPT_DELAY_MILLIS)
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return@launch
            if (!policy.isEligible(assetCount)) return@launch

            // Play Review does not report whether the dialog was shown or a review was submitted,
            // so every request must count as an attempt.
            policy.recordAttempt()
            requestReview()
        }
    }

    private fun requestReview() {
        val reviewManager = ReviewManagerFactory.create(this)
        Log.i(TAG, "Requesting the in-app review flow")
        reviewManager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewManager.launchReviewFlow(this@MainActivity, task.result)
                    .addOnCompleteListener {
                        // The API deliberately does not disclose whether a review was submitted.
                    }
            } else {
                Log.e(TAG, "Could not retrieve reviewInfo", task.exception)
            }
        }
    }
}
