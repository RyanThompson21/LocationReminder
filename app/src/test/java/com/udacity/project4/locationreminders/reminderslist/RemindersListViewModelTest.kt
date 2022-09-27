package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    val reminder1 = ReminderDTO("title1", "description1",
        "location1", 50.0, 50.0)

    val reminder2 = ReminderDTO("title2", "description2",
        "location2", 100.0, 100.0)

    @Test
    fun testLoadReminders() {
        fakeDataSource = FakeDataSource(mutableListOf(reminder1, reminder2))
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
        fakeDataSource)
        remindersListViewModel.loadReminders()
        // check list is right size
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(2))
    }

    @Test
    fun testLoadRemindersError() {
        fakeDataSource = FakeDataSource(null)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
            fakeDataSource)

        remindersListViewModel.loadReminders()
        // check that snackbar exists and is showing correct message
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(),
            `is`("No reminders found"))
    }

    @Test
    fun testLoadingIndicator() {
        fakeDataSource = FakeDataSource(mutableListOf(reminder1, reminder2))
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
            fakeDataSource)
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        // check that loading indicator is shown
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // check indicator not showing
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @After
    fun tearDown() {
        stopKoin()
    }
}