package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO("title", "description",
        "location", 50.0, 50.0)
    private val reminder2 = ReminderDTO("title2", "description2",
        "location2", 150.0, 150.0)

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun testGetReminders() = runBlockingTest{
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        val list = database.reminderDao().getReminders()

        assertThat(list.size, `is` (2))
        assertThat(list[0].title, `is`("title"))
        assertThat(list[1].title, `is`("title2"))
        assertThat(list, hasItems(reminder1, reminder2))
    }

    @Test
    fun testGetReminders_EmptyList() = runBlockingTest{
        val list = database.reminderDao().getReminders()

        assertThat(list.size, `is` (0))
        assertThat(list, `is` (emptyList()))
    }

    @Test
    fun testGetReminderById () = runBlockingTest {
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        val rem = database.reminderDao().getReminderById(reminder1.id)

        assertThat(rem?.id, `is` (reminder1.id))
        assertThat(rem?.title, `is`("title"))
    }

    @Test
    fun testGetReminderById_EmptyList () = runBlockingTest {
        val rem = database.reminderDao().getReminderById(reminder1.id)

        assertThat(rem?.id, `is` (nullValue()))
    }

    @Test
    fun testDeleteAllReminders() = runBlockingTest {
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        database.reminderDao().deleteAllReminders()
        val list = database.reminderDao().getReminders()

        assertThat(list, `is` (emptyList()))
    }
}