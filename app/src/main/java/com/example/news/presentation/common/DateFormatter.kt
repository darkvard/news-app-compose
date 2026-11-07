package com.example.news.presentation.common

import android.text.format.DateUtils
import java.time.Instant

/** "2023-06-16T22:24:33Z" -> "2 hours ago". Falls back to the raw value if it can't be parsed. */
fun formatPublishedAt(raw: String?, now: Long = System.currentTimeMillis()): String {
    if (raw.isNullOrBlank()) return ""
    return runCatching {
        DateUtils.getRelativeTimeSpanString(
            Instant.parse(raw).toEpochMilli(),
            now,
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }.getOrDefault(raw)
}
