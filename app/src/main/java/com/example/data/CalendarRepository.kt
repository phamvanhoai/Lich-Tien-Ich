package com.example.data

import kotlinx.coroutines.flow.Flow

class CalendarRepository(private val db: AppDatabase) {
    private val eventDao = db.eventDao()
    private val choreDao = db.choreDao()
    private val settingDao = db.settingDao()

    val allEvents: Flow<List<Event>> = eventDao.getAllEvents()
    val allChores: Flow<List<Chore>> = choreDao.getAllChores()
    val allSettings: Flow<List<Setting>> = settingDao.getAllSettings()

    fun getEventsForDate(dateMillis: Long): Flow<List<Event>> = eventDao.getEventsForDate(dateMillis)

    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
    suspend fun deleteEventById(id: Int) = eventDao.deleteEventById(id)
    suspend fun getEventById(id: Int): Event? = eventDao.getEventById(id)
    suspend fun getEventsWithReminders(): List<Event> = eventDao.getEventsWithReminders()

    suspend fun insertChore(chore: Chore) = choreDao.insertChore(chore)
    suspend fun updateChore(chore: Chore) = choreDao.updateChore(chore)
    suspend fun deleteChore(chore: Chore) = choreDao.deleteChore(chore)
    suspend fun deleteChoreById(id: Int) = choreDao.deleteChoreById(id)

    suspend fun getSetting(key: String): Setting? = settingDao.getSetting(key)
    suspend fun saveSetting(key: String, value: String) {
        settingDao.insertSetting(Setting(key, value))
    }
}
