package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.CalendarRepository
import com.example.utils.ReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "onReceive: Action = $action")

        when (action) {
            ACTION_REMINDER -> {
                handleReminderTrigger(context, intent)
            }
            Intent.ACTION_BOOT_COMPLETED, 
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                handleReboot(context)
            }
        }
    }

    private fun handleReminderTrigger(context: Context, intent: Intent) {
        val eventId = intent.getIntExtra(EXTRA_EVENT_ID, 0)
        val title = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "Sự kiện sắp diễn ra"
        val note = intent.getStringExtra(EXTRA_EVENT_NOTE) ?: ""
        val category = intent.getStringExtra(EXTRA_EVENT_CATEGORY) ?: "Sự kiện"
        val colorHex = intent.getStringExtra(EXTRA_EVENT_COLOR) ?: "#D32F2F"

        showNotification(context, eventId, title, note, category, colorHex)
    }

    private fun handleReboot(context: Context) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val repository = CalendarRepository(db)
                ReminderManager.rescheduleAll(context, repository)
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling reminders on boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        eventId: Int,
        title: String,
        note: String,
        category: String,
        colorHex: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc nhở lịch hẹn",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh thông báo nhắc nhở khi đến sự kiện, lịch trình đã lưu"
                enableLights(true)
                lightColor = Color.parseColor(colorHex)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action: open MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_event_id", eventId)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId,
            openIntent,
            flags
        )

        // Parse custom color of event to decorate notification
        val colorInt = try {
            Color.parseColor(colorHex)
        } catch (e: Exception) {
            Color.RED
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon) // Use launcher icon
            .setContentTitle(title)
            .setContentText(if (note.isNotEmpty()) note else "Đến giờ hẹn sự kiện: $category")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                if (note.isNotEmpty()) "$note\n\nDanh mục: $category" else "Đến giờ hẹn sự kiện: $category"
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setColor(colorInt)
            .setColorized(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(eventId, notificationBuilder.build())
        Log.d(TAG, "Notification shown for event ID: $eventId")
    }

    companion object {
        private const val TAG = "ReminderReceiver"
        
        const val CHANNEL_ID = "calendar_reminders_channel"
        const val ACTION_REMINDER = "com.hovait.lich.ACTION_REMINDER"
        
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_EVENT_TITLE = "event_title"
        const val EXTRA_EVENT_NOTE = "event_note"
        const val EXTRA_EVENT_COLOR = "event_color"
        const val EXTRA_EVENT_CATEGORY = "event_category"
    }
}
