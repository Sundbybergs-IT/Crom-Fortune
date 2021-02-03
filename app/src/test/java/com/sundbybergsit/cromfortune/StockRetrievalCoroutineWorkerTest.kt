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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
class StockRetrievalCoroutineWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `doWork - always - works`() {
        val worker = TestListenableWorkerBuilder<StockRetrievalCoroutineWorker>(context).build()
        runBlocking {
            val result: ListenableWorker.Result = worker.doWork()
            assertTrue(result == ListenableWorker.Result.success())
        }
    }

}
