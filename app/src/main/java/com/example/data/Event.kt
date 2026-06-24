package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val note: String = "",
    val category: String, // Work, Study, Chore, Holiday, Family, Appointment
    val colorHex: String,
    val dateMillis: Long,
    val startTime: String, // e.g., "09:00"
    val endTime: String,   // e.g., "10:00"
    val url: String = "",
    val location: String = "",
    val hasReminder: Boolean = false,
    val reminderMinutesBefore: Int = 15
)
