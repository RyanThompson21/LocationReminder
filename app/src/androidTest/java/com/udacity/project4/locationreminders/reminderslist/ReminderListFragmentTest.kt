package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.FakeDataSource
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import org.koin.test.get
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.test.KoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest{

    @get:Rule
    val instantExecutor = InstantTaskExecutorRule()

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        //stop the original app koin
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as FakeDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as FakeDataSource
                )
            }
            single { FakeDataSource() }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our fake repository
        repository = get() as FakeDataSource

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    // test passes but make sure you're logged in before you run the test or it will crash
    fun checkRemindersDisplayed() = runBlockingTest {
        // add reminder to repo
        val reminder = ReminderDTO("title", "descrip", "Locale",
            10.0, 50.0)
        repository.saveReminder(reminder)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText("title"))))
    }

    @Test
    fun checkNoReminders_DisplaysNoData() = runBlockingTest {
        repository.deleteAllReminders() // clear repo
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        // test for snackbar message is in RemindersListViewModelTest
        onView(withId(R.id.noDataTextView))
            .check(matches(withText("No Data")))
    }

    @Test
    fun addReminderFabClick_navigateToSaveReminder() {
        val navController = mock(NavController::class.java)
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme).onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // click the button
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed())).perform(click())
        // check destination was correct
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}