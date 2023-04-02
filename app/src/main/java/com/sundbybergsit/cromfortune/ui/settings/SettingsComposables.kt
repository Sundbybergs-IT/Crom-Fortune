package com.sundbybergsit.cromfortune.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.ButtonText

@Composable
fun Settings(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.settings_title), style = MaterialTheme.typography.h6)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back Icon")
                }
            }, actions = {
                TextButton(
                    onClick = {
                        // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
//                        val dialog = TimeIntervalStockRetrievalDialogFragment()
//                        dialog.show(parentFragmentManager, SettingsFragment.TAG)
                    }
                ) {
                    ButtonText(
                        text = stringResource(id = R.string.action_configure_stock_retrieval_intervals)
                    )
                }
                TextButton(
                    onClick = {
                        // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
//                        val dialog = SupportedStockDialogFragment()
//                        dialog.show(parentFragmentManager, SettingsFragment.TAG)
                    }
                ) {
                    ButtonText(
                        text = stringResource(id = R.string.action_stocks_supported)
                    )
                }
                TextButton(
                    onClick = {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/Sundbybergs-IT/Crom-Fortune/issues")
                        )
                        context.startActivity(browserIntent)
                    }
                ) {
                    ButtonText(
                        text = stringResource(id = R.string.generic_to_do)
                    )
                }
            }
        )
    }) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
        }
    }
}
