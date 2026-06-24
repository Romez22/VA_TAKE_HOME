package com.virginactive.shared.domain.home

object DateTimeDisplay {

    fun timeLabel(isoWithOffset: String): String {
        val afterT = isoWithOffset.substringAfter('T', missingDelimiterValue = "")
        if (afterT.isEmpty()) return isoWithOffset
        val time = afterT.substringBefore('+').substringBefore('-').substringBefore('Z')
        val parts = time.split(':')
        if (parts.size < 2) return isoWithOffset
        return "${parts[0]}:${parts[1]}"
    }

    fun dateLabel(isoWithOffset: String): String {
        val date = isoWithOffset.substringBefore('T', missingDelimiterValue = "")
        return if (date.isEmpty()) isoWithOffset else date
    }

    fun offsetLabel(isoWithOffset: String): String {
        val afterT = isoWithOffset.substringAfter('T', missingDelimiterValue = "")
        if (afterT.isEmpty()) return ""
        if (afterT.endsWith('Z')) return "Z"
        val plus = afterT.lastIndexOf('+')
        val minus = afterT.lastIndexOf('-')
        val idx = maxOf(plus, minus)
        return if (idx > 0) afterT.substring(idx) else ""
    }
}
