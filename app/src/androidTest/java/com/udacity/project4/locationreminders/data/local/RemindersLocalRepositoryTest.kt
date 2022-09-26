package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var localRepository: RemindersLocalRepository

    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO("title", "description",
        "location", 50.0, 50.0)
    private val reminder2 = ReminderDTO("title2", "description2",
        "location2", 150.0, 150.0)

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java).build()

        localRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun testGetReminders() = runBlockingTest {
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)

        val result = (localRepository.getReminders() as Result.Success).data

        assertThat(result.size, `is` (2))
        assertThat(result.contains(reminder1), `is` (true))
        assertThat(result.contains(reminder2), `is` (true))
    }

    @Test
    fun testGetReminders_EmptyList() = runBlockingTest {
        val result = localRepository.getReminders()
        result as Result.Success // success because you're still returning a list, its just empty
        assertEquals(result.data.size, 0)
    }

    @Test
    fun testGetReminderById() = runBlockingTest {
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)

        val result = localRepository.getReminder(reminder1.id) as Result.Success

        assertEquals(result.data.id, reminder1.id)
    }

    @Test
    fun testGetReminderById_Error() = runBlockingTest {
        localRepository.saveReminder(reminder2)
        val result = localRepository.getReminder(reminder1.id) as Result.Error

        assertEquals(result.message, "Reminder not found!")
    }

    @Test
    fun testDeleteAll() = runBlockingTest {
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)

        localRepository.deleteAllReminders()

        val result = (localRepository.getReminders() as Result.Success).data

        assertEquals(result.size, 0)
        assertThat(result.contains(reminder1), `is` (false))
        assertThat(result.contains(reminder2), `is` (false))
    }

}