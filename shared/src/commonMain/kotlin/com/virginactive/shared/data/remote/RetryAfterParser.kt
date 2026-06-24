package com.virginactive.shared.data.remote

internal fun parseRetryAfter(raw: String?): String? {
    val value = raw?.trim()
    if (value.isNullOrEmpty()) return null

    value.toLongOrNull()?.let { seconds ->
        return if (seconds >= 0) value else null
    }

    val looksLikeHttpDate = runCatching {
        value.contains("GMT", ignoreCase = true) && value.any { it.isDigit() }
    }.getOrDefault(false)

    return if (looksLikeHttpDate) value else null
}
