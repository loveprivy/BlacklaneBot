package com.blacklanebot

import android.content.Context
import android.content.SharedPreferences

object BotConfig {
    private const val PREFS = "bot_config"
    private const val KEY_MIN_HOURS = "min_hours"
    private const val KEY_MAX_HOURS = "max_hours"

    const val BLACKLANE_PACKAGE = "com.blacklane.passenger"
    const val MOCK_PACKAGE = "com.mockblacklane"

    // USA / Florida keywords used to identify Florida ride offers
    val FLORIDA_KEYWORDS = setOf(
        "FL", "Florida", "Miami", "MIA", "Fort Lauderdale", "FLL",
        "Boca Raton", "West Palm Beach", "PBI", "Orlando", "MCO", "USA", "United States"
    )

    // Backwards compatibility aliases
    val CANADA_KEYWORDS = FLORIDA_KEYWORDS
    val USA_KEYWORDS = emptySet<String>()

    fun getMinHours(ctx: Context): Float = prefs(ctx).getFloat(KEY_MIN_HOURS, 1.0f)
    fun getMaxHours(ctx: Context): Float = prefs(ctx).getFloat(KEY_MAX_HOURS, 2.0f)

    fun save(ctx: Context, minHours: Float, maxHours: Float) {
        prefs(ctx).edit().putFloat(KEY_MIN_HOURS, minHours).putFloat(KEY_MAX_HOURS, maxHours).apply()
    }

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
