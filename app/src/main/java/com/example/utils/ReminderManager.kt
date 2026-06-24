package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.CalendarRepository
import com.example.data.Event
import com.example.receiver.ReminderReceiver
import java.util.Calendar

object ReminderManager {
    private const val TAG = "ReminderManager"

    fun calculateTriggerTime(event: Event): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = event.dateMillis

        // Parse startTime "HH:mm"
        val parts = event.startTime.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: 0
            val minute = parts[1].toIntOrNull() ?: 0
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
        }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Subtract reminder minutes offset
        calendar.add(Calendar.MINUTE, -event.reminderMinutesBefore)
        return calendar.timeInMillis
    }

    fun scheduleReminder(context: Context, event: Event) {
        if (!event.hasReminder) {
            cancelReminder(context, event)
            return
        }

        val triggerTime = calculateTriggerTime(event)
        if (triggerTime <= System.currentTimeMillis()) {
            Log.d(TAG, "Not scheduling for past event: ${event.title}")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(ReminderReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(ReminderReceiver.EXTRA_EVENT_NOTE, event.note)
            putExtra(ReminderReceiver.EXTRA_EVENT_COLOR, event.colorHex)
            putExtra(ReminderReceiver.EXTRA_EVENT_CATEGORY, event.category)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id,
            intent,
            flags
        )

        try {
            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact alarm for ${event.title} at $triggerTime")
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled inexact alarm for ${event.title} at $triggerTime")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule exact alarm due to SecurityException, falling back", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    fun cancelReminder(context: Context, event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
        }

        val flags = PendingIntent.FLAG_NO_CREATE or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        // Check if pending intent exists
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id,
            intent,
            flags
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Canceled reminder alarm for ${event.title}")
        }
    }

    suspend fun rescheduleAll(context: Context, repository: CalendarRepository) {
        Log.d(TAG, "Rescheduling all reminders...")
        val events = repository.getEventsWithReminders()
        val now = System.currentTimeMillis()
        var scheduledCount = 0
        for (event in events) {
            val triggerTime = calculateTriggerTime(event)
            if (triggerTime > now) {
                scheduleReminder(context, event)
                scheduledCount++
            } else {
                cancelReminder(context, event)
            }
        }
        Log.d(TAG, "Rescheduled $scheduledCount active reminders out of ${events.size}")
    }
}
