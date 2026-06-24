package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY dateMillis ASC, startTime ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE dateMillis = :dateMillis ORDER BY startTime ASC")
    fun getEventsForDate(dateMillis: Long): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Int)

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Int): Event?

    @Query("SELECT * FROM events WHERE hasReminder = 1")
    suspend fun getEventsWithReminders(): List<Event>
}

@Dao
interface ChoreDao {
    @Query("SELECT * FROM chores ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getAllChores(): Flow<List<Chore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChore(chore: Chore)

    @Update
    suspend fun updateChore(chore: Chore)

    @Delete
    suspend fun deleteChore(chore: Chore)

    @Query("DELETE FROM chores WHERE id = :id")
    suspend fun deleteChoreById(id: Int)
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): Setting?

    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<Setting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: Setting)
}
