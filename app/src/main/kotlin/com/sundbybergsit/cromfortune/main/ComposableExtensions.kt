package com.sundbybergsit.cromfortune.main

import android.content.Context
import android.content.ContextWrapper
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
    val activity = LocalContext.current.findComponentActivity()
    return activity.viewModels(extrasProducer = null, factoryProducer = factoryProducer)
}

tailrec fun Context.findComponentActivity(): ComponentActivity {
    return when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findComponentActivity()
        else -> throw IllegalStateException("Expected a ComponentActivity context but found ${this::class.java.name}")
    }
}
