package com.example.news.ads.policy

/** Injectable time source so the policies can be unit tested without waiting. */
fun interface Clock {
    fun nowMillis(): Long

    companion object {
        val System = Clock { java.lang.System.currentTimeMillis() }
    }
}
