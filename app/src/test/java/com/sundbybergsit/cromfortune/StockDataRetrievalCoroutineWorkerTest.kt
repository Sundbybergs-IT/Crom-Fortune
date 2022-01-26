package com.sundbybergsit.cromfortune

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class StockDataRetrievalCoroutineWorkerTest {

    private lateinit var context: Context

    @get:Rule
    val cromTestRule = CromTestRule()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `doWork - always - works`() {
        val worker = TestListenableWorkerBuilder<StockDataRetrievalCoroutineWorker>(context).build()
        runBlocking {
            val result: ListenableWorker.Result = worker.doWork()
            assertTrue(result == ListenableWorker.Result.success())
        }
    }

}
