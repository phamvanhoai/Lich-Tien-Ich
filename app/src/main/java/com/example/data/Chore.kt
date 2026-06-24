package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chores")
data class Chore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // e.g., Work, School, House, Family, Sport
    val isCompleted: Boolean = false,
    val dueDateMillis: Long = System.currentTimeMillis()
)
