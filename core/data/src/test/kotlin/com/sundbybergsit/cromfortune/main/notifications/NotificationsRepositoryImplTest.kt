package com.sundbybergsit.cromfortune.main.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class NotificationsRepositoryImplTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var repository: NotificationsRepositoryImpl

    @Before
    fun setUp() {
        repository = NotificationsRepositoryImpl(context)
        repository.clear()
    }

    @Test
    fun `add adds notification to list`() {
        val notification = NotificationMessage(
            dateInMillis = 1000L,
            message = "Test message"
        )

        repository.add(notification)

        val list = repository.list()
        assertEquals(1, list.size)
        assertTrue(list.contains(notification))
    }

    @Test
    fun `remove deletes notification from list`() {
        val notification1 = NotificationMessage(
            dateInMillis = 1000L,
            message = "Test message 1"
        )
        val notification2 = NotificationMessage(
            dateInMillis = 2000L,
            message = "Test message 2"
        )
        repository.add(notification1)
        repository.add(notification2)

        repository.remove(notification1)

        val list = repository.list()
        assertEquals(1, list.size)
        assertTrue(list.contains(notification2))
    }

    @Test
    fun `clear removes all notifications`() {
        val notification1 = NotificationMessage(
            dateInMillis = 1000L,
            message = "Test message 1"
        )
        repository.add(notification1)

        repository.clear()

        assertTrue(repository.list().isEmpty())
    }

}
