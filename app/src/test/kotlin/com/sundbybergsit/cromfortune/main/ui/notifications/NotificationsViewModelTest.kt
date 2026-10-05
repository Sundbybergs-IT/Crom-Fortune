package com.sundbybergsit.cromfortune.main.ui.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.main.CoroutineScopeTestRule
import com.sundbybergsit.cromfortune.main.notifications.NotificationsRepositoryImpl
import com.sundbybergsit.cromfortune.main.notifications.PREFERENCES_NAME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class NotificationsViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineScopeTestRule(setMain = true)

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var repository: NotificationsRepositoryImpl
    private lateinit var viewModel: NotificationsViewModel

    @Before
    fun setUp() {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        repository = NotificationsRepositoryImpl(context)
    }

    @Test
    fun `remove removes single notification from state and repository`() {
        val now = Instant.now().toEpochMilli()
        val todayNotification1 = NotificationMessage(
            dateInMillis = now,
            message = "Today notification 1"
        )
        val todayNotification2 = NotificationMessage(
            dateInMillis = now + 1000L,
            message = "Today notification 2"
        )
        repository.add(todayNotification1)
        repository.add(todayNotification2)

        viewModel = NotificationsViewModel(repository)

        assertEquals(2, viewModel.newNotifications.value.items.size)

        viewModel.remove(todayNotification1)

        assertEquals(1, viewModel.newNotifications.value.items.size)
        assertTrue(viewModel.newNotifications.value.items.contains(todayNotification2))
        assertEquals(1, repository.list().size)
    }

    @Test
    fun `clearNotifications removes all notifications`() {
        val now = Instant.now().toEpochMilli()
        val todayNotification = NotificationMessage(
            dateInMillis = now,
            message = "Today notification"
        )
        repository.add(todayNotification)

        viewModel = NotificationsViewModel(repository)

        assertEquals(1, viewModel.newNotifications.value.items.size)

        viewModel.clearNotifications()

        assertTrue(viewModel.newNotifications.value.items.isEmpty())
        assertTrue(repository.list().isEmpty())
    }

}
