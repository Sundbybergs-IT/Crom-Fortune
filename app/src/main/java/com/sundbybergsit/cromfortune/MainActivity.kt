package com.sundbybergsit.cromfortune

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : AppCompatActivity() {

    companion object {

        const val TAG: String = "MainActivity"
        private const val APP_UPDATE_REQUEST_CODE = 1711

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_settings))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this,
                        APP_UPDATE_REQUEST_CODE)
            }
        }
        appUpdateManager.registerListener(UpdateInstallStateUpdatedListener(this, appUpdateManager))
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

    class UpdateInstallStateUpdatedListener(
            private val activity: Activity,
            private val appUpdateManager: AppUpdateManager,
    ) : InstallStateUpdatedListener {

        override fun onStateUpdate(state: InstallState) {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            }
        }

        /* Displays the snackbar notification and call to action. */
        private fun popupSnackbarForCompleteUpdate() {
            Snackbar.make(
                    activity.findViewById(R.id.coordinatorLayout_activityMain),
                    activity.getString(R.string.generic_update_completed),
                    Snackbar.LENGTH_INDEFINITE
            ).apply {
                setAction("RESTART") { appUpdateManager.completeUpdate() }
                setActionTextColor(activity.resources.getColor(R.color.colorAccent, null))
                show()
            }
        }

    }

}
