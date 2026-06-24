package com.virginactive.shared.domain.home

import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeDisplayTest {

    @Test
    fun timeLabelPreservesWallClockForPositiveOffset() {
        assertEquals("12:00", DateTimeDisplay.timeLabel("2026-06-24T12:00:00+02:00"))
        assertEquals("17:30", DateTimeDisplay.timeLabel("2026-06-24T17:30:00+02:00"))
    }

    @Test
    fun timeLabelGuardsAgainstHardCodedSingleOffset() {
        assertEquals("17:30", DateTimeDisplay.timeLabel("2026-06-24T17:30:00+01:00"))
        assertEquals("08:00", DateTimeDisplay.timeLabel("2026-06-27T08:00:00+01:00"))
    }

    @Test
    fun dateLabelPreservesWallClockDate() {
        assertEquals("2026-06-24", DateTimeDisplay.dateLabel("2026-06-24T23:30:00+02:00"))
        assertEquals("2026-06-27", DateTimeDisplay.dateLabel("2026-06-27T08:00:00+01:00"))
    }

    @Test
    fun offsetLabelIsPreservedVerbatim() {
        assertEquals("+02:00", DateTimeDisplay.offsetLabel("2026-06-24T12:00:00+02:00"))
        assertEquals("+01:00", DateTimeDisplay.offsetLabel("2026-06-24T17:30:00+01:00"))
        assertEquals("Z", DateTimeDisplay.offsetLabel("2026-06-24T12:00:00Z"))
    }

    @Test
    fun handlesNegativeOffsetWithoutTreatingDateDashAsOffset() {
        assertEquals("09:15", DateTimeDisplay.timeLabel("2026-06-24T09:15:00-05:00"))
        assertEquals("-05:00", DateTimeDisplay.offsetLabel("2026-06-24T09:15:00-05:00"))
    }
}
