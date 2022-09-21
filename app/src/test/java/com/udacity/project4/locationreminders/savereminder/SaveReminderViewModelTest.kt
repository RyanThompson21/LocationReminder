package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    val reminder1 = ReminderDataItem("title1", "description1",
        "location1", 50.0, 50.0)


    @Test
    fun testSaveReminder() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),
            fakeDataSource)
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder1)

        // check that loading indicatior is shown
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        // check indicator no longer showing
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        // check that the success toast message was shown
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(),
            `is`("Reminder Saved !"))
    }

    @Test
    fun testValidate() {
        fakeDataSource = FakeDataSource(null)
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),
            fakeDataSource)
        reminder1.title = null
        saveReminderViewModel.validateEnteredData(reminder1)
        // check correct snackbar error msg shows with null title
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))

        reminder1.title = ""
        saveReminderViewModel.validateEnteredData(reminder1)
        // check correct snackbar error msg shows with empty title
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))

        reminder1.title = "asdf" // set title to make sure location error message shows
        reminder1.location = null
        saveReminderViewModel.validateEnteredData(reminder1)
        // check correct snackbar error msg shows with null location
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))

        reminder1.location = ""
        saveReminderViewModel.validateEnteredData(reminder1)
        // check correct snackbar error msg shows with empty location
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}