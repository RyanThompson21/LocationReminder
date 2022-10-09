package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource() : ReminderDataSource {

    var remindersList: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    private var shouldReturnError = false

    fun setShouldReturnError(error: Boolean) {
        shouldReturnError = error
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("error retrieving reminders")
        }
        return Result.Success(remindersList.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("error retrieving reminder")
        }
        val reminder = remindersList.getValue(id)
        if (reminder != null) {
            return Result.Success(remindersList.getValue(id))
        }
        else {
            Result.Error("Reminder not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }

}