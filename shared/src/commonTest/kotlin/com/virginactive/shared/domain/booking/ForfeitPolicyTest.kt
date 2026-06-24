package com.virginactive.shared.domain.booking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ForfeitPolicyTest {

    private companion object {
        val NOW: Instant = Instant.parse("2026-06-24T05:00:00Z")
    }

    private fun startsAtUtc(ahead: kotlin.time.Duration): String =
        (NOW + ahead).toString()

    @Test
    fun thirteenHoursAheadIsOutsideTheWindow() {
        val startsAt = startsAtUtc(13.hours)
        assertFalse(
            ForfeitPolicy.isWithinForfeitWindow(startsAt, NOW),
            "13h ahead is outside the 12h forfeit window",
        )
    }

    @Test
    fun exactlyTwelveHoursAheadIsTheInclusiveLowerEdge() {
        val startsAt = startsAtUtc(12.hours)
        assertTrue(
            ForfeitPolicy.isWithinForfeitWindow(startsAt, NOW),
            "exactly 12h ahead: now == start-12h is INSIDE the window (inclusive lower edge, BOOK-07)",
        )
    }

    @Test
    fun elevenFiftyNineAheadIsInsideTheWindow() {
        val startsAt = startsAtUtc(11.hours + 59.minutes)
        assertTrue(
            ForfeitPolicy.isWithinForfeitWindow(startsAt, NOW),
            "11h59m ahead is inside the forfeit window",
        )
    }

    @Test
    fun twelveOhOneAheadIsJustOutsideTheWindow() {
        val startsAt = startsAtUtc(12.hours + 1.minutes)
        assertFalse(
            ForfeitPolicy.isWithinForfeitWindow(startsAt, NOW),
            "12h01m ahead is just outside the forfeit window",
        )
    }

    @Test
    fun classAlreadyStartedIsNotWithinWindow() {
        val startsAt = startsAtUtc((-1).hours)
        assertFalse(
            ForfeitPolicy.isWithinForfeitWindow(startsAt, NOW),
            "a past/already-started class is not within the forfeit window (now >= start)",
        )
    }

    @Test
    fun offsetIndependence_sameAbsoluteInstantUnderTwoOffsetsYieldsEqualResult() {
        val startsAtPlus02 = "2026-06-24T13:00:00+02:00"
        val startsAtMinus05 = "2026-06-24T06:00:00-05:00"

        val resultPlus02 = ForfeitPolicy.isWithinForfeitWindow(startsAtPlus02, NOW)
        val resultMinus05 = ForfeitPolicy.isWithinForfeitWindow(startsAtMinus05, NOW)

        assertEquals(
            resultPlus02,
            resultMinus05,
            "same absolute instant under +02:00 and -05:00 must yield the SAME boolean (offset-aware, BOOK-07)",
        )
        assertTrue(resultPlus02, "6h ahead is inside the window (sanity on the +02:00 representation)")
        assertTrue(resultMinus05, "6h ahead is inside the window (sanity on the -05:00 representation)")
    }

    @Test
    fun malformedStartsAtReturnsFalseAndNeverThrows() {
        assertFalse(
            ForfeitPolicy.isWithinForfeitWindow("not-a-timestamp", NOW),
            "malformed startsAt -> false (guarded, never throws)",
        )
    }
}
