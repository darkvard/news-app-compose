package com.example.news.ads.policy

/**
 * Where native ads go in a feed: after item #[firstAfter], then after every [interval] items.
 * Defaults (4, 8) -> ads after items 4, 12, 20, ... (1-based), i.e. indices 3, 11, 19 (0-based).
 */
class NativeAdPlacement(
    private val firstAfter: Int = 4,
    private val interval: Int = 8
) {
    init {
        require(firstAfter > 0 && interval > 0) { "firstAfter and interval must be positive" }
    }

    /** True if an ad slot goes right after the item at [index] (0-based). */
    fun isAdAfter(index: Int): Boolean {
        val position = index + 1
        return position >= firstAfter && (position - firstAfter) % interval == 0
    }

    /** 0, 1, 2... : which ad slot sits after [index]. Only meaningful when [isAdAfter] is true. */
    fun slotAfter(index: Int): Int = (index + 1 - firstAfter) / interval
}
