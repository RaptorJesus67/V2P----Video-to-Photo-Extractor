package com.kcmitch.v2p.applets.settings

import android.app.ActivityManager
import android.content.Context
import java.util.Locale

fun getRamInfo(context: Context): String {
    return try {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        val availGb = memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0)
        val usedGb = totalGb - availGb
        String.format(Locale.US, "%.1f / %.1f GB Used", usedGb, totalGb)
    } catch (e: Exception) {
        "N/A"
    }
}
