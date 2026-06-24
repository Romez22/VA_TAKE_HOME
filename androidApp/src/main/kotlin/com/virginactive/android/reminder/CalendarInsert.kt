package com.virginactive.android.reminder

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import kotlin.time.Instant

object CalendarInsert {

    fun addToCalendar(
        context: Context,
        title: String,
        startsAtIso: String,
        endsAtIso: String,
        location: String,
    ): Boolean {
        val beginMillis = runCatching { Instant.parse(startsAtIso).toEpochMilliseconds() }
            .getOrNull() ?: return false
        val endMillis = runCatching { Instant.parse(endsAtIso).toEpochMilliseconds() }
            .getOrNull() ?: beginMillis

        val intent = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
