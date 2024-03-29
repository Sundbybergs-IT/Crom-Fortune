package com.sundbybergsit.cromfortune.main

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

fun Modifier.contentDescription(contentDescription: String) =
    this.semantics { this.contentDescription = contentDescription }
