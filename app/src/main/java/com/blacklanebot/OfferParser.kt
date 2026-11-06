package com.blacklanebot

import android.util.Log
import java.util.Calendar
import java.util.regex.Pattern

object OfferParser {
    private const val TAG = "BLBot-Parser"

    private val TIME_PATTERN = Pattern.compile(
        "(?:(\\d{1,2})\\s+([A-Za-z]{3})\\s*[\u00b7\\-\\| ]\\s*)?(\\d{1,2}):(\\d{2})"
    )

    private val MONTH_MAP = mapOf(
        "jan" to 0, "feb" to 1, "mar" to 2, "apr" to 3,
        "may" to 4, "jun" to 5, "jul" to 6, "aug" to 7,
        "sep" to 8, "oct" to 9, "nov" to 10, "dec" to 11
    )

    fun isFloridaRide(text: String): Boolean {
        try {
            val hasFlorida = BotConfig.FLORIDA_KEYWORDS.any { kw ->
                text.contains(kw, ignoreCase = true)
            }
            Log.d(TAG, "isFlorida=$hasFlorida | text snippet: ${text.take(80)}")
            return hasFlorida
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Florida ride: ", e)
            return false
        }
    }

    fun isCanadaRide(text: String): Boolean {
        return isFloridaRide(text)
    }

    fun isWithinTimeWindow(text: String, minHours: Float, maxHours: Float): Boolean {
        try {
            val matcher = TIME_PATTERN.matcher(text)
            if (!matcher.find()) {
                Log.d(TAG, "No time found in: ${text.take(80)}")
                return false
            }

            val dayStr = matcher.group(1)
            val monthStr = matcher.group(2)?.lowercase()
            val hourStr = matcher.group(3)
            val minStr = matcher.group(4)

            val now = Calendar.getInstance()
            val offerTime = Calendar.getInstance()

            if (dayStr != null && monthStr != null && MONTH_MAP.containsKey(monthStr)) {
                offerTime.set(Calendar.MONTH, MONTH_MAP[monthStr]!!)
                offerTime.set(Calendar.DAY_OF_MONTH, dayStr.toInt())
                if (offerTime.before(now)) {
                    offerTime.add(Calendar.YEAR, 1)
                }
            }

            offerTime.set(Calendar.HOUR_OF_DAY, hourStr?.toInt() ?: 0)
            offerTime.set(Calendar.MINUTE, minStr?.toInt() ?: 0)
            offerTime.set(Calendar.SECOND, 0)
            offerTime.set(Calendar.MILLISECOND, 0)

            val diffMs = offerTime.timeInMillis - now.timeInMillis
            val diffHours = diffMs.toFloat() / (1000f * 60f * 60f)

            val match = diffHours in minHours..maxHours
            Log.d(TAG, "diffHours=$diffHours min=$minHours max=$maxHours match=$match")
            return match
        } catch (e: Exception) {
            Log.e(TAG, "Error checking time window: ", e)
            return false
        }
    }
}
