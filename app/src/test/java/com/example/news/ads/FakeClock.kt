package com.example.news.ads

import com.example.news.ads.policy.Clock

class FakeClock(var now: Long = 1_000_000L) : Clock {
    override fun nowMillis() = now
    fun advanceBy(millis: Long) {
        now += millis
    }
}
