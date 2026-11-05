package com.blacklanebot

import android.util.Log
import java.util.Calendar
import java.util.regex.Pattern

object OfferParser {
    private const val TAG = "BLBot-Parser"

    // Matches patterns like "14 Aug · 12:30" or "Thu 14 Aug · 12:30" or "12:30"
    private val TIME_PATTERN = Pattern.compile(
        "(?:(\\d{1,2})\\s+([A-Za-z]{3})\\s*[\u00b7\\-\\ ]\\s*)?(\\d{1,2}):(\\d{2})"
    )

    private val MONTH_MAP = mapOf(
        "jan" to 0, "feb" to 1, "mar" to 2, "apr" to 3,
        "may" to 4, "jun" to 5, "jul" to 6, "aug" to 7,
        "sep" to 8, "oct" to 9, "nov" to 10, "dec" to 11
    )

    fun isFloridaRide(text: String): Boolean {
        val hasFlorida = BotConfig.FLORIDA_KEYWORDS.any { kw ->
            text.contains(kw, ignoreCase = kw.trimStart().length == kw.length)
        }
        Log.d(TAG, "isFlorida=$hasFlorida | text snippet: ${text.take(80)}")
        return hasFlorida
    }

    fun isCanadaRide(text: String): Boolean {
        return isFloridaRide(text)
    }

    fun isWithinTimeWindow(text: String, minHours: Float, maxHours: Float): Boolean {
        val matcher = TIME_PATTERN.matcher(text)
        if (!matcher.find()) {
            Log.d(TAG, "No time found in: ${text.take(80)}")
            return false
        }

        val dayStr = matcher.group(1)
        val monthStr = matcher.group(2)
        val hourStr = matcher.group(3)
        val minStr = matcher.group(4)

        if (hourStr == null || minStr == null) return false

        val target = Calendar.getInstance()

        if (dayStr != null && monthStr != null) {
            val monthIdx = MONTH_MAP[monthStr.lowercase()] ?: return false
            target.set(Calendar.MONTH, monthIdx)
            target.set(Calendar.DAY_OF_MONTH, dayStr.toInt())
        }

        target.set(Calendar.HOUR_OF_DAY, hourStr.toInt())
        target.set(Calendar.MINUTE, minStr.toInt())
        target.set(Calendar.SECOND, 0)
        target.set(Calendar.MILLISECOND, 0)

        val now = System.currentTimeMillis()
        var diffMs = target.timeInMillis - now

        if (dayStr == null && diffMs < 0) {
            target.add(Calendar.DAY_OF_MONTH, 1)
            diffMs = target.timeInMillis - now
        }

        val diffHours = diffMs.toFloat() / (1000 * 60 * 60)
        val valid = diffHours in minHours..maxHours
        Log.d(TAG, "Time diff: ${"%.2f".format(diffHours)}h, valid=$valid ($minHours..$maxHours)")
        return valid
    }
}
