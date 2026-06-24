package com.virginactive.android.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlin.time.Instant

object ReminderScheduler {

    const val CHANNEL_ID = "class_reminders"
    private const val CHANNEL_NAME = "Class reminders"

    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_TIME = "extra_time"

    private val LEAD_MILLIS = 2L * 60L * 60L * 1000L

    fun schedule(
        context: Context,
        classId: String,
        title: String,
        startsAtIso: String,
        timeLabel: String,
    ): Boolean {
        val startMillis = runCatching { Instant.parse(startsAtIso).toEpochMilliseconds() }
            .getOrNull() ?: return false
        val triggerAt = startMillis - LEAD_MILLIS
        if (triggerAt <= System.currentTimeMillis()) return false

        ensureChannel(context)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TIME, timeLabel)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            classId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
        return true
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Reminders 2 hours before a booked class" },
        )
    }
}
