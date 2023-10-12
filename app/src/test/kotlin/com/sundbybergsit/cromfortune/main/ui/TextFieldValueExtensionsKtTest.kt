package com.sundbybergsit.cromfortune.main.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.main.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class TextFieldValueExtensionsKtTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test(expected = ValidatorException::class)
    fun `validateDate - invalid date - throws ValidatorException`() {
        TextFieldValue("yodhnf").validateDate(
            context = context,
            errorMutableState = mutableStateOf(false),
            errorMessageMutableState = mutableStateOf(""),
            pattern = "MM/dd/yyyy"
        )
    }

    @Test
    fun `validateDate - invalid date - sets correct values`() {
        val errorMutableState = mutableStateOf(false)
        val errorMessageMutableState = mutableStateOf("")

        try {
            TextFieldValue("yodhnf").validateDate(
                context = context,
                errorMutableState = errorMutableState,
                errorMessageMutableState = errorMessageMutableState,
                pattern = "MM/dd/yyyy"
            )
        } catch (e: ValidatorException) {
            // Expected
        }

        assertTrue(errorMutableState.value)
        assertEquals(
            expected = context.getString(R.string.generic_error_invalid_date),
            actual = errorMessageMutableState.value
        )
    }

    @Test
    fun `validateDate - valid date - sets correct values`() {
        val errorMutableState = mutableStateOf(true)
        val errorMessageMutableState = mutableStateOf("old error")

        TextFieldValue("01/20/2020").validateDate(
            context = context,
            errorMutableState = errorMutableState,
            errorMessageMutableState = errorMessageMutableState,
            pattern = "MM/dd/yyyy"
        )

        assertFalse(errorMutableState.value)
        assertEquals(expected = "", actual = errorMessageMutableState.value)
    }

}
