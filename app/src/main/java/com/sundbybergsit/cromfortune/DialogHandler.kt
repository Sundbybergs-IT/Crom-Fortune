package com.sundbybergsit.cromfortune

import android.util.Log
import androidx.compose.material.SnackbarData
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DialogHandler {

    private const val TAG = "DialogHandler"

    private val _snackbarFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    val snackbarFlow = _snackbarFlow as StateFlow<String?>

    fun showSnack(text: String) {
        Log.i(TAG, "showSnack(text=[${text}])")
        _snackbarFlow.value = text
    }

    @JvmStatic
    fun acknowledgeSnack() {
        Log.i(TAG, "acknowledgeSnack()")
        _snackbarFlow.value = null
    }

}
