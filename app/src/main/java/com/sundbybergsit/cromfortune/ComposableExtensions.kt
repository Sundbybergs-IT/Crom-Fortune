package com.sundbybergsit.cromfortune

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@MainThread
@Composable
inline fun <reified VM : ViewModel> activityBoundViewModel(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)
): Lazy<VM> {
    val activity = LocalContext.current as ComponentActivity
    return activity.viewModels(extrasProducer = null, factoryProducer = factoryProducer)
}
