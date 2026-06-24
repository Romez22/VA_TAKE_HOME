package com.virginactive.shared.domain.booking

import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

object ForfeitPolicy {

    private val WINDOW = 12.hours

    fun isWithinForfeitWindow(startsAtIso: String, now: Instant = Clock.System.now()): Boolean =
        runCatching {
            val start = Instant.parse(startsAtIso)
            now >= start - WINDOW && now < start
        }.getOrDefault(false)
}
