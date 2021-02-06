package com.sundbybergsit.cromfortune

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
class StockDataRetrievalCoroutineWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `doWork - always - works`() {
        val worker = TestListenableWorkerBuilder<TestableStockDataRetrievalCoroutineWorker>(context).build()
        runBlocking {
            val result: ListenableWorker.Result = worker.doWork()
            assertTrue(result == ListenableWorker.Result.success())
        }
    }

    class TestableStockDataRetrievalCoroutineWorker(context: Context, workerParameters: WorkerParameters) :
            StockDataRetrievalCoroutineWorker(context, workerParameters) {

        override fun getRateInSek(currency: String): Double {
            return 1.0
        }

    }

}
